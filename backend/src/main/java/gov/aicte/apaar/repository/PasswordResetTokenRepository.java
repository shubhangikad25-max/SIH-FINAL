package gov.aicte.apaar.repository;

import gov.aicte.apaar.model.PasswordResetToken;
import gov.aicte.apaar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    void deleteByUser(User user);
}
