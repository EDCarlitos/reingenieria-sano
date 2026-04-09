package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sano.dto.OficioSaveDto;
import com.sano.sano.models.Funcionario;
import com.sano.sano.repositorios.FuncionarioRepository;
import com.sano.sano.services.OficioService;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class DashboardController {

    private final FuncionarioRepository funcionarioRepository;
    private final OficioService oficioService;




    
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
        // Agregar DTO vacío para el formulario
        model.addAttribute("oficioSaveDto", new OficioSaveDto());

        return "asignar";
    }


    @PostMapping("/asignar-numero")
    public String guardarOficio(@Valid @ModelAttribute OficioSaveDto oficioSaveDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // Reponer los atributos necesarios para renderizar la vista con errores
            model.addAttribute("fecha", LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
            model.addAttribute("oficios", List.of());
            model.addAttribute("active", "asignar");
            model.addAttribute("pageTitle", "Asignar Número de Oficio");
            model.addAttribute("logoUrl",
                    "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR6v4hPPDO9V4BR5Ck-aDigPDL2WqFRpkUIWA&s");

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

        oficioService.saveOficio(oficioSaveDto);

        return "redirect:/";
    }

}
