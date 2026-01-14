package short_link_service.exception;

public class LinkLimitExceededException extends RuntimeException {
    public LinkLimitExceededException(String message) {
        super(message);
    }
}
