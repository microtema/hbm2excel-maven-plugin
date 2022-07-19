package de.microtema.maven.plugin.hbm2java.excel.template;

import de.microtema.maven.plugin.hbm2java.MojoFileUtil;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum HeaderType {

    SOURCE_FIELD_NAME("Source Field Name", 20_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

            String columnName = columnDescription.getSourceName();

            cell.setCellValue(columnName);
        }
    },
    TARGET_FIELD_NAME("Target Field Name", 10_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

            cell.setCellValue(columnDescription.getName());
        }
    },
    TYPE("Type", 5_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

            String cellValue = MojoFileUtil.resolveFiledType(columnDescription.getJavaType(), columnDescription.getSqlType());

            cell.setCellValue(cellValue);
        }
    },
    PRIMARY_KEY("Primary Key", 6_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

            if (columnDescription.isPrimaryKey()) {
                cell.setCellValue("Yes");
            }
        }
    },
    REQUIRED("Required", 5_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

            boolean required = columnDescription.isRequired();
            List<Boolean> requiredList = columnDescription.getRequiredList();

            Boolean allRequired = null;
            long count = requiredList.stream().filter(Boolean::booleanValue).count();
            if (count == 0) {
                allRequired = Boolean.FALSE;
            } else if (count == requiredList.size()) {
                allRequired = Boolean.TRUE;
            }

            Row row = cell.getRow();

            Sheet sheet = row.getSheet();

            boolean firstSheet = sheet.getSheetName().contains("Common Fields");

            if (firstSheet) {
                if (Objects.isNull(allRequired)) {
                    cell.setCellValue("[" + requiredList.stream().map(it -> it ? "y" : "n").collect(Collectors.joining(",")) + "]");
                } else if (Boolean.TRUE.equals(allRequired)) {
                    cell.setCellValue("Yes");
                }
            } else if (required) {
                cell.setCellValue("Yes");
            }
        }
    },
    SIZE("Size", 3_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {

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
        public void execute(Cell cell, ColumnDescription columnDescription) {
            cell.setCellValue(columnDescription.getDefaultValue());
        }
    },
    COMMENT("Comment", 20_000) {
        @Override
        public void execute(Cell cell, ColumnDescription columnDescription) {
            cell.setCellValue(columnDescription.getDescription());
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

    public abstract void execute(Cell rowCell, ColumnDescription columnDescription);
}
