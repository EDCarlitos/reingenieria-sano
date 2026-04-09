package com.sano.sano.services.imp;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.stereotype.Service;

import com.sano.dto.OficioSaveDto;
import com.sano.sano.models.Funcionario;
import com.sano.sano.models.Oficio;
import com.sano.sano.repositorios.FuncionarioRepository;
import com.sano.sano.repositorios.OficioRepository;
import com.sano.sano.services.OficioService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OficioServiceImp implements OficioService {

    private final OficioRepository oficioRepository;
    private final FuncionarioRepository funcionarioRepository;

   

    @Override
    public void saveOficio(OficioSaveDto oficioSaveDto) {

        Funcionario funcionario = funcionarioRepository.findById(oficioSaveDto.getFuncionarioId()).orElse(null);

        Oficio oficio = new Oficio(null,
                oficioSaveDto.getPaterno(),
                oficioSaveDto.getMaterno(),
                oficioSaveDto.getNombres(),
                oficioSaveDto.getContesta(),
                oficioSaveDto.isEsRespuesta(),
                oficioSaveDto.getAsunto(),
                oficioSaveDto.getObservacion(),
                funcionario,
                LocalDate.now(),
                LocalTime.now().toString());

            oficioRepository.save(oficio);

    }

}
