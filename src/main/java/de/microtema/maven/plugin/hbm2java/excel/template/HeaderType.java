package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.MojoFileUtil;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.util.Map;

public enum HeaderType {

    SOURCE_FIELD_NAME("Source Field Name", 20_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            String columnName = columnDescription.getName();

            String cellValue = fieldMapping.entrySet().stream()
                    .filter(it -> StringUtils.equalsIgnoreCase(it.getValue(), columnName))
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            cell.setCellValue(cellValue);
        }
    },
    TARGET_FIELD_NAME("Target Field Name", 10_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            cell.setCellValue(columnDescription.getName());
        }
    },
    TYPE("Type", 5_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            String cellValue = MojoFileUtil.resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());

            cell.setCellValue(cellValue);
        }
    },
    PRIMARY_KEY("Primary Key", 6_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            if (columnDescription.isPrimaryKey()) {
                cell.setCellValue("Yes");
            }
        }
    },
    REQUIRED("Required", 5_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            if (columnDescription.isRequired()) {
                cell.setCellValue("Yes");
            }
        }
    },
    SIZE("Size", 3_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

            int size = columnDescription.getSize();

            // NOTE: not every type provide/support size attribute
            String cellType = MojoFileUtil.resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());

            switch (cellType) {
                case "String":
                case "Integer":
                case "Long":
                case "BigDecimal":
                    cell.setCellValue(size);
                    break;
            }
        }
    },
    DEFAULT_VALUE("Default Value", 10_000) {
        @Override
        public void execute(Cell rowCell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

        }
    },
    COMMENT("Comment", 20_000) {
        @Override
        public void execute(Cell rowCell, ColumnDescription columnDescription, Map<String, String> fieldMapping) {

        }
    };

    private String name;

    private int width;

    HeaderType(String name, int width) {
        this.name = name;
        this.width = width;
    }

    public String getName() {

        return name;
    }

    public int getWidth() {

        return width;
    }

    public abstract void execute(Cell rowCell, ColumnDescription columnDescription, Map<String, String> fieldMapping);
}
