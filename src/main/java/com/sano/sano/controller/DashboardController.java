package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sano.sano.models.Funcionario;
import com.sano.sano.repositorios.FuncionarioRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final FuncionarioRepository funcionarioRepository;

    public DashboardController(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }


    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("oficios", List.of()); // reemplaza con tu servicio
        model.addAttribute("pageTitle", "Dashboard — Sano");
        // Indica qué item del sidebar debe marcarse como activo (ej: "asignar","buscar","cancelar","reportes","salir").
        model.addAttribute("active", ""); // dejar vacío si no hay sección activa
        // URL opcional del logo. Si es null, el fragmento muestra la inicial.
        model.addAttribute("logoUrl", 
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6v4hPPDO9V4BR5Ck-aDigPDL2WqFRpkUIWA&s"
        );
        return "index";
    }

    @GetMapping("/asignar-numero")
    public String asignarNumero(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("oficios", List.of());
        model.addAttribute("active", "asignar");
        model.addAttribute("pageTitle", "Asignar Número de Oficio");
        model.addAttribute("logoUrl", 
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6v4hPPDO9V4BR5Ck-aDigPDL2WqFRpkUIWA&s"
        );

        List<Funcionario> funcionarios;
        try {
            funcionarios = funcionarioRepository.findAll();
        } catch (Exception ex) {
            System.err.println("Warning: could not load funcionarios: " + ex.getMessage());
            funcionarios = List.of();
        }
        model.addAttribute("funcionarios", funcionarios);

        return "asignar";
    }

}
