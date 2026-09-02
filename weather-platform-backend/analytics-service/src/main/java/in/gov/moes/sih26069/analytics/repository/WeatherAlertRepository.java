package in.gov.moes.sih26069.analytics.repository;

import in.gov.moes.sih26069.analytics.entity.WeatherAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlertEntity, String> {
    List<WeatherAlertEntity> findByIsActiveTrueAndExpiresAtAfterOrderBySentAtDesc(Instant now);
    Optional<WeatherAlertEntity> findByIdentifier(String identifier);
}
