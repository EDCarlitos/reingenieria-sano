package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

import com.sano.sano.models.Usuario;
import com.sano.sano.services.UsuarioService;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String usuarios(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("pageTitle", "Gestión de Usuarios — Sano");
        model.addAttribute("active", "usuarios");
        return "usuarios";
    }

    // ── API endpoints ──

    @GetMapping("/api")
    @ResponseBody
    public List<Usuario> list() {
        List<Usuario> usuarios = usuarioService.findAll();
        // Never expose passwords to the client
        usuarios.forEach(u -> u.setPassword(null));
        return usuarios;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Usuario getOne(@PathVariable String id) {
        Usuario u = usuarioService.findById(id);
        u.setPassword(null);
        return u;
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            String username = body.getOrDefault("username", "").trim();
            String password = body.getOrDefault("password", "").trim();
            String rol = body.getOrDefault("rol", "EMPLEADO").trim();

            if (username.isEmpty() || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario y contraseña son obligatorios"));
            }
            if (password.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 4 caracteres"));
            }
            if (!rol.equals("ADMIN") && !rol.equals("EMPLEADO")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rol inválido"));
            }

            Usuario created = usuarioService.create(username, password, rol);
            created.setPassword(null);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, String> body) {
        try {
            String username = body.getOrDefault("username", "").trim();
            String password = body.get("password");
            String rol = body.getOrDefault("rol", "EMPLEADO").trim();

            if (username.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario es obligatorio"));
            }
            if (password != null && !password.isBlank() && password.length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "La contraseña debe tener al menos 4 caracteres"));
            }
            if (!rol.equals("ADMIN") && !rol.equals("EMPLEADO")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rol inválido"));
            }

            Usuario updated = usuarioService.update(id, username, password, rol);
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/api/{id}/toggle")
    @ResponseBody
    public ResponseEntity<?> toggleActivo(@PathVariable String id) {
        try {
            usuarioService.toggleActivo(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
