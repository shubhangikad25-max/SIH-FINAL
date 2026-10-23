package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, Long> {
    List<ConsentRecord> findByApaarIdOrderByTimestampDesc(String apaarId);
}
