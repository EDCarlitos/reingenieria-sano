package com.sano.dto;

import com.sano.sano.models.Funcionario;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class OficioDto {
    

    protected String paterno;
    protected String materno;
    protected String nombres;
    protected String contesta;
    protected boolean esRespuesta;
    protected String asunto;
    protected String observacion;
    protected Funcionario funcionario;

}
