package short_link_service.config;

public class AppConfig {
    public static final int DEFAULT_EXPIRATION_HOURS = ConfigLoader.getIntProperty("link.default.expiration.hours", 24);
    public static final int MAX_EXPIRATION_DAYS = ConfigLoader.getIntProperty("link.max.expiration.days", 365);
    public static final int MIN_EXPIRATION_MINUTES = ConfigLoader.getIntProperty("link.min.expiration.minutes", 5);
    public static final int DEFAULT_MAX_CLICKS = ConfigLoader.getIntProperty("link.default.max.clicks", 10);
    public static final int MAX_CLICKS_LIMIT = ConfigLoader.getIntProperty("link.max.clicks.limit", 10000);
    public static final int SHORT_URL_LENGTH = ConfigLoader.getIntProperty("link.short.url.length", 8);
    public static final String SHORT_URL_PREFIX = ConfigLoader.getProperty("link.short.url.prefix", "clck.ru/");

    public static final String ALLOWED_PROTOCOLS = ConfigLoader.getProperty("security.allowed.protocols",
            "http,https,ftp");
    public static final int MAX_URL_LENGTH = ConfigLoader.getIntProperty("security.max.url.length", 2048);
}
