package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.MojoFileUtil;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;


public class ExcelTemplate {

    @SneakyThrows
    public void writeOut(List<TableDescription> tableDescriptions, ProjectData projectData) {

        String outputFile = projectData.getOutputFile();
        List<TableDescription> mergeTableDescriptions = projectData.getMergeTableDescriptions();

        Workbook workbook = new XSSFWorkbook();

        for (int index = 0; index < tableDescriptions.size(); index++) {

            TableDescription tableDescription = tableDescriptions.get(index);

            mergeTableDescription(tableDescription, mergeTableDescriptions, index);

            writeOutImpl(workbook, tableDescription);
        }

        FileOutputStream outputStream = new FileOutputStream(outputFile);
        workbook.write(outputStream);
        workbook.close();
    }

    private void mergeTableDescription(TableDescription tableDescription, List<TableDescription> mergeTableDescriptions, int currentIndex) {

        TableDescription sourceTableDescription = mergeTableDescriptions.stream().filter(it -> it.getIndex() == currentIndex).findFirst().orElse(null);

        if (Objects.isNull(sourceTableDescription)) {
            return;
        }

        List<ColumnDescription> sourceColumns = sourceTableDescription.getColumns();
        List<ColumnDescription> targetColumns = tableDescription.getColumns();

        for (ColumnDescription targetColumnDescription : targetColumns) {

            ColumnDescription sourceColumnDescription = findColumnDescription(sourceColumns, targetColumnDescription.getName());
            mergeColumnDescription(targetColumnDescription, sourceColumnDescription);
        }
    }

    private void mergeColumnDescription(ColumnDescription targetColumnDescription, ColumnDescription sourceColumnDescription) {

        if (Objects.isNull(sourceColumnDescription)) {
            return;
        }

        targetColumnDescription.setSourceName(sourceColumnDescription.getSourceName());
        targetColumnDescription.setDefaultValue(sourceColumnDescription.getDefaultValue());
        targetColumnDescription.setDescription(sourceColumnDescription.getDescription());
    }

    private ColumnDescription findColumnDescription(List<ColumnDescription> sourceColumns, String name) {

        return sourceColumns.stream()
                .filter(it -> StringUtils.equalsIgnoreCase(it.getName(), name))
                .findFirst().orElse(null);
    }

    private void writeOutImpl(Workbook workbook, TableDescription tableDescription) {

        List<ColumnDescription> columns = tableDescription.getColumns();

        int columnSize = CollectionUtils.size(tableDescription.getColumns());

        String sheetName = MojoFileUtil.cleanupTableName(columnSize + " " + tableDescription.getName());

        Sheet sheet = workbook.createSheet(sheetName);

        writeHeaders(workbook, sheet);

        writerColumnFilter(sheet);

        writeContent(sheet, columns);

        createConditionalFormatting(sheet, columns.size());
    }

    private void writeHeaders(Workbook workbook, Sheet sheet) {

        CellStyle headerStyle = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setFontName("Calibri");
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

    private void writerColumnFilter(Sheet sheet) {

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, HeaderType.values().length - 1));
        sheet.createFreezePane(0, 1);
    }

    private void writeContent(Sheet sheet, List<ColumnDescription> columns) {

        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();

        Font font = sheet.getWorkbook().createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 18);

        cellStyle.setFont(font);

        for (int index = 0; index < columns.size(); index++) {

            Row row = sheet.createRow(index + 1); // 0: header row

            for (HeaderType headerType : HeaderType.values()) {

                int cellIndex = headerType.ordinal();

                Cell rowCell = row.createCell(cellIndex);

                rowCell.setCellStyle(cellStyle);

                ColumnDescription columnDescription = columns.get(index);

                headerType.execute(rowCell, columnDescription);
            }
        }
    }

    private void createConditionalFormatting(Sheet sheet, int rowSize) {

        SheetConditionalFormatting conditionalFormatting = sheet.getSheetConditionalFormatting();

        // Header formatting
        ConditionalFormattingRule formattingRule = conditionalFormatting.createConditionalFormattingRule("MOD(ROW() - 1, 1) = 0");

        PatternFormatting patternFormatting = formattingRule.createPatternFormatting();
        patternFormatting.setFillBackgroundColor(IndexedColors.BLUE_GREY.index);
        patternFormatting.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        FontFormatting fontFormatting = formattingRule.createFontFormatting();
        fontFormatting.setFontColorIndex(IndexedColors.WHITE.index);

        CellRangeAddress[] regions = {CellRangeAddress.valueOf("A1:H1")};
        conditionalFormatting.addConditionalFormatting(regions, formattingRule);

        // rows formatting
        formattingRule = conditionalFormatting.createConditionalFormattingRule("MOD(ROW() - 1, 2) = 0");

        patternFormatting = formattingRule.createPatternFormatting();
        patternFormatting.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.index);
        patternFormatting.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

        CellRangeAddress[] regions1 = {CellRangeAddress.valueOf("A1:H" + (rowSize + 2))};
        conditionalFormatting.addConditionalFormatting(regions1, formattingRule);
    }
}
