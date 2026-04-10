package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sano.sano.models.AuditLog;
import com.sano.sano.services.AuditLogService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/logs")
@AllArgsConstructor
public class LogsController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String logs(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("pageTitle", "Registro de Actividad — Sano");
        model.addAttribute("active", "logs");
        return "logs";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<AuditLog> list() {
        return auditLogService.findAll();
    }
}
