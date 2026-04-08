package com.sano.sano.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "funcionarios")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Funcionario {
    
    @Id
    private String id;
    private String nombre;
    private String puesto;

}
