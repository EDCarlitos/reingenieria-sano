package com.sano.sano.repositorios;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sano.sano.models.Oficio;

public interface OficioRepository extends MongoRepository<Oficio, String> {
    List<Oficio> findByNombresContainingIgnoreCase(String termino);
    Oficio findTopByAnioOrderByNumeroOficioDesc(Integer anio);
}
