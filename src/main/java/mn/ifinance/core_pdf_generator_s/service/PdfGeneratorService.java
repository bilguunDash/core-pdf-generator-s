package mn.ifinance.core_pdf_generator_s.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import mn.ifinance.core_pdf_generator_s.module.PdfBodyReq;
import mn.ifinance.core_pdf_generator_s.module.PdfRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    public ByteArrayOutputStream generatePdf(PdfRequest pdfRequest, List<PdfBodyReq> pdfBodyReqList) {
        // Use A4 page size for layout
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add header elements
            addHeaderElements(document, pdfRequest);

            // Add metadata
            if (pdfRequest.getMeta() != null) {
                addMetadata(document, pdfRequest.getMeta());
                document.add(Chunk.NEWLINE); // Add a line break after metadata
            }

            // Add data rows
            if (pdfRequest.getHeader() != null && !pdfBodyReqList.isEmpty()) {
                addDataRows(document, pdfRequest.getHeader(), pdfBodyReqList);
            }

            // Add printed date
            addPrintedDate(document);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return outputStream;
    }

    private void addHeaderElements(Document document, PdfRequest pdfRequest) throws IOException, DocumentException {
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3, 2});

        // Add logo directly to the first cell
        PdfPCell logoCell = new PdfPCell(addLogo(pdfRequest.getIcon()));
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setPaddingTop(10);
        logoCell.setPaddingLeft(5);
        logoCell.setPaddingBottom(20); // Add space below the logo
        headerTable.addCell(logoCell);

        // Add bank name and metadata next to the logo
        PdfPCell bankNameCell = new PdfPCell(createBankInfo());
        bankNameCell.setBorder(Rectangle.NO_BORDER);
        bankNameCell.setPaddingTop(0);
        bankNameCell.setPaddingBottom(25);
        bankNameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(bankNameCell);

        // Add title in the third cell
        PdfPCell titleCell = new PdfPCell(createTitle(pdfRequest));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingLeft(-80);
        titleCell.setPaddingTop(0);
        titleCell.setPaddingBottom(45);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(titleCell);

        document.add(headerTable);
    }

    private Image addLogo(String iconPath) throws IOException, DocumentException {
        try {
            if (iconPath != null && !iconPath.isEmpty()) {
                Image logo = Image.getInstance(iconPath);
                logo.scaleToFit(40, 40); // Adjust logo size
                logo.setAlignment(Element.ALIGN_LEFT);
                return logo;
            } else {
                // If no iconPath provided, return a default placeholder image
                return Image.getInstance("https://via.placeholder.com/40");
            }
        } catch (BadElementException | IOException e) {
            // If the image URL is invalid, handle the exception and use a default placeholder image
            System.out.println("Invalid image URL, using placeholder image.");
            Image placeholder = Image.getInstance("https://via.placeholder.com/40");
            placeholder.scaleToFit(40, 40); // Adjust placeholder size
            placeholder.setAlignment(Element.ALIGN_LEFT);
            return placeholder;
        }
    }

    // Method to create bank info
    private PdfPTable createBankInfo() {
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

        return bankInfoTable;
    }

    // Method to create the title
    private PdfPTable createTitle(PdfRequest pdfRequest) {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph(pdfRequest.getTitle(), titleFont);
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
        PdfPTable metaTable = new PdfPTable(4); // 4 columns to handle key-value pairs in a structured manner
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{1, 3, 1, 3}); // Adjust widths to fit the page size

        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // Add metadata cells in a structured manner
        for (int i = 0; i < metadata.size(); i++) {
            PdfRequest.Metadata meta = metadata.get(i);

            PdfPCell keyCell = new PdfPCell(new Phrase(meta.getTitle() + ":", metaFont)); // Adding colon here
            keyCell.setBorder(Rectangle.NO_BORDER);
            keyCell.setPaddingBottom(5);

            PdfPCell valueCell = new PdfPCell(new Phrase(meta.getValue(), metaFont));
            valueCell.setBorder(Rectangle.NO_BORDER);
            valueCell.setPaddingBottom(5);

            metaTable.addCell(keyCell);
            metaTable.addCell(valueCell);

            // Ensure that rows are complete with empty cells if metadata size is odd
            if (i == metadata.size() - 1 && metadata.size() % 2 != 0) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("", metaFont));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                emptyCell.setPaddingBottom(5);

                metaTable.addCell(emptyCell);
                metaTable.addCell(emptyCell);
            }
        }

        document.add(metaTable);
    }


    private void addDataRows(Document document, List<PdfRequest.Column> header, List<PdfBodyReq> bodyList) throws DocumentException {
        PdfPTable dataTable = new PdfPTable(header.size());
        dataTable.setWidthPercentage(100);
        dataTable.setSpacingBefore(20f); // Space before the table
        dataTable.setWidths(new float[]{2, 2, 2, 2, 2, 2, 3, 2}); // Adjust widths to fit the page size

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        for (PdfRequest.Column column : header) {
            PdfPCell headerCell = new PdfPCell(new Phrase(column.getFieldName(), headerFont));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setPaddingBottom(10);
            headerCell.setBorder(Rectangle.BOX);
            headerCell.setBorderColor(BaseColor.WHITE);
            dataTable.addCell(headerCell);
        }

        for (PdfBodyReq row : bodyList) {
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
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
    }

    private void addPrintedDate(Document document) throws DocumentException {
        Font printedDateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph printedDate = new Paragraph("Printed Date: " + java.time.LocalDate.now().toString(), printedDateFont);
        printedDate.setAlignment(Element.ALIGN_RIGHT);
        printedDate.setSpacingBefore(-290);
        document.add(printedDate);
    }
}
