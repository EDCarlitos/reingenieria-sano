package com.sano.sano.dto;

import java.time.LocalDate;

import com.sano.sano.models.Funcionario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OficioDto {

    protected String id;
    protected Integer numeroOficio;
    protected Integer anio;
    protected String paterno;
    protected String materno;
    protected String nombres;
    protected String contesta;
    protected boolean esRespuesta;
    protected String asunto;
    protected String observacion;
    protected Funcionario funcionario;
    protected LocalDate fecha;
    protected String hora;
    protected boolean eliminado;
    protected String motivoEliminacion;
    protected LocalDate fechaEliminacion;

}
