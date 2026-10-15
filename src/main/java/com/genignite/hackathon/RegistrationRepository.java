package com.genignite.hackathon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RegistrationRepository extends JpaRepository<Registration,Long> {
  Optional<Registration> findByOrderId(String orderId);
}
