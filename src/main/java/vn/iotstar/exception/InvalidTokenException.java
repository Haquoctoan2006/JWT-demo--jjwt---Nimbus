package vn.iotstar.exception;

/**
 * Nem ra khi JWT khong hop le / sai chu ky / het han (dung cho NimbusTokenService,
 * de co hanh vi 401 giong voi cac ngoai le cua jjwt).
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
