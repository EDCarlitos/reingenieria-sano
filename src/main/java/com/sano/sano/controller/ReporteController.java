package com.sano.sano.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.models.Funcionario;
import com.sano.sano.services.FuncionarioService;
import com.sano.sano.services.ReporteService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/generar-reportes")
@AllArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final FuncionarioService funcionarioService;

    @GetMapping
    public String reportes(Model model) {
        model.addAttribute("fecha", LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new java.util.Locale("es"))));
        model.addAttribute("pageTitle", "Generar Reportes — Sano");
        model.addAttribute("active", "reportes");

        List<Funcionario> funcionarios;
        try {
            funcionarios = funcionarioService.getActiveFuncionarios();
        } catch (Exception ex) {
            funcionarios = List.of();
        }
        model.addAttribute("funcionarios", funcionarios);
        return "reportes";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarPdf(@ModelAttribute OficioFilterDto filter) {
        byte[] pdf = reporteService.generarPdf(filter);

        String filename = "reporte_oficios_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> descargarExcel(@ModelAttribute OficioFilterDto filter) {
        byte[] excel = reporteService.generarExcel(filter);

        String filename = "reporte_oficios_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
