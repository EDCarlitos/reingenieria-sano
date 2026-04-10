package com.sano.sano.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class OficioFilterDto {

    private String paterno;
    private String materno;
    private String nombres;
    private String asunto;
    private String funcionarioNombre;
    private Boolean esRespuesta;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;
}
