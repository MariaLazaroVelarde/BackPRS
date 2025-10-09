package pe.edu.vallegrande.vgmsdistribution.infrastructure.exception;

import lombok.Getter;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorMessage errorMessage;

    // Nuevo constructor que recibe los tres parámetros para crear el ErrorMessage internamente
    public CustomException(int errorCode, String error, String message) {
        super(message);
        this.errorMessage = new ErrorMessage(errorCode, error, message);
    }

    // Constructor que recibe directamente el ErrorMessage (mantener para compatibilidad)
    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    // Static helper method for common not found exceptions
    public static CustomException notFound(String entityType, String id) {
        return new CustomException(
                404,
                entityType + " not found",
                "No se encontró " + entityType + " con id: " + id
        );
    }

    public static CustomException badRequest(String message, String details) {
        return new CustomException(
                400,
                message,
                details
        );
    }

    public static CustomException internalServerError(String message, String details) {
        return new CustomException(
                500,
                message,
                details
        );
    }

}
