package com.sano.sano.services;

import java.util.List;

import com.sano.sano.models.AuditLog;

public interface AuditLogService {
    void registrar(String usuario, String accion, String entidad, String detalle);
    List<AuditLog> findAll();
}
