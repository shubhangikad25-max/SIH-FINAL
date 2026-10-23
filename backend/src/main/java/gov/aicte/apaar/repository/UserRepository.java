package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByGoogleSub(String googleSub);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByApprovalStatus(String approvalStatus);
    List<User> findByInstitutionId(String institutionId);
}
