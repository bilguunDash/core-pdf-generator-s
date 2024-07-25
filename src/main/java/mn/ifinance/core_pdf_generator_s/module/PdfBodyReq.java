package mn.ifinance.core_pdf_generator_s.module;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "pdf_body")
public class PdfBodyReq {
    @Id
    private String id;
    private String date;
    private String branch;
    private String startBalance;
    private String debit;
    private String credit;
    private String endBalance;
    private String description;
    private String targetAccount;
}
