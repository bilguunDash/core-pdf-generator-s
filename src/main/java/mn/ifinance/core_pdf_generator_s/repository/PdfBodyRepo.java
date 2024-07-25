package mn.ifinance.core_pdf_generator_s.repository;

import mn.ifinance.core_pdf_generator_s.module.PdfBodyReq;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfBodyRepo  extends MongoRepository<PdfBodyReq, String> {
}
