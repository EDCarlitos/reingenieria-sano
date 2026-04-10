package com.sano.sano.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "audit_logs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditLog {

    @Id
    private String id;
    private String usuario;
    private String accion;      // CREAR, EDITAR, ELIMINAR
    private String entidad;     // OFICIO, USUARIO, FUNCIONARIO
    private String detalle;
    private LocalDateTime fecha;
}
