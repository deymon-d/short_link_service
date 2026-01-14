package short_link_service.core.generator;

import short_link_service.config.AppConfig;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureHashLinkGenerator implements LinkGenerator {
    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateShortUrl(String originalUrl) {
        String salt = generateSalt();
        String input = originalUrl + ":" + salt;
        String hash = generateSHA256Hash(input);
        String base62 = base62Encode(hash);
        int length = Math.min(AppConfig.SHORT_URL_LENGTH, base62.length());
        return base62.substring(0, length);
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(salt);
    }

    private String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            byte[] doubleHash = digest.digest(hashBytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(doubleHash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate hash", e);
        }
    }

    private String base62Encode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        long number = 0;

        for (byte b : bytes) {
            number = (number << 8) + (b & 0xFF);
        }

        StringBuilder result = new StringBuilder();
        while (number > 0) {
            result.insert(0, BASE62.charAt((int) (number % 62)));
            number /= 62;
        }
        while (result.length() < AppConfig.SHORT_URL_LENGTH) {
            result.append(BASE62.charAt(random.nextInt(BASE62.length())));
        }
        return result.toString();
    }
}
