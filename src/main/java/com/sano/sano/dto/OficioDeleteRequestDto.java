package com.sano.sano.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OficioDeleteRequestDto {

    @NotEmpty(message = "El motivo de eliminación es obligatorio")
    private String motivoEliminacion;

    private String username;

    @NotEmpty(message = "La contraseña es obligatoria")
    private String password;
}
