package com.sano.sano.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Usuario {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String rol; // ADMIN, EMPLEADO

    private boolean activo = true;
}
