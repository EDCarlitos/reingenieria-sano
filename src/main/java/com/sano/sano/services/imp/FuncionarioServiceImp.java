package com.sano.sano.services.imp;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sano.sano.models.Funcionario;
import com.sano.sano.repositorios.FuncionarioRepository;
import com.sano.sano.services.FuncionarioService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FuncionarioServiceImp implements FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    @Override
    public List<Funcionario> getAllFuncionarios() {
        return funcionarioRepository.findAllByOrderByNombreAsc();
    }

    @Override
    public List<Funcionario> getActiveFuncionarios() {
        return funcionarioRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Override
    public Funcionario findById(String id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado"));
    }

    @Override
    public Funcionario create(String nombre, String puesto) {
        if (funcionarioRepository.existsByNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un funcionario con ese nombre");
        }
        Funcionario f = new Funcionario();
        f.setNombre(nombre);
        f.setPuesto(puesto);
        f.setActivo(true);
        return funcionarioRepository.save(f);
    }

    @Override
    public Funcionario update(String id, String nombre, String puesto) {
        Funcionario f = findById(id);
        if (funcionarioRepository.existsByNombreAndIdNot(nombre, id)) {
            throw new IllegalArgumentException("Ya existe un funcionario con ese nombre");
        }
        f.setNombre(nombre);
        f.setPuesto(puesto);
        return funcionarioRepository.save(f);
    }

    @Override
    public void toggleActivo(String id) {
        Funcionario f = findById(id);
        f.setActivo(!f.isActivo());
        funcionarioRepository.save(f);
    }
}
