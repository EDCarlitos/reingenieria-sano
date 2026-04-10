package com.sano.sano.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sano.sano.dto.OficioDto;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.dto.OficioSaveDto;
import com.sano.sano.dto.PageResultDto;
import com.sano.sano.services.OficioService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/oficios")
@AllArgsConstructor
public class OficioApiController {

    private final OficioService oficioService;

    @GetMapping
    public PageResultDto<OficioDto> buscar(
            @ModelAttribute OficioFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        return oficioService.getOficiosFiltrados(filter, page, size);
    }

    @PostMapping
    public void guardar(@Valid @org.springframework.web.bind.annotation.RequestBody OficioSaveDto oficioSaveDto) {
        oficioService.saveOficio(oficioSaveDto);
    }
}
