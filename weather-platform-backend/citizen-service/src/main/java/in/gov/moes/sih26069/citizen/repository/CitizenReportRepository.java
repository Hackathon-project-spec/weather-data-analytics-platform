package in.gov.moes.sih26069.citizen.repository;

import in.gov.moes.sih26069.citizen.entity.CitizenReportEntity;
import in.gov.moes.sih26069.common.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitizenReportRepository extends JpaRepository<CitizenReportEntity, String> {
    List<CitizenReportEntity> findAllByOrderByCreatedAtDesc();
    List<CitizenReportEntity> findByVerificationStatusOrderByCreatedAtDesc(VerificationStatus status);
    List<CitizenReportEntity> findByStateOrderByCreatedAtDesc(String state);
    List<CitizenReportEntity> findByDistrictOrderByCreatedAtDesc(String district);
    long countByVerificationStatus(VerificationStatus status);
}
