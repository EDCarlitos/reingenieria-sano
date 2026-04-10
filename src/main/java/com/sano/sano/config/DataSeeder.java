package com.sano.sano.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sano.sano.models.Usuario;
import com.sano.sano.repositorios.UsuarioRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsuarios(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                usuarioRepository.save(new Usuario(null, "admin", passwordEncoder.encode("admin123"), "ADMIN"));
            }
            if (usuarioRepository.findByUsername("empleado").isEmpty()) {
                usuarioRepository.save(new Usuario(null, "empleado", passwordEncoder.encode("empleado123"), "EMPLEADO"));
            }
        };
    }
}
