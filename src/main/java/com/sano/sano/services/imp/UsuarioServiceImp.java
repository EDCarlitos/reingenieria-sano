package com.sano.sano.services.imp;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sano.sano.models.Usuario;
import com.sano.sano.repositorios.UsuarioRepository;
import com.sano.sano.services.UsuarioService;

@Service
public class UsuarioServiceImp implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImp(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Override
    public Usuario findById(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public Usuario create(String username, String password, String rol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario update(String id, String username, String password, String rol) {
        Usuario usuario = findById(id);

        // If username changed, check uniqueness
        if (!usuario.getUsername().equals(username) && usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        usuario.setUsername(username);
        usuario.setRol(rol);

        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public void toggleActivo(String id) {
        Usuario usuario = findById(id);
        usuario.setActivo(!usuario.isActivo());
        usuarioRepository.save(usuario);
    }

    @Override
    public Usuario findByUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null);
    }

    @Override
    public boolean verifyCredentials(String username, String rawPassword) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario desactivado");
        }
        return passwordEncoder.matches(rawPassword, usuario.getPassword());
    }
}
