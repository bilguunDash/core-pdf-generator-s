package mn.ifinance.core_pdf_generator_s.controller;

import mn.ifinance.core_pdf_generator_s.module.PdfRequest;
import mn.ifinance.core_pdf_generator_s.service.PdfGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final PdfGeneratorService pdfGeneratorService;

    public PdfController(PdfGeneratorService pdfGeneratorService) {
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfRequest pdfRequest) {
        ByteArrayOutputStream pdfOutputStream = pdfGeneratorService.generatePdf(pdfRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "generated.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfOutputStream.toByteArray());
    }
}
