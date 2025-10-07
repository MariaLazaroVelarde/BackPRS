package pe.edu.vallegrande.vgmsdistribution.infrastructure.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Object>> handleCustomException(CustomException ex) {
        // Extrae el mensaje de error personalizado
        ErrorMessage errorMessage = ex.getErrorMessage();

        // Envuelve el mensaje en un ResponseDto
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);

        // Retorna la respuesta con el código de error
        return ResponseEntity.status(errorMessage.getErrorCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleGenericException(Exception ex) {
        int statusCode = (ex instanceof RuntimeException) ? 500 : 400;

        // Crea un mensaje de error genérico
        ErrorMessage errorMessage = new ErrorMessage(
                statusCode,
                "Error interno del servidor",
                ex.getMessage()
        );

        // Envuelve en ResponseDto
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);

        // Retorna la respuesta
        return ResponseEntity.status(statusCode).body(response);
    }
}
