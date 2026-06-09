package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.entity.Department;
import com.SI.Crud.funcionarios_SI.model.entity.Employee;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExportService {

    private static final Locale AO_LOCALE = new Locale("pt", "AO");
    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String GENERATED_AT = "Gerado em " + LocalDateTime.now().format(EXPORT_DATE_FORMAT);

    private static final Color PDF_NAVY = new Color(7, 17, 31);
    private static final Color PDF_PANEL = new Color(15, 23, 42);
    private static final Color PDF_BLUE = new Color(59, 130, 246);
    private static final Color PDF_PURPLE = new Color(124, 58, 237);
    private static final Color PDF_TEXT = new Color(248, 250, 252);
    private static final Color PDF_MUTED = new Color(100, 116, 139);
    private static final Color PDF_BORDER = new Color(226, 232, 240);
    private static final Color PDF_ROW = new Color(248, 250, 252);

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToCsv() throws IOException {
        List<Employee> employees = sortedEmployees(employeeRepository.findAllWithDepartment());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader("ID", "Nome", "Email", "Telefone", "Cargo", "Salario", "Departamento", "Status")
                     .build())) {

            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            for (Employee employee : employees) {
                csvPrinter.printRecord(
                        employee.getId(),
                        safe(employee.getName()),
                        safe(employee.getEmail()),
                        safe(employee.getPhone()),
                        safe(employee.getPosition()),
                        formatMoney(employee.getSalary()),
                        departmentName(employee),
                        employee.getStatus() != null ? employee.getStatus().name() : "N/A"
                );
            }

            csvPrinter.flush();
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToExcel() throws IOException {
        List<Employee> employees = sortedEmployees(employeeRepository.findAllWithDepartment());

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Funcionarios");
            ExportStyles styles = createWorkbookStyles(workbook);
            String[] headers = {"ID", "Funcionario", "Email", "Telefone", "Cargo", "Salario", "Departamento", "Status"};

            int rowIndex = createExcelHeader(sheet, styles, "Relatorio de Funcionarios",
                    "Directorio completo de funcionarios cadastrados no SGF", headers.length - 1);
            rowIndex = createEmployeeMetrics(sheet, styles, rowIndex, employees);
            rowIndex = createExcelTableHeader(sheet, styles, rowIndex, headers);

            for (Employee employee : employees) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, employee.getId(), styles.bodyCenter());
                writeCell(row, 1, safe(employee.getName()), styles.bodyStrong());
                writeCell(row, 2, safe(employee.getEmail()), styles.body());
                writeCell(row, 3, safe(employee.getPhone()), styles.body());
                writeCell(row, 4, safe(employee.getPosition()), styles.body());
                writeCell(row, 5, moneyValue(employee.getSalary()), styles.money());
                writeCell(row, 6, departmentName(employee), styles.badgeBlue());
                writeCell(row, 7, employee.getStatus() != null ? employee.getStatus().name() : "N/A", styles.badgeGreen());
            }

            finishSheet(sheet, headers.length, rowIndex);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportEmployeesToPdf() throws IOException {
        List<Employee> employees = sortedEmployees(employeeRepository.findAllWithDepartment());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 26, 26);
            PdfWriter.getInstance(document, out);
            document.open();

            addPdfHero(document, "Relatorio de Funcionarios",
                    "Directorio completo de funcionarios cadastrados no SGF",
                    employees.size() + " funcionario(s)", formatMoney(totalSalary(employees)));
            addPdfSpacer(document, 8);

            PdfPTable table = new PdfPTable(new float[]{.55f, 2.1f, 2.5f, 1.55f, 1.7f, 1.35f, 1.55f});
            table.setWidthPercentage(100);
            addPdfHeaders(table, "ID", "Funcionario", "Email", "Cargo", "Departamento", "Salario", "Status");

            int index = 0;
            for (Employee employee : employees) {
                Color background = index++ % 2 == 0 ? Color.WHITE : PDF_ROW;
                addPdfCell(table, String.valueOf(employee.getId()), background, Element.ALIGN_CENTER, false);
                addPdfCell(table, safe(employee.getName()), background, Element.ALIGN_LEFT, true);
                addPdfCell(table, safe(employee.getEmail()), background, Element.ALIGN_LEFT, false);
                addPdfCell(table, safe(employee.getPosition()), background, Element.ALIGN_LEFT, false);
                addPdfCell(table, departmentName(employee), background, Element.ALIGN_LEFT, false);
                addPdfCell(table, formatMoney(employee.getSalary()), background, Element.ALIGN_RIGHT, false);
                addPdfCell(table, employee.getStatus() != null ? employee.getStatus().name() : "N/A", background, Element.ALIGN_CENTER, false);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportDepartmentsToExcel() throws IOException {
        List<Department> departments = departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Departamentos");
            ExportStyles styles = createWorkbookStyles(workbook);
            String[] headers = {"ID", "Departamento", "Descricao", "Funcionarios ativos", "Vinculos totais"};

            int rowIndex = createExcelHeader(sheet, styles, "Relatorio de Departamentos",
                    "Mapa estrutural dos departamentos registados no SGF", headers.length - 1);
            rowIndex = createDepartmentMetrics(sheet, styles, rowIndex, departments);
            rowIndex = createExcelTableHeader(sheet, styles, rowIndex, headers);

            for (Department department : departments) {
                Row row = sheet.createRow(rowIndex++);
                writeCell(row, 0, department.getId(), styles.bodyCenter());
                writeCell(row, 1, safe(department.getName()), styles.bodyStrong());
                writeCell(row, 2, safe(department.getDescription(), "Sem descricao"), styles.body());
                writeCell(row, 3, employeeRepository.countByDepartmentIdAndDeletedAtIsNull(department.getId()), styles.badgeGreen());
                writeCell(row, 4, employeeRepository.countByDepartmentId(department.getId()), styles.badgeBlue());
            }

            finishSheet(sheet, headers.length, rowIndex);
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportPayrollToPdf() throws IOException {
        List<Employee> employees = sortedEmployees(employeeRepository.findAllWithDepartment());
        BigDecimal total = totalSalary(employees);
        BigDecimal average = employees.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(employees.size()), 0, java.math.RoundingMode.HALF_UP);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 32, 32, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            addPdfHero(document, "Folha Salarial",
                    "Resumo financeiro mensal dos funcionarios cadastrados",
                    "Total: " + formatMoney(total), "Media: " + formatMoney(average));
            addPdfSpacer(document, 8);

            PdfPTable table = new PdfPTable(new float[]{.7f, 2.3f, 2f, 1.55f});
            table.setWidthPercentage(100);
            addPdfHeaders(table, "ID", "Funcionario", "Cargo", "Salario");

            int index = 0;
            for (Employee employee : employees) {
                Color background = index++ % 2 == 0 ? Color.WHITE : PDF_ROW;
                addPdfCell(table, String.valueOf(employee.getId()), background, Element.ALIGN_CENTER, false);
                addPdfCell(table, safe(employee.getName()), background, Element.ALIGN_LEFT, true);
                addPdfCell(table, safe(employee.getPosition()), background, Element.ALIGN_LEFT, false);
                addPdfCell(table, formatMoney(employee.getSalary()), background, Element.ALIGN_RIGHT, false);
            }

            document.add(table);
            addPdfSpacer(document, 12);
            addPdfTotal(document, "Total da folha salarial", formatMoney(total));
            document.close();
            return out.toByteArray();
        }
    }

    private int createExcelHeader(Sheet sheet, ExportStyles styles, String title, String subtitle, int lastColumn) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(32);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SGF | " + title);
        titleCell.setCellStyle(styles.title());
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumn));

        Row subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(24);
        Cell subtitleCell = subtitleRow.createCell(0);
        subtitleCell.setCellValue(subtitle + " - " + GENERATED_AT);
        subtitleCell.setCellStyle(styles.subtitle());
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, lastColumn));

        return 3;
    }

    private int createEmployeeMetrics(Sheet sheet, ExportStyles styles, int rowIndex, List<Employee> employees) {
        Row row = sheet.createRow(rowIndex++);
        writeMetric(row, 0, "Funcionarios", String.valueOf(employees.size()), styles);
        writeMetric(row, 2, "Folha total", formatMoney(totalSalary(employees)), styles);
        writeMetric(row, 4, "Departamentos", String.valueOf(employees.stream().map(this::departmentName).distinct().count()), styles);
        return rowIndex + 1;
    }

    private int createDepartmentMetrics(Sheet sheet, ExportStyles styles, int rowIndex, List<Department> departments) {
        long activeEmployees = departments.stream()
                .mapToLong(department -> employeeRepository.countByDepartmentIdAndDeletedAtIsNull(department.getId()))
                .sum();
        Row row = sheet.createRow(rowIndex++);
        writeMetric(row, 0, "Departamentos", String.valueOf(departments.size()), styles);
        writeMetric(row, 2, "Funcionarios ativos", String.valueOf(activeEmployees), styles);
        writeMetric(row, 4, "Fonte", "SGF", styles);
        return rowIndex + 1;
    }

    private int createExcelTableHeader(Sheet sheet, ExportStyles styles, int rowIndex, String[] headers) {
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.setHeightInPoints(24);
        for (int index = 0; index < headers.length; index++) {
            writeCell(headerRow, index, headers[index], styles.header());
        }
        return rowIndex;
    }

    private void finishSheet(Sheet sheet, int columns, int lastRow) {
        sheet.createFreezePane(0, 6);
        sheet.setAutoFilter(new CellRangeAddress(5, Math.max(5, lastRow - 1), 0, columns - 1));
        for (int index = 0; index < columns; index++) {
            sheet.autoSizeColumn(index);
            int currentWidth = sheet.getColumnWidth(index);
            sheet.setColumnWidth(index, Math.min(Math.max(currentWidth + 700, 3400), 11000));
        }
    }

    private void writeMetric(Row row, int startColumn, String label, String value, ExportStyles styles) {
        writeCell(row, startColumn, label, styles.metricLabel());
        writeCell(row, startColumn + 1, value, styles.metricValue());
    }

    private void writeCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
        cell.setCellStyle(style);
    }

    private ExportStyles createWorkbookStyles(XSSFWorkbook workbook) {
        XSSFCellStyle title = workbook.createCellStyle();
        title.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        title.setFillForegroundColor(color(7, 17, 31));
        title.setAlignment(HorizontalAlignment.LEFT);
        title.setVerticalAlignment(VerticalAlignment.CENTER);
        title.setFont(font(workbook, IndexedColors.WHITE.getIndex(), 18, true));

        XSSFCellStyle subtitle = workbook.createCellStyle();
        subtitle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        subtitle.setFillForegroundColor(color(15, 23, 42));
        subtitle.setAlignment(HorizontalAlignment.LEFT);
        subtitle.setVerticalAlignment(VerticalAlignment.CENTER);
        subtitle.setFont(font(workbook, IndexedColors.GREY_25_PERCENT.getIndex(), 10, false));

        XSSFCellStyle header = workbook.createCellStyle();
        header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        header.setFillForegroundColor(color(59, 130, 246));
        header.setAlignment(HorizontalAlignment.LEFT);
        header.setVerticalAlignment(VerticalAlignment.CENTER);
        header.setFont(font(workbook, IndexedColors.WHITE.getIndex(), 10, true));
        addThinBorder(header, IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());

        XSSFCellStyle body = workbook.createCellStyle();
        body.setVerticalAlignment(VerticalAlignment.CENTER);
        body.setWrapText(true);
        body.setFont(font(workbook, IndexedColors.BLUE_GREY.getIndex(), 10, false));
        addThinBorder(body, IndexedColors.GREY_25_PERCENT.getIndex());

        XSSFCellStyle bodyStrong = workbook.createCellStyle();
        bodyStrong.cloneStyleFrom(body);
        bodyStrong.setFont(font(workbook, IndexedColors.DARK_BLUE.getIndex(), 10, true));

        XSSFCellStyle bodyCenter = workbook.createCellStyle();
        bodyCenter.cloneStyleFrom(body);
        bodyCenter.setAlignment(HorizontalAlignment.CENTER);

        XSSFCellStyle money = workbook.createCellStyle();
        money.cloneStyleFrom(body);
        money.setAlignment(HorizontalAlignment.RIGHT);
        money.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0 \"Kz\""));
        money.setFont(font(workbook, IndexedColors.DARK_BLUE.getIndex(), 10, true));

        XSSFCellStyle badgeBlue = badgeStyle(workbook, color(219, 234, 254), IndexedColors.BLUE.getIndex());
        XSSFCellStyle badgeGreen = badgeStyle(workbook, color(220, 252, 231), IndexedColors.GREEN.getIndex());

        XSSFCellStyle metricLabel = workbook.createCellStyle();
        metricLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        metricLabel.setFillForegroundColor(color(226, 232, 240));
        metricLabel.setFont(font(workbook, IndexedColors.BLUE_GREY.getIndex(), 9, true));
        metricLabel.setAlignment(HorizontalAlignment.CENTER);
        addThinBorder(metricLabel, IndexedColors.WHITE.getIndex());

        XSSFCellStyle metricValue = workbook.createCellStyle();
        metricValue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        metricValue.setFillForegroundColor(color(238, 242, 255));
        metricValue.setFont(font(workbook, IndexedColors.DARK_BLUE.getIndex(), 11, true));
        metricValue.setAlignment(HorizontalAlignment.CENTER);
        addThinBorder(metricValue, IndexedColors.WHITE.getIndex());

        return new ExportStyles(title, subtitle, header, body, bodyStrong, bodyCenter, money, badgeBlue, badgeGreen, metricLabel, metricValue);
    }

    private XSSFCellStyle badgeStyle(XSSFWorkbook workbook, XSSFColor background, short fontColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(background);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFont(font(workbook, fontColor, 10, true));
        addThinBorder(style, IndexedColors.WHITE.getIndex());
        return style;
    }

    private org.apache.poi.ss.usermodel.Font font(Workbook workbook, short color, int size, boolean bold) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setColor(color);
        font.setFontHeightInPoints((short) size);
        font.setBold(bold);
        font.setFontName("Inter");
        return font;
    }

    private XSSFColor color(int red, int green, int blue) {
        return new XSSFColor(new byte[]{(byte) red, (byte) green, (byte) blue}, new DefaultIndexedColorMap());
    }

    private void addThinBorder(CellStyle style, short color) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setTopBorderColor(color);
        style.setRightBorderColor(color);
        style.setBottomBorderColor(color);
        style.setLeftBorderColor(color);
    }

    private void addPdfHero(Document document, String title, String subtitle, String metricOne, String metricTwo) throws IOException {
        PdfPTable hero = new PdfPTable(new float[]{3.4f, 1.3f, 1.3f});
        hero.setWidthPercentage(100);
        hero.setSpacingAfter(8);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setBackgroundColor(PDF_NAVY);
        titleCell.setPadding(14);
        titleCell.addElement(pdfParagraph("SGF", 10, true, PDF_BLUE, Element.ALIGN_LEFT));
        titleCell.addElement(pdfParagraph(title, 20, true, PDF_TEXT, Element.ALIGN_LEFT));
        titleCell.addElement(pdfParagraph(subtitle + "\n" + GENERATED_AT, 9, false, new Color(203, 213, 225), Element.ALIGN_LEFT));
        hero.addCell(titleCell);

        hero.addCell(pdfMetricCell(metricOne, "Indicador"));
        hero.addCell(pdfMetricCell(metricTwo, "Resumo"));
        document.add(hero);
    }

    private PdfPCell pdfMetricCell(String value, String label) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(PDF_PANEL);
        cell.setPadding(12);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.addElement(pdfParagraph(value, 13, true, PDF_TEXT, Element.ALIGN_CENTER));
        cell.addElement(pdfParagraph(label, 8, false, new Color(203, 213, 225), Element.ALIGN_CENTER));
        return cell;
    }

    private Paragraph pdfParagraph(String text, int size, boolean bold, Color color, int alignment) {
        Font font = FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size);
        font.setColor(color);
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setAlignment(alignment);
        paragraph.setLeading(size + 3);
        return paragraph;
    }

    private void addPdfHeaders(PdfPTable table, String... headers) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        font.setColor(Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(PDF_BLUE);
            cell.setBorderColor(PDF_BLUE);
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private void addPdfCell(PdfPTable table, String value, Color background, int alignment, boolean strong) {
        Font font = FontFactory.getFont(strong ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 8);
        font.setColor(strong ? PDF_NAVY : PDF_MUTED);
        PdfPCell cell = new PdfPCell(new Phrase(safe(value), font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(PDF_BORDER);
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addPdfTotal(Document document, String label, String value) throws IOException {
        PdfPTable total = new PdfPTable(new float[]{2.5f, 1.2f});
        total.setWidthPercentage(100);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, pdfFont(11, true, PDF_TEXT)));
        labelCell.setPadding(10);
        labelCell.setBackgroundColor(PDF_PURPLE);
        labelCell.setBorderColor(PDF_PURPLE);
        total.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, pdfFont(12, true, PDF_TEXT)));
        valueCell.setPadding(10);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(PDF_BLUE);
        valueCell.setBorderColor(PDF_BLUE);
        total.addCell(valueCell);

        document.add(total);
    }

    private Font pdfFont(int size, boolean bold, Color color) {
        Font font = FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, size);
        font.setColor(color);
        return font;
    }

    private void addPdfSpacer(Document document, int height) throws IOException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setLeading(height);
        document.add(spacer);
    }

    private List<Employee> sortedEmployees(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator
                        .comparing(this::departmentName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Employee::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private BigDecimal totalSalary(List<Employee> employees) {
        return employees.stream()
                .map(Employee::getSalary)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double moneyValue(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    private String formatMoney(BigDecimal value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(AO_LOCALE);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return formatter.format(value == null ? BigDecimal.ZERO : value) + " Kz";
    }

    private String departmentName(Employee employee) {
        return employee.getDepartment() != null ? safe(employee.getDepartment().getName()) : "N/A";
    }

    private String safe(String value) {
        return safe(value, "N/A");
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record ExportStyles(
            CellStyle title,
            CellStyle subtitle,
            CellStyle header,
            CellStyle body,
            CellStyle bodyStrong,
            CellStyle bodyCenter,
            CellStyle money,
            CellStyle badgeBlue,
            CellStyle badgeGreen,
            CellStyle metricLabel,
            CellStyle metricValue
    ) {
    }
}
