package com.sano.sano.repositorios;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sano.sano.models.AuditLog;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findAllByOrderByFechaDesc();
}
