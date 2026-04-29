package com.sano.sano.services.imp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sano.sano.dto.OficioDto;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.dto.OficioSaveDto;
import com.sano.sano.dto.OficioUpdateDto;
import com.sano.sano.dto.PageResultDto;
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
    private final MongoTemplate mongoTemplate;

    @Override
    public void saveOficio(OficioSaveDto oficioSaveDto) {
        Funcionario funcionario = funcionarioRepository.findById(oficioSaveDto.getFuncionarioId()).orElse(null);

        int anioActual = LocalDate.now().getYear();
        Oficio ultimoOficio = oficioRepository.findTopByAnioOrderByNumeroOficioDesc(anioActual);
        int siguienteNumero = (ultimoOficio != null && ultimoOficio.getNumeroOficio() != null) ? ultimoOficio.getNumeroOficio() + 1 : 1;

        Oficio oficio = new Oficio();
        oficio.setNumeroOficio(siguienteNumero);
        oficio.setAnio(anioActual);
        oficio.setPaterno(oficioSaveDto.getPaterno());
        oficio.setMaterno(oficioSaveDto.getMaterno());
        oficio.setNombres(oficioSaveDto.getNombres());
        oficio.setContesta(oficioSaveDto.getContesta());
        oficio.setEsRespuesta(oficioSaveDto.isEsRespuesta());
        oficio.setAsunto(oficioSaveDto.getAsunto());
        oficio.setObservacion(oficioSaveDto.getObservacion());
        oficio.setFuncionario(funcionario);
        oficio.setFecha(LocalDate.now());
        oficio.setHora(LocalTime.now().toString());

        oficioRepository.save(oficio);
    }

    @Override
    public OficioDto updateOficio(String id, OficioUpdateDto dto) {
        Oficio oficio = oficioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oficio no encontrado"));

        Funcionario funcionario = funcionarioRepository.findById(dto.getFuncionarioId()).orElse(null);

        oficio.setPaterno(dto.getPaterno());
        oficio.setMaterno(dto.getMaterno());
        oficio.setNombres(dto.getNombres());
        oficio.setContesta(dto.getContesta());
        oficio.setEsRespuesta(dto.isEsRespuesta());
        oficio.setAsunto(dto.getAsunto());
        oficio.setObservacion(dto.getObservacion());
        oficio.setFuncionario(funcionario);

        oficio = oficioRepository.save(oficio);
        return mapToDto(oficio);
    }

    @Override
    public void deleteOficio(String id, String motivoEliminacion) {
        Oficio oficio = oficioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oficio no encontrado"));

        oficio.setEliminado(true);
        oficio.setMotivoEliminacion(motivoEliminacion);
        oficio.setFechaEliminacion(LocalDate.now());
        oficioRepository.save(oficio);
    }

    @Override
    public PageResultDto<OficioDto> getOficiosFiltrados(OficioFilterDto filter, int page, int size) {
        List<Criteria> criterias = new ArrayList<>();

        // Excluir registros eliminados
        criterias.add(Criteria.where("eliminado").ne(true));

        if (StringUtils.hasText(filter.getPaterno())) {
            criterias.add(Criteria.where("paterno").regex(filter.getPaterno(), "i"));
        }
        if (StringUtils.hasText(filter.getMaterno())) {
            criterias.add(Criteria.where("materno").regex(filter.getMaterno(), "i"));
        }
        if (StringUtils.hasText(filter.getNombres())) {
            criterias.add(Criteria.where("nombres").regex(filter.getNombres(), "i"));
        }
        if (StringUtils.hasText(filter.getAsunto())) {
            criterias.add(Criteria.where("asunto").regex(filter.getAsunto(), "i"));
        }
        if (StringUtils.hasText(filter.getFuncionarioNombre())) {
            criterias.add(Criteria.where("funcionario.nombre").regex(filter.getFuncionarioNombre(), "i"));
        }
        if (filter.getEsRespuesta() != null) {
            criterias.add(Criteria.where("esRespuesta").is(filter.getEsRespuesta()));
        }
        if (filter.getFechaDesde() != null) {
            criterias.add(Criteria.where("fecha").gte(filter.getFechaDesde()));
        }
        if (filter.getFechaHasta() != null) {
            criterias.add(Criteria.where("fecha").lte(filter.getFechaHasta()));
        }

        Query query = new Query();
        if (!criterias.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
        }

        long totalElements = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Oficio.class);

        Pageable pageable = PageRequest.of(page, size);
        query.with(pageable);

        List<Oficio> oficios = mongoTemplate.find(query, Oficio.class);

        List<OficioDto> content = oficios.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResultDto<>(
                content,
                totalElements,
                totalPages,
                page,
                size,
                page < totalPages - 1,
                page > 0);
    }

    private OficioDto mapToDto(Oficio o) {
        OficioDto dto = new OficioDto();
        dto.setId(o.getId());
        dto.setNumeroOficio(o.getNumeroOficio());
        dto.setAnio(o.getAnio());
        dto.setPaterno(o.getPaterno());
        dto.setMaterno(o.getMaterno());
        dto.setNombres(o.getNombres());
        dto.setContesta(o.getContesta());
        dto.setEsRespuesta(o.isEsRespuesta());
        dto.setAsunto(o.getAsunto());
        dto.setObservacion(o.getObservacion());
        dto.setFuncionario(o.getFuncionario());
        dto.setFecha(o.getFecha());
        dto.setHora(o.getHora());
        dto.setEliminado(o.isEliminado());
        dto.setMotivoEliminacion(o.getMotivoEliminacion());
        dto.setFechaEliminacion(o.getFechaEliminacion());
        return dto;
    }
}
