package com.sano.sano.services;

import java.util.List;

import com.sano.sano.models.Usuario;

public interface UsuarioService {
    List<Usuario> findAll();
    Usuario findById(String id);
    Usuario findByUsername(String username);
    Usuario create(String username, String password, String rol);
    Usuario update(String id, String username, String password, String rol);
    void toggleActivo(String id);
    boolean verifyCredentials(String username, String rawPassword);
}
