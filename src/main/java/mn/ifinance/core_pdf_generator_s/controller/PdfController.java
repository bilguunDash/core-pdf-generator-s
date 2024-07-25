package mn.ifinance.core_pdf_generator_s.controller;

import com.itextpdf.text.DocumentException;
import mn.ifinance.core_pdf_generator_s.module.PdfBodyReq;
import mn.ifinance.core_pdf_generator_s.module.PdfRequest;
import mn.ifinance.core_pdf_generator_s.repository.PdfBodyRepo;
import mn.ifinance.core_pdf_generator_s.service.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfGeneratorService pdfGeneratorService;
    private final PdfBodyRepo pdfBodyReqRepository;

    public PdfController(PdfGeneratorService pdfGeneratorService, PdfBodyRepo pdfBodyReqRepository) {
        this.pdfGeneratorService = pdfGeneratorService;
        this.pdfBodyReqRepository = pdfBodyReqRepository;
    }

    @PostMapping("/store")
    public ResponseEntity<PdfBodyReq> storePdfBodyReq(@RequestBody PdfBodyReq pdfBodyReq) {
        PdfBodyReq savedRequest = pdfBodyReqRepository.save(pdfBodyReq);
        return ResponseEntity.ok(savedRequest);
    }

    @PostMapping("/generate/{id}")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfRequest pdfRequest, @PathVariable Integer id) throws DocumentException, IOException {
        Optional<PdfBodyReq> pdfBodyReqOptional = pdfBodyReqRepository.findById(id.toString());

        if (pdfBodyReqOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PdfBodyReq pdfBodyReq = pdfBodyReqOptional.get();
        ByteArrayOutputStream pdfOutputStream = pdfGeneratorService.generatePdf(pdfRequest, List.of(pdfBodyReq));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "generated.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfOutputStream.toByteArray());
    }

    @PostMapping("/all-generate")
    public ResponseEntity<byte[]> allGenerate(@RequestBody PdfRequest pdfRequest) throws DocumentException, IOException {
        List<PdfBodyReq> pdfBodyReqList = pdfBodyReqRepository.findAll();
        if (pdfBodyReqList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ByteArrayOutputStream pdfOutputStream = pdfGeneratorService.generatePdf(pdfRequest, pdfBodyReqList);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "generated.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfOutputStream.toByteArray());
    }
}
