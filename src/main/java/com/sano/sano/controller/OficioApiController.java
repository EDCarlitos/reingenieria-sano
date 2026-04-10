package com.sano.sano.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sano.sano.dto.OficioDeleteRequestDto;
import com.sano.sano.dto.OficioDto;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.dto.OficioSaveDto;
import com.sano.sano.dto.OficioUpdateDto;
import com.sano.sano.dto.PageResultDto;
import com.sano.sano.models.Usuario;
import com.sano.sano.services.OficioService;
import com.sano.sano.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/oficios")
@AllArgsConstructor
public class OficioApiController {

    private final OficioService oficioService;
    private final UsuarioService usuarioService;

    @GetMapping
    public PageResultDto<OficioDto> buscar(
            @ModelAttribute OficioFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        return oficioService.getOficiosFiltrados(filter, page, size);
    }

    @PostMapping
    public void guardar(@Valid @RequestBody OficioSaveDto oficioSaveDto) {
        oficioService.saveOficio(oficioSaveDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OficioDto> actualizar(@PathVariable String id,
                                                @Valid @RequestBody OficioUpdateDto dto) {
        OficioDto updated = oficioService.updateOficio(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/eliminar")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable String id,
                                                        @Valid @RequestBody OficioDeleteRequestDto dto,
                                                        Authentication authentication) {
        String currentRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("");

        if ("ADMIN".equals(currentRole)) {
            if (!usuarioService.verifyCredentials(authentication.getName(), dto.getPassword())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Contraseña incorrecta"));
            }
        } else {
            if (dto.getUsername() == null || dto.getUsername().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Debe proporcionar un usuario administrador"));
            }
            Usuario admin = usuarioService.findByUsername(dto.getUsername());
            if (admin == null || !"ADMIN".equals(admin.getRol())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "El usuario proporcionado no es administrador"));
            }
            if (!usuarioService.verifyCredentials(dto.getUsername(), dto.getPassword())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Credenciales de administrador inválidas"));
            }
        }

        oficioService.deleteOficio(id, dto.getMotivoEliminacion());
        return ResponseEntity.ok(Map.of("message", "Oficio eliminado correctamente"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
