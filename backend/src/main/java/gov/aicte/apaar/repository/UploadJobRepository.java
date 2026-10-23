package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.UploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, Long> {
    List<UploadJob> findByInstitutionIdOrderByCreatedAtDesc(String institutionId);
}
