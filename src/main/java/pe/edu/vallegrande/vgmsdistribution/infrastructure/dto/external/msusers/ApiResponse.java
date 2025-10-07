package pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.external.msusers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsdistribution.infrastructure.dto.ErrorMessage;

/**
 * DTO genérico para la respuesta de la API de MS-USERS
 * @param <T> Tipo de los datos contenidos en la respuesta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ErrorMessage error;
}