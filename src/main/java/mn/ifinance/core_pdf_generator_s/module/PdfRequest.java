package mn.ifinance.core_pdf_generator_s.module;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@Setter
public class PdfRequest {

    @Id
    private String id;
    private List<Column> header;
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
    public static class Metadata {
        private String title;
        private String value;
    }
}

