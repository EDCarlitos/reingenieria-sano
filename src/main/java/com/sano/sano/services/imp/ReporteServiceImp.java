package com.sano.sano.services.imp;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sano.sano.dto.OficioFilterDto;
import com.sano.sano.models.Oficio;
import com.sano.sano.services.ReporteService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReporteServiceImp implements ReporteService {

    private final MongoTemplate mongoTemplate;

    private List<Oficio> getOficiosFiltrados(OficioFilterDto filter) {
        List<Criteria> criterias = new ArrayList<>();
        criterias.add(Criteria.where("eliminado").ne(true));

        if (StringUtils.hasText(filter.getPaterno())) {
            criterias.add(Criteria.where("paterno").regex(filter.getPaterno(), "i"));
        }
        if (StringUtils.hasText(filter.getMaterno())) {
            criterias.add(Criteria.where("materno").regex(filter.getMaterno(), "i"));
        }
        if (StringUtils.hasText(filter.getNombres())) {
            criterias.add(Criteria.where("nombres").regex(filter.getNombres(), "i"));
        }
        if (StringUtils.hasText(filter.getAsunto())) {
            criterias.add(Criteria.where("asunto").regex(filter.getAsunto(), "i"));
        }
        if (StringUtils.hasText(filter.getFuncionarioNombre())) {
            criterias.add(Criteria.where("funcionario.nombre").regex(filter.getFuncionarioNombre(), "i"));
        }
        if (filter.getEsRespuesta() != null) {
            criterias.add(Criteria.where("esRespuesta").is(filter.getEsRespuesta()));
        }
        if (filter.getFechaDesde() != null) {
            criterias.add(Criteria.where("fecha").gte(filter.getFechaDesde()));
        }
        if (filter.getFechaHasta() != null) {
            criterias.add(Criteria.where("fecha").lte(filter.getFechaHasta()));
        }

        Query query = new Query();
        if (!criterias.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterias.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, Oficio.class);
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "—";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @Override
    public byte[] generarPdf(OficioFilterDto filter) {
        List<Oficio> oficios = getOficiosFiltrados(filter);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.LETTER.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Reporte de Oficios", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtitle
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
            Paragraph sub = new Paragraph("Generado el " + formatDate(LocalDate.now()) + " — " + oficios.size() + " registro(s)", subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            document.add(sub);

            // Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2f, 2.5f, 1.8f, 1.2f, 1.2f, 1.5f});

            // Header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, java.awt.Color.WHITE);
            java.awt.Color headerBg = new java.awt.Color(37, 99, 235);
            String[] headers = {"N° / ID", "Solicitante", "Asunto", "Funcionario", "Fecha", "Tipo", "Observación"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBorderWidth(0.5f);
                table.addCell(cell);
            }

            // Rows
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
            boolean alt = false;
            java.awt.Color altBg = new java.awt.Color(241, 245, 249);
            for (Oficio o : oficios) {
                java.awt.Color bg = alt ? altBg : java.awt.Color.WHITE;
                alt = !alt;

                String solicitante = (nvl(o.getPaterno()) + " " + nvl(o.getMaterno()) + ", " + nvl(o.getNombres())).trim();
                String funcionario = o.getFuncionario() != null ? o.getFuncionario().getNombre() : "—";
                String tipo = o.isEsRespuesta() ? "En contestación" : "Original";

                String[] values = {
                    nvl(o.getId()),
                    solicitante,
                    nvl(o.getAsunto()),
                    funcionario,
                    formatDate(o.getFecha()),
                    tipo,
                    nvl(o.getObservacion())
                };

                for (String v : values) {
                    PdfPCell cell = new PdfPCell(new Phrase(v, cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(5);
                    cell.setBorderWidth(0.3f);
                    cell.setBorderColor(java.awt.Color.LIGHT_GRAY);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF", e);
        }
    }

    @Override
    public byte[] generarExcel(OficioFilterDto filter) {
        List<Oficio> oficios = getOficiosFiltrados(filter);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Oficios");

            // Title row
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFRow titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Reporte de Oficios");
            titleRow.getCell(0).setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Subtitle row
            XSSFFont subFont = workbook.createFont();
            subFont.setFontHeightInPoints((short) 10);
            subFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            CellStyle subStyle = workbook.createCellStyle();
            subStyle.setFont(subFont);
            subStyle.setAlignment(HorizontalAlignment.CENTER);

            XSSFRow subRow = sheet.createRow(1);
            subRow.createCell(0).setCellValue("Generado el " + formatDate(LocalDate.now()) + " — " + oficios.size() + " registro(s)");
            subRow.getCell(0).setCellStyle(subStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // Header row
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            String[] headers = {"N° / ID", "Solicitante", "Asunto", "Funcionario", "Fecha", "Tipo", "Observación"};
            XSSFRow headerRow = sheet.createRow(3);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);

            int rowIdx = 4;
            for (Oficio o : oficios) {
                XSSFRow row = sheet.createRow(rowIdx++);

                String solicitante = (nvl(o.getPaterno()) + " " + nvl(o.getMaterno()) + ", " + nvl(o.getNombres())).trim();
                String funcionario = o.getFuncionario() != null ? o.getFuncionario().getNombre() : "—";
                String tipo = o.isEsRespuesta() ? "En contestación" : "Original";

                String[] values = {
                    nvl(o.getId()),
                    solicitante,
                    nvl(o.getAsunto()),
                    funcionario,
                    formatDate(o.getFecha()),
                    tipo,
                    nvl(o.getObservacion())
                };

                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i]);
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Ensure minimum width
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}
