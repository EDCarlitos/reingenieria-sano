package com.sano.sano.services;


import com.sano.sano.dto.OficioDto;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.dto.OficioSaveDto;
import com.sano.sano.dto.PageResultDto;

public interface OficioService {

    void saveOficio(OficioSaveDto oficioSaveDto);

    PageResultDto<OficioDto> getOficiosFiltrados(OficioFilterDto filter, int page, int size);
}