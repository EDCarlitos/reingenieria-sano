package com.sano.sano.dto;


import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OficioSaveDto {

    @NotEmpty(message = "El apellido paterno es obligatorio")
    private String paterno;
    @NotEmpty(message = "El apellido materno es obligatorio")
    private String materno;
    @NotEmpty(message = "Los nombres son obligatorios")
    private String nombres;
    @NotEmpty(message = "El campo 'contesta' es obligatorio")
    private String contesta;
    private boolean esRespuesta;

    @NotEmpty(message = "El asunto es obligatorio")
    private String asunto;
    @NotEmpty(message = "La observación es obligatoria")
    private String observacion;
    @NotEmpty(message = "Seleccione un funcionario")
    private String funcionarioId;


}
