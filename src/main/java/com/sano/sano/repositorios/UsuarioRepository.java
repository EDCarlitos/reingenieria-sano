package com.sano.sano.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sano.sano.models.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);
    List<Usuario> findAllByOrderByUsernameAsc();
    boolean existsByUsername(String username);
}
