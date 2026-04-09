package com.sano.sano.services;

import org.springframework.stereotype.Service;

import com.sano.dto.OficioSaveDto;

public interface OficioService {
    
    void saveOficio(OficioSaveDto oficioSaveDto);
}