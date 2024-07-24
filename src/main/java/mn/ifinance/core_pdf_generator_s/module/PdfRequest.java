package mn.ifinance.core_pdf_generator_s.module;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "pdf_requests")
public class PdfRequest {

    @Id
    private String id;
    private List<Column> header;
    private List<DataRow> body;
    private String title;
    private String icon; // Optional
    private List<Metadata> meta; // Optional

    @Getter
    @Setter
    public static class Column {
        private String fieldName;
        private String fieldKey;
    }

    @Getter
    @Setter
    public static class DataRow {
        private String date;
        private String branch;
        private String startBalance;
        private String debit;
        private String credit;
        private String endBalance;
        private String description;
        private String targetAccount;
    }

    @Getter
    @Setter
    public static class Metadata {
        private String title;
        private String value;
    }
}

