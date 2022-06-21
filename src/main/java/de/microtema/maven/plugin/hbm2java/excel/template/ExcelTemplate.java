package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.MojoFileUtil;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;


public class ExcelTemplate {

    @SneakyThrows
    public void writeOut(List<TableDescription> tableDescriptions, ProjectData projectData) {

        String outputFile = projectData.getOutputFile();
        Map<String, String> fieldMapping = projectData.getFieldMapping();

        Workbook workbook = new XSSFWorkbook();

        for (TableDescription tableDescription : tableDescriptions) {
            writeOutImpl(workbook, tableDescription, fieldMapping);
        }

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        workbook.write(outputStream);
        workbook.close();
    }

    private void writeOutImpl(Workbook workbook, TableDescription tableDescription, Map<String, String> fieldMapping) {

        String sheetName = MojoFileUtil.cleanupTableName(tableDescription.getName());

        Sheet sheet = workbook.createSheet(sheetName);

        writeHeaders(workbook, sheet);
        writeContent(sheet, tableDescription.getColumns(), fieldMapping);
    }

    private void writeHeaders(Workbook workbook, Sheet sheet) {

        CellStyle headerStyle = workbook.createCellStyle();

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 20);
        font.setBold(true);
        headerStyle.setFont(font);

        Row headerRow = sheet.createRow(0);

        for (HeaderType headerType : HeaderType.values()) {

            int cellIndex = headerType.ordinal();

            sheet.setColumnWidth(cellIndex, headerType.getWidth());

            Cell headerCell = headerRow.createCell(cellIndex);

            headerCell.setCellValue(headerType.getName());
            headerCell.setCellStyle(headerStyle);
        }
    }

    private void writeContent(Sheet sheet, List<ColumnDescription> columns, Map<String, String> fieldMapping) {

        for (int index = 0; index < columns.size(); index++) {

            Row row = sheet.createRow(index + 1); // 0: header row

            for (HeaderType headerType : HeaderType.values()) {

                int cellIndex = headerType.ordinal();

                Cell rowCell = row.createCell(cellIndex);

                ColumnDescription columnDescription = columns.get(index);

                headerType.execute(rowCell, columnDescription, fieldMapping);
            }
        }
    }
}
