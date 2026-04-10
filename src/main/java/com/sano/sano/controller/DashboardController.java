package com.sano.sano.controller;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sano.sano.dto.OficioDto;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.dto.PageResultDto;
import com.sano.sano.services.OficioService;

@Controller
public class DashboardController {

    private final OficioService oficioService;

    public DashboardController(OficioService oficioService) {
        this.oficioService = oficioService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));

        // Obtener los últimos 10 oficios (sin filtros, página 0, size 20)
        PageResultDto<OficioDto> page = oficioService.getOficiosFiltrados(new OficioFilterDto(), 0, 20);
        List<OficioDto> oficios = page.getContent();
        model.addAttribute("oficios", oficios);

        model.addAttribute("pageTitle", "Dashboard — Sano");
        model.addAttribute("active", "");
        return "index";
    }

    @GetMapping("/sistema-info")
    public String sistemaInfo(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("pageTitle", "Información del Sistema — Sano");
        model.addAttribute("active", "sistema");
        return "sistema-info";
    }

}
