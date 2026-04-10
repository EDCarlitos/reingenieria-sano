package com.sano.sano.services.imp;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sano.sano.models.AuditLog;
import com.sano.sano.repositorios.AuditLogRepository;
import com.sano.sano.services.AuditLogService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuditLogServiceImp implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void registrar(String usuario, String accion, String entidad, String detalle) {
        AuditLog log = new AuditLog();
        log.setUsuario(usuario);
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setDetalle(detalle);
        log.setFecha(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByFechaDesc();
    }
}
