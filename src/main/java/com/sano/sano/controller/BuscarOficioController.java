package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sano.sano.models.Funcionario;
import com.sano.sano.services.FuncionarioService;

@Controller
@RequestMapping("/buscar-oficio")
public class BuscarOficioController {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es"));

    private final FuncionarioService funcionarioService;

    public BuscarOficioController(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @GetMapping
    public String buscarOficio(Model model) {
        model.addAttribute("fecha", LocalDate.now().format(DATE_FMT));
        model.addAttribute("pageTitle", "Buscar Oficio");
        model.addAttribute("active", "buscar");
        List<Funcionario> funcionarios = funcionarioService.getAllFuncionarios();
        model.addAttribute("funcionarios", funcionarios);
        return "buscar-oficio";
    }
}
