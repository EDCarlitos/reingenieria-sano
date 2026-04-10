package com.sano.sano.models;


import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "oficios")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Oficio {

    @Id
    private String id;
    private String paterno;
    private String materno;
    private String nombres;
    private String contesta;
    private boolean esRespuesta;
    private String asunto;
    private String observacion;
    private Funcionario funcionario;
    private LocalDate fecha;
    private String hora;
    private boolean eliminado;
    private String motivoEliminacion;
    private LocalDate fechaEliminacion;
}
