package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sano.sano.models.Funcionario;
import com.sano.sano.services.AuditLogService;
import com.sano.sano.services.FuncionarioService;

@Controller
@RequestMapping("/funcionarios")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final AuditLogService auditLogService;

    public FuncionarioController(FuncionarioService funcionarioService, AuditLogService auditLogService) {
        this.funcionarioService = funcionarioService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String funcionarios(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("pageTitle", "Gestión de Funcionarios — Sano");
        model.addAttribute("active", "funcionarios");
        return "funcionarios";
    }

    // ── API endpoints ──

    @GetMapping("/api")
    @ResponseBody
    public List<Funcionario> list() {
        return funcionarioService.getAllFuncionarios();
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Funcionario getOne(@PathVariable String id) {
        return funcionarioService.findById(id);
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Authentication authentication) {
        try {
            String nombre = body.getOrDefault("nombre", "").trim();
            String puesto = body.getOrDefault("puesto", "").trim();

            if (nombre.isEmpty() || puesto.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre y puesto son obligatorios"));
            }

            Funcionario created = funcionarioService.create(nombre, puesto);
            auditLogService.registrar(authentication.getName(), "CREAR", "FUNCIONARIO",
                    "Funcionario creado: " + nombre + " — " + puesto);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, String> body, Authentication authentication) {
        try {
            String nombre = body.getOrDefault("nombre", "").trim();
            String puesto = body.getOrDefault("puesto", "").trim();

            if (nombre.isEmpty() || puesto.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre y puesto son obligatorios"));
            }

            Funcionario updated = funcionarioService.update(id, nombre, puesto);
            auditLogService.registrar(authentication.getName(), "EDITAR", "FUNCIONARIO",
                    "Funcionario editado: " + nombre + " — " + puesto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/api/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleActivo(@PathVariable String id, Authentication authentication) {
        try {
            Funcionario f = funcionarioService.findById(id);
            String estado = f.isActivo() ? "desactivado" : "reactivado";
            funcionarioService.toggleActivo(id);
            auditLogService.registrar(authentication.getName(), "EDITAR", "FUNCIONARIO",
                    "Funcionario " + estado + ": " + f.getNombre());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
