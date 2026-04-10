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

    @Override
    public PageResultDto<OficioDto> getOficiosFiltrados(OficioFilterDto filter, int page, int size) {
        List<Criteria> criterias = new ArrayList<>();

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
                .map(o -> new OficioDto(
                        o.getId(),
                        o.getPaterno(),
                        o.getMaterno(),
                        o.getNombres(),
                        o.getContesta(),
                        o.isEsRespuesta(),
                        o.getAsunto(),
                        o.getObservacion(),
                        o.getFuncionario(),
                        o.getFecha(),
                        o.getHora()))
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
}
