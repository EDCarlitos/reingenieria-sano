package com.sano.sano.repositorios;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sano.sano.models.Funcionario;

public interface FuncionarioRepository extends MongoRepository<Funcionario, String> {
    Funcionario findByNombre(String nombre);
    List<Funcionario> findAllByOrderByNombreAsc();
    List<Funcionario> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreAndIdNot(String nombre, String id);
    boolean existsByNombre(String nombre);
}
