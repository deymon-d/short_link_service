package short_link_service.core.validator;

import short_link_service.config.AppConfig;
import short_link_service.exception.ValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class UrlValidator {
    private static final Set<String> ALLOWED_PROTOCOLS = new HashSet<>();

    static {
        String[] protocols = AppConfig.ALLOWED_PROTOCOLS.split(",");
        ALLOWED_PROTOCOLS.addAll(Arrays.asList(protocols));
    }

    public static void validateUrl(String url) throws ValidationException {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("URL cannot be null or empty");
        }

        if (url.length() > AppConfig.MAX_URL_LENGTH) {
            throw new ValidationException("URL is too long (max " + AppConfig.MAX_URL_LENGTH + " characters)");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw new ValidationException("URL must have a protocol (http://, https://, etc.)");
            }

            if (!ALLOWED_PROTOCOLS.contains(scheme.toLowerCase())) {
                throw new ValidationException(
                        "Protocol '" + scheme + "' is not allowed. " + "Allowed protocols: " + ALLOWED_PROTOCOLS);
            }

            String host = uri.getHost();
            if (host == null) {
                throw new ValidationException("URL must have a valid host");
            }

            int port = uri.getPort();
            if (port != -1 && (port < 1 || port > 65535)) {
                throw new ValidationException("Invalid port number: " + port);
            }

        } catch (URISyntaxException e) {
            throw new ValidationException("Invalid URL format: " + e.getMessage());
        }
    }

    public static void validateMaxClicks(int maxClicks) throws ValidationException {
        if (maxClicks < 1) {
            throw new ValidationException("Max clicks must be at least 1");
        }
        if (maxClicks > AppConfig.MAX_CLICKS_LIMIT) {
            throw new ValidationException("Max clicks cannot exceed " + AppConfig.MAX_CLICKS_LIMIT);
        }
    }

    public static void validateExpiration(LocalDateTime expirationDate) throws ValidationException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime minExpiration = now.plusMinutes(AppConfig.MIN_EXPIRATION_MINUTES);
        LocalDateTime maxExpiration = now.plusDays(AppConfig.MAX_EXPIRATION_DAYS);

        if (expirationDate.isBefore(minExpiration)) {
            throw new ValidationException(
                    "Expiration date must be at least " + AppConfig.MIN_EXPIRATION_MINUTES + " minutes in the future");
        }
        if (expirationDate.isAfter(maxExpiration)) {
            throw new ValidationException(
                    "Expiration date cannot be more than " + AppConfig.MAX_EXPIRATION_DAYS + " days in the future");
        }
    }
}
