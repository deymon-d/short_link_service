package short_link_service.unit;

import short_link_service.core.validator.UrlValidator;
import short_link_service.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    @Test
    void validateUrl_ValidHttpUrl_ShouldNotThrow() {
        assertDoesNotThrow(() -> UrlValidator.validateUrl("https://www.example.com"));
    }

    @Test
    void validateUrl_ValidHttpsUrl_ShouldNotThrow() {
        assertDoesNotThrow(() -> UrlValidator.validateUrl("https://www.example.com/path?query=1"));
    }

    @Test
    void validateUrl_NullUrl_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateUrl(null));
    }

    @Test
    void validateUrl_EmptyUrl_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateUrl(""));
    }

    @Test
    void validateUrl_NoProtocol_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateUrl("www.example.com"));
    }

    @Test
    void validateUrl_InvalidProtocol_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateUrl("file:///etc/passwd"));
    }

    @Test
    void validateUrl_TooLongUrl_ShouldThrow() {
        String longUrl = "https://" + "a".repeat(3000) + ".com";
        assertThrows(ValidationException.class, () -> UrlValidator.validateUrl(longUrl));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:8080", "https://127.0.0.1/api", "http://192.168.1.1:3000"})
    void validateUrl_LocalhostUrl_ShouldAllowWithWarning(String url) {
        assertDoesNotThrow(() -> UrlValidator.validateUrl(url));
    }

    @Test
    void validateMaxClicks_ValidRange_ShouldNotThrow() {
        assertDoesNotThrow(() -> UrlValidator.validateMaxClicks(1));
        assertDoesNotThrow(() -> UrlValidator.validateMaxClicks(100));
        assertDoesNotThrow(() -> UrlValidator.validateMaxClicks(10000));
    }

    @Test
    void validateMaxClicks_Zero_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateMaxClicks(0));
    }

    @Test
    void validateMaxClicks_Negative_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateMaxClicks(-1));
    }

    @Test
    void validateMaxClicks_TooLarge_ShouldThrow() {
        assertThrows(ValidationException.class, () -> UrlValidator.validateMaxClicks(10001));
    }
}
