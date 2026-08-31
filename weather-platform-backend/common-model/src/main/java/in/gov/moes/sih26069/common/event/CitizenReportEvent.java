package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import java.time.Instant;

public class CitizenReportEvent {
    private String reportId;
    private String reporterName;
    private String reporterContactHash;
    private DisasterCategory category;
    private int severityLevel; // 1 to 5
    private double latitude;
    private double longitude;
    private String state;
    private String district;
    private String description;
    private String mediaUrl;
    private int upvotes;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public CitizenReportEvent() {
        this.timestamp = Instant.now();
    }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterContactHash() { return reporterContactHash; }
    public void setReporterContactHash(String reporterContactHash) { this.reporterContactHash = reporterContactHash; }

    public DisasterCategory getCategory() { return category; }
    public void setCategory(DisasterCategory category) { this.category = category; }

    public int getSeverityLevel() { return severityLevel; }
    public void setSeverityLevel(int severityLevel) { this.severityLevel = severityLevel; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public int getUpvotes() { return upvotes; }
    public void setUpvotes(int upvotes) { this.upvotes = upvotes; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
