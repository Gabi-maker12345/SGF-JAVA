package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToCsv() throws IOException {
        List<Employee> employees = employeeRepository.findAllWithDepartment();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader("ID", "Nome", "Email", "Cargo", "Salario", "Departamento", "Status"))) {
            for (Employee e : employees) {
                csvPrinter.printRecord(e.getId(), e.getName(), e.getEmail(), e.getPosition(), e.getSalary(), 
                        e.getDepartment() != null ? e.getDepartment().getName() : "N/A", e.getStatus());
            }
            csvPrinter.flush();
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToExcel() throws IOException {
        List<Employee> employees = employeeRepository.findAllWithDepartment();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Funcionarios");
            String[] headers = {"ID", "Nome", "Email", "Cargo", "Salario", "Departamento", "Status"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Employee e : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(e.getName());
                row.createCell(2).setCellValue(e.getEmail());
                row.createCell(3).setCellValue(e.getPosition());
                row.createCell(4).setCellValue(e.getSalary().doubleValue());
                row.createCell(5).setCellValue(e.getDepartment() != null ? e.getDepartment().getName() : "N/A");
                row.createCell(6).setCellValue(e.getStatus().toString());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToPdf() throws IOException {
        List<Employee> employees = employeeRepository.findAllWithDepartment();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            font.setSize(18);
            Paragraph p = new Paragraph("Relatorio de Funcionarios", font);
            p.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(10);

            String[] headers = {"ID", "Nome", "Email", "Cargo", "Salario", "Depto"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                table.addCell(cell);
            }

            for (Employee e : employees) {
                table.addCell(String.valueOf(e.getId()));
                table.addCell(e.getName());
                table.addCell(e.getEmail());
                table.addCell(e.getPosition());
                table.addCell(String.valueOf(e.getSalary()));
                table.addCell(e.getDepartment() != null ? e.getDepartment().getName() : "N/A");
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    // Additional methods for Departments and Payroll can follow similar patterns

    @Transactional(readOnly = true)
    public byte[] exportDepartmentsToExcel() throws IOException {
        List<Department> departments = departmentRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Departamentos");
            String[] headers = {"ID", "Nome", "Descricao"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (Department d : departments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getName());
                row.createCell(2).setCellValue(d.getDescription());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportPayrollToPdf() throws IOException {
        List<Employee> employees = employeeRepository.findAllWithDepartment();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Folha Salarial", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Spacer

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100f);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{1, 3, 3, 2});

            String[] headers = {"ID", "Nome", "Cargo", "Salario"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                table.addCell(cell);
            }

            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            for (Employee e : employees) {
                table.addCell(String.valueOf(e.getId()));
                table.addCell(e.getName());
                table.addCell(e.getPosition());
                table.addCell(String.format("%,.2f", e.getSalary()));
                total = total.add(e.getSalary());
            }

            document.add(table);
            
            Paragraph totalPara = new Paragraph("Total da Folha: " + String.format("%,.2f", total), titleFont);
            totalPara.setAlignment(Paragraph.ALIGN_RIGHT);
            document.add(totalPara);

            document.close();
            return out.toByteArray();
        }
    }
}
