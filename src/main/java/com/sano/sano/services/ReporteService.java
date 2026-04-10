package com.sano.sano.services;

import com.sano.sano.dto.OficioFilterDto;

public interface ReporteService {
    byte[] generarPdf(OficioFilterDto filter);
    byte[] generarExcel(OficioFilterDto filter);
}
