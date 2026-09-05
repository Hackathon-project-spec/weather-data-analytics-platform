package in.gov.moes.sih26069.analytics.repository;

import in.gov.moes.sih26069.analytics.entity.AiEventEntity;
import in.gov.moes.sih26069.common.enums.OperationalEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiEventRepository extends JpaRepository<AiEventEntity, String> {
    List<AiEventEntity> findByOperationalStatusOrderByCreatedAtDesc(OperationalEventStatus status);
    List<AiEventEntity> findByEventTypeOrderByCreatedAtDesc(String eventType);
    List<AiEventEntity> findBySeverityOrderByCreatedAtDesc(String severity);
    List<AiEventEntity> findByStateOrderByCreatedAtDesc(String state);
    List<AiEventEntity> findByCityOrderByCreatedAtDesc(String city);
    List<AiEventEntity> findAllByOrderByCreatedAtDesc();

    long countByOperationalStatus(OperationalEventStatus status);
    long countBySeverity(String severity);
    long countByEventType(String eventType);
}
