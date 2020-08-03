package pl.sector17.sector17.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sector17.sector17.model.VerificationToken;

public interface TokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByToken(String token);
    VerificationToken findByUserId(Long id);
    VerificationToken deleteByUserId(Long id);
}
