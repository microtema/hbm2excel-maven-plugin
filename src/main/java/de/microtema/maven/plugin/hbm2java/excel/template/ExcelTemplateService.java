package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.MojoUtil;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ExcelTemplateService {

    private final ExcelTemplate excelTemplate;

    public void writeTemplates(List<TableDescription> tableDescriptions, ProjectData projectData) {

        Set<String> commonColumns = getCommonColumns(tableDescriptions);

        TableDescription commonTableDescription = null;

        if (!commonColumns.isEmpty()) {

            TableDescription tableDescription = tableDescriptions.get(0);
            List<ColumnDescription> commonsColumns = tableDescription.getColumns()
                    .stream()
                    .filter(it -> commonColumns.contains(it.getName()))
                    .collect(Collectors.toList());

            commonTableDescription = new TableDescription();
            commonTableDescription.setName("Common Fields");
            commonTableDescription.setColumns(commonsColumns);
        }

        for (TableDescription tableDescription : tableDescriptions) {

            tableDescription.getColumns().removeIf(it -> commonColumns.contains(it.getName()));
        }

        if (Objects.nonNull(commonTableDescription)) {
            tableDescriptions.add(0, commonTableDescription);
        }

        excelTemplate.writeOut(tableDescriptions, projectData);
    }

    private Set<String> getCommonColumns(List<TableDescription> tableDescriptions) {

        if (tableDescriptions.size() == 0) {
            return Collections.emptySet();
        }

        Map<String, Set<String>> column2Table = new HashMap<>();

        for (TableDescription tableDescription : tableDescriptions) {

            String tableName = tableDescription.getName();
            List<ColumnDescription> columns = tableDescription.getColumns();

            for (ColumnDescription columnDescription : columns) {

                String columnName = columnDescription.getName();

                Set<String> tables = column2Table.get(columnName);

                if (Objects.isNull(tables)) {
                    tables = new HashSet<>();
                    column2Table.put(columnName, tables);
                }

                List<Boolean> requiredList = getAllRequired(columnName, tableDescriptions);
                columnDescription.setRequiredList(requiredList);

                tables.add(tableName);
            }
        }

        column2Table.entrySet().removeIf(it -> it.getValue().size() != tableDescriptions.size());

        return column2Table.keySet();
    }

    private List<Boolean> getAllRequired(String columnName, List<TableDescription> tableDescriptions) {

        List<Boolean> requiredList = new ArrayList<>();

        for (TableDescription tableDescription : tableDescriptions) {
            tableDescription.getColumns().stream()
                    .filter(it -> StringUtils.equalsIgnoreCase(it.getName(), columnName))
                    .forEach(it -> requiredList.add(it.isRequired()));
        }

        return requiredList;
    }

    @SneakyThrows
    public List<TableDescription> getFieldMappings(String inputFile) {

        if (StringUtils.isEmpty(inputFile)) {
            return Collections.emptyList();
        }

        List<TableDescription> tableDescriptions = new ArrayList<>();

        InputStream inputStream = new FileInputStream(inputFile);

        Workbook workbook = new XSSFWorkbook(inputStream);

        int numberOfSheets = workbook.getNumberOfSheets();

        for (int index = 0; index < numberOfSheets; index++) {

            Sheet sheet = workbook.getSheetAt(index);

            TableDescription tableDescription = new TableDescription();

            tableDescription.setName(sheet.getSheetName());
            tableDescription.setIndex(index);
            List<ColumnDescription> columns = new ArrayList<>();
            tableDescription.setColumns(columns);

            tableDescriptions.add(tableDescription);

            for (Row row : sheet) {

                int rowNum = row.getRowNum();

                if (rowNum == 0) {
                    continue;
                }

                Cell sourceNameCell = row.getCell(0);
                Cell targetNameCell = row.getCell(1);
                Cell defaultValueCell = row.getCell(6);
                Cell descriptionCell = row.getCell(7);

                ColumnDescription columnDescription = new ColumnDescription();

                columnDescription.setSourceName(sourceNameCell.getStringCellValue());
                columnDescription.setName(targetNameCell.getStringCellValue());
                columnDescription.setDefaultValue(MojoUtil.getStringCellValue(defaultValueCell));
                columnDescription.setDescription(MojoUtil.getStringCellValue(descriptionCell));

                columns.add(columnDescription);
            }
        }

        return tableDescriptions;
    }
}
