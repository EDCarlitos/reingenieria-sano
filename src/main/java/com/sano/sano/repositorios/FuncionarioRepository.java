package com.sano.sano.repositorios;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sano.sano.models.Funcionario;

public interface FuncionarioRepository extends MongoRepository<Funcionario, String> {
    Funcionario findByNombre(String nombre);
}
