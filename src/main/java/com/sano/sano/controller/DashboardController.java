package com.sano.sano.controller;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.sano.sano.models.Funcionario;
import com.sano.sano.services.FuncionarioService;

@Controller
public class DashboardController {

    private final FuncionarioService funcionarioService;

    public DashboardController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));

        List<Funcionario> funcionarios;
        try {
            funcionarios = funcionarioService.getActiveFuncionarios();
        } catch (Exception ex) {
            funcionarios = List.of();
        }
        model.addAttribute("funcionarios", funcionarios);

        model.addAttribute("pageTitle", "Oficios — Sano");
        model.addAttribute("active", "oficios");
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
