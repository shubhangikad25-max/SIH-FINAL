package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.DuplicateConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DuplicateConflictRepository extends JpaRepository<DuplicateConflict, Long> {
    List<DuplicateConflict> findByPrimaryCollegeIdOrderByDetectedAtDesc(String primaryCollegeId);
    List<DuplicateConflict> findByStatusOrderByDetectedAtDesc(String status);
    List<DuplicateConflict> findAllByOrderByDetectedAtDesc();
    Optional<DuplicateConflict> findByApaarIdAndStatus(String apaarId, String status);
    long countByStatus(String status);
}
