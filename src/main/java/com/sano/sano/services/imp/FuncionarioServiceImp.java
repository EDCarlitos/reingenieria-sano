package com.sano.sano.services.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sano.sano.models.Funcionario;
import com.sano.sano.repositorios.FuncionarioRepository;
import com.sano.sano.services.FuncionarioService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FuncionarioServiceImp implements  FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;  



    @Override
    public List<Funcionario> getAllFuncionarios() {


        return funcionarioRepository.findAll();
    }

    
    
}
