package mn.ifinance.core_pdf_generator_s.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import mn.ifinance.core_pdf_generator_s.module.PdfRequest;
import mn.ifinance.core_pdf_generator_s.repository.PdfRequestRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private final PdfRequestRepository pdfRequestRepository;

    public ByteArrayOutputStream generatePdf(PdfRequest pdfRequest) {
        // Use A4 page size for layout
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add header elements
            addHeaderElements(document);

            // Add metadata
            if (pdfRequest.getMeta() != null) {
                addMetadata(document, pdfRequest.getMeta());
                document.add(Chunk.NEWLINE); // Add a line break after metadata
            }

            // Add data rows
            if (pdfRequest.getHeader() != null && pdfRequest.getBody() != null) {
                addDataRows(document, pdfRequest.getHeader(), pdfRequest.getBody());
            }

            // Add printed date if not already present in metadata
            if (pdfRequest.getMeta() == null || pdfRequest.getMeta().stream().noneMatch(meta -> "Printed Date".equals(meta.getTitle()))) {
                addPrintedDate(document);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream;
    }

    private void addHeaderElements(Document document) throws IOException, DocumentException {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3, 2});

        // Add logo directly to the first cell
        PdfPCell logoCell = new PdfPCell(addLogo());
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPaddingTop(10);
        logoCell.setPaddingLeft(5);
        headerTable.addCell(logoCell);

        // Add bank name and metadata next to the logo
        PdfPCell bankNameCell = new PdfPCell(createBankInfo());
        bankNameCell.setBorder(Rectangle.NO_BORDER);
        bankNameCell.setPaddingTop(0);
        bankNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(bankNameCell);

        // Add title in the third cell
        PdfPCell titleCell = new PdfPCell(createTitle());
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingLeft(-80);
        titleCell.setPaddingTop(-15);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(titleCell);

        document.add(headerTable);
    }

    // Method to create the logo element
    private Image addLogo() throws IOException, DocumentException {
        Resource imgFile = new ClassPathResource("khan-bank.png");
        if (imgFile.exists()) {
            Image logo = Image.getInstance(imgFile.getURL());
            logo.scaleToFit(40, 40); // Adjust logo siz
            logo.setAlignment(Element.ALIGN_LEFT);
            return logo;
        } else {
            System.out.println("Image file not found!");
            return null;
        }
    }

    // Method to create bank info
    private PdfPTable createBankInfo() throws DocumentException {
        PdfPTable bankInfoTable = new PdfPTable(1);
        bankInfoTable.setWidthPercentage(100);

        Font bankNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph bankName = new Paragraph("KHAAN BANK", bankNameFont);
        bankName.setAlignment(Element.ALIGN_LEFT);
        PdfPCell bankNameCell = new PdfPCell(bankName);
        bankNameCell.setBorder(Rectangle.NO_BORDER);
        bankNameCell.setPaddingLeft(-30);
        bankNameCell.setPaddingTop(5);
        bankNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        bankInfoTable.addCell(bankNameCell);

        PdfPTable metaDataTable = new PdfPTable(2);
        metaDataTable.setWidthPercentage(100);
        metaDataTable.setWidths(new float[]{1, 3});

        PdfPCell metaDataCell = new PdfPCell(metaDataTable);
        metaDataCell.setBorder(Rectangle.NO_BORDER);
        bankInfoTable.addCell(metaDataCell);

        return bankInfoTable;
    }


    // Method to create the title
    private PdfPTable createTitle() {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Deposit Account Statement", titleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        PdfPCell titleCell = new PdfPCell(title);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingLeft(-30);
        titleCell.setPaddingTop(20);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleTable.addCell(titleCell);

        return titleTable;
    }

    private void addMetadata(Document document, List<PdfRequest.Metadata> metadata) throws DocumentException {
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{1, 3}); // Adjust widths to fit the page size

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        for (PdfRequest.Metadata meta : metadata) {
            PdfPCell keyCell = new PdfPCell(new Phrase(meta.getTitle() + ":", metaFont));
            keyCell.setBorder(Rectangle.NO_BORDER);
            keyCell.setPaddingBottom(5);

            PdfPCell valueCell = new PdfPCell(new Phrase(meta.getValue(), metaFont));
            valueCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setPaddingBottom(5);

            metaTable.addCell(keyCell);
            metaTable.addCell(valueCell);
        }

        document.add(metaTable);
    }

    private void addDataRows(Document document, List<PdfRequest.Column> header, List<PdfRequest.DataRow> body) throws DocumentException {
        PdfPTable dataTable = new PdfPTable(header.size());
        dataTable.setWidthPercentage(100);
        dataTable.setSpacingBefore(20f); // Space before the table
        dataTable.setWidths(new float[]{1, 1, 2, 2, 2, 2, 3, 2}); // Adjust widths to fit the page size

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        for (PdfRequest.Column column : header) {
            PdfPCell headerCell = new PdfPCell(new Phrase(column.getFieldName(), headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPaddingBottom(10);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderColor(BaseColor.BLACK);
            dataTable.addCell(headerCell);
        }

        for (PdfRequest.DataRow row : body) {
            addDataCell(dataTable, row.getDate(), dataFont);
            addDataCell(dataTable, row.getBranch(), dataFont);
            addDataCell(dataTable, row.getStartBalance(), dataFont);
            addDataCell(dataTable, row.getDebit(), dataFont);
            addDataCell(dataTable, row.getCredit(), dataFont);
            addDataCell(dataTable, row.getEndBalance(), dataFont);
            addDataCell(dataTable, row.getDescription(), dataFont);
            addDataCell(dataTable, row.getTargetAccount(), dataFont);
        }

        document.add(dataTable);
    }

    private void addDataCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingBottom(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(BaseColor.BLACK); // Set border color to white
        table.addCell(cell);
    }

    private void addPrintedDate(Document document) throws DocumentException {
        Font printedDateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph printedDate = new Paragraph("Printed Date: " + java.time.LocalDate.now().toString(), printedDateFont);
        printedDate.setAlignment(Element.ALIGN_RIGHT);
        printedDate.setSpacingBefore(10); // Adjust spacing

        document.add(printedDate);
    }
}
