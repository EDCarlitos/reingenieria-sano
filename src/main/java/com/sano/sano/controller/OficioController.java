package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.sano.sano.dto.OficioSaveDto;
import com.sano.sano.models.Funcionario;
import com.sano.sano.services.AuditLogService;
import com.sano.sano.services.FuncionarioService;
import com.sano.sano.services.OficioService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es"));

    private final FuncionarioService funcionarioService;
    private final OficioService oficioService;
    private final AuditLogService auditLogService;

    @GetMapping
    public String asignarNumero(Model model) {
        populateAsignarModel(model);
        model.addAttribute("oficioSaveDto", new OficioSaveDto());
        return "asignar";
    }

    @PostMapping
    public String guardarOficio(@Valid @ModelAttribute OficioSaveDto oficioSaveDto,
                                BindingResult bindingResult, Model model,
                                Authentication authentication) {

        if (bindingResult.hasErrors()) {
            populateAsignarModel(model);
            return "asignar";
        }

        oficioService.saveOficio(oficioSaveDto);
        auditLogService.registrar(authentication.getName(), "CREAR", "OFICIO",
                "Oficio creado para: " + oficioSaveDto.getNombres() + " " + oficioSaveDto.getPaterno());
        return "redirect:/";
    }

    private void populateAsignarModel(Model model) {
        model.addAttribute("fecha", LocalDate.now().format(DATE_FMT));
        model.addAttribute("oficios", List.of());
        model.addAttribute("active", "asignar");
        model.addAttribute("pageTitle", "Asignar Número de Oficio");

        List<Funcionario> funcionarios;
        try {
            funcionarios = funcionarioService.getActiveFuncionarios();
        } catch (Exception ex) {
            funcionarios = List.of();
        }
        model.addAttribute("funcionarios", funcionarios);
    }
}
