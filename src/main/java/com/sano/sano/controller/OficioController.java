package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.sano.sano.dto.OficioSaveDto;
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
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/asignar-numero")
@AllArgsConstructor
public class OficioController {

    private final FuncionarioRepository funcionarioRepository;
    private final OficioService oficioService;

    @GetMapping
    public String asignarNumero(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("oficios", List.of());
        model.addAttribute("active", "asignar");
        model.addAttribute("pageTitle", "Asignar Número de Oficio");

        List<Funcionario> funcionarios;
        try {
            funcionarios = funcionarioRepository.findAll();
        } catch (Exception ex) {
            System.err.println("Warning: could not load funcionarios: " + ex.getMessage());
            funcionarios = List.of();
        }
        model.addAttribute("funcionarios", funcionarios);
        model.addAttribute("oficioSaveDto", new OficioSaveDto());

        return "asignar";
    }

    @PostMapping
    public String guardarOficio(@Valid @ModelAttribute OficioSaveDto oficioSaveDto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("fecha", LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
            model.addAttribute("oficios", List.of());
            model.addAttribute("active", "asignar");
            model.addAttribute("pageTitle", "Asignar Número de Oficio");

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
