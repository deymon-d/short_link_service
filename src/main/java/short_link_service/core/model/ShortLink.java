package short_link_service.core.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private final String id;
    private final String originalUrl;
    private final String shortUrl;
    private final String userId;
    private final LocalDateTime creationDate;
    private LocalDateTime expirationDate;
    private int clickCount;
    private int maxClicks;
    private String title;
    private String description;

    public ShortLink(String originalUrl, String shortUrl, String userId, LocalDateTime expirationDate, int maxClicks) {
        this.id = UUID.randomUUID().toString();
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.userId = userId;
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.clickCount = 0;
        this.maxClicks = maxClicks;
    }

    public void update(String title, String description, Integer maxClicks, LocalDateTime expirationDate) {
        if (title != null)
            this.title = title;
        if (description != null)
            this.description = description;
        if (maxClicks != null) {
            if (maxClicks < clickCount) {
                throw new IllegalArgumentException("New max clicks cannot be less than current click count");
            }
            this.maxClicks = maxClicks;
        }
        if (expirationDate != null) {
            if (expirationDate.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Expiration date cannot be in the past");
            }
            this.expirationDate = expirationDate;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }

    public boolean isClickLimitExceeded() {
        return clickCount >= maxClicks;
    }

    public void incrementClickCount() {
        this.clickCount++;
    }

    public String getId() {
        return id;
    }
    public String getOriginalUrl() {
        return originalUrl;
    }
    public String getShortUrl() {
        return shortUrl;
    }
    public String getUserId() {
        return userId;
    }
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    public int getClickCount() {
        return clickCount;
    }
    public int getMaxClicks() {
        return maxClicks;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
