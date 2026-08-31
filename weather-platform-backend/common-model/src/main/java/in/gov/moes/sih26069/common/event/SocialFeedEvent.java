package in.gov.moes.sih26069.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import in.gov.moes.sih26069.common.enums.DisasterCategory;
import java.time.Instant;
import java.util.List;

public class SocialFeedEvent {
    private String postId;
    private String platform; // "Twitter/X (Simulated)", "Telegram (Simulated)"
    private String authorHandle;
    private String state;
    private String district;
    private double latitude;
    private double longitude;
    private DisasterCategory disasterCategory;
    private String text;
    private List<String> hashtags;
    private double sentimentScore; // -1.0 (Critical/Panic) to +1.0
    private boolean isSimulated = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public SocialFeedEvent() {
        this.timestamp = Instant.now();
        this.isSimulated = true;
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getAuthorHandle() { return authorHandle; }
    public void setAuthorHandle(String authorHandle) { this.authorHandle = authorHandle; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public DisasterCategory getDisasterCategory() { return disasterCategory; }
    public void setDisasterCategory(DisasterCategory disasterCategory) { this.disasterCategory = disasterCategory; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getHashtags() { return hashtags; }
    public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

    public double getSentimentScore() { return sentimentScore; }
    public void setSentimentScore(double sentimentScore) { this.sentimentScore = sentimentScore; }

    public boolean isSimulated() { return isSimulated; }
    public void setSimulated(boolean simulated) { isSimulated = simulated; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
