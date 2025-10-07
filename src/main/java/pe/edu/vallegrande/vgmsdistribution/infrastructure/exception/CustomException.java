package pe.edu.vallegrande.vgmsdistribution.infrastructure.exception;

import org.springframework.http.HttpStatus;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;

public class CustomException extends RuntimeException {

    private final ErrorMessage errorMessage;

    // Constructor que recibe directamente el ErrorMessage (mantener para compatibilidad)
    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    // Nuevo constructor que recibe los tres parámetros para crear el ErrorMessage internamente
    public CustomException(int errorCode, String error, String message) {
        super(message);
        this.errorMessage = new ErrorMessage(errorCode, error, message);
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    // Static helper method for common not found exceptions
    public static CustomException notFound(String entityType, String id) {
        return new CustomException(
            HttpStatus.NOT_FOUND.value(),
            entityType + " not found",
            "The requested " + entityType.toLowerCase() + " with id " + id + " was not found"
        );
    }
}
