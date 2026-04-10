package com.sano.sano.services;

import java.util.List;

import com.sano.sano.models.Funcionario;

public interface FuncionarioService {
    
    List<Funcionario> getAllFuncionarios();
    List<Funcionario> getActiveFuncionarios();
    Funcionario findById(String id);
    Funcionario create(String nombre, String puesto);
    Funcionario update(String id, String nombre, String puesto);
    void toggleActivo(String id);
}
