package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import lombok.RequiredArgsConstructor;

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

                tables.add(tableName);
            }
        }

        column2Table.entrySet().removeIf(it -> it.getValue().size() != tableDescriptions.size());

        return column2Table.keySet();
    }
}
