package de.microtema.maven.plugin.hbm2java;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class MojoUtil {

    public static String cleanupTableName(String tableName) {

        return tableName.replace("[", "").replace("]", "").replaceAll(File.separator, "");
    }

    public static String getTableName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 1) {
            return parts[0];
        }
        if (parts.length == 2) {
            return parts[1];
        }
        if (parts.length == 3) {
            return parts[2];
        }

        return null;
    }

    public static String getDatabaseName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 3) {
            return parts[1];
        }

        return null;
    }

    public static String getNamePrefix(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length > 1) {
            return parts[0];
        }

        return null;
    }

    public static String resolveFiledType(String javaType, String sqlType) {

        switch (javaType) {
            case "java.sql.Timestamp":
                return LocalDateTime.class.getSimpleName();
            case "java.math.BigDecimal":
                return BigDecimal.class.getSimpleName();
            case "java.lang.Integer":
                return int.class.getSimpleName();
            case "java.lang.String":
                return String.class.getSimpleName();
            case "java.lang.Boolean":
            case "java.lang.Short":
                return boolean.class.getSimpleName();
            default:
                return resolveFiledTypeFromSQlType(sqlType);
        }
    }

    private static String resolveFiledTypeFromSQlType(String sqlType) {

        switch (sqlType) {
            case "timestamp":
                return LocalDateTime.class.getSimpleName();
            case "image":
                return byte[].class.getSimpleName();
            case "bigint":
                return Long.class.getSimpleName();
            default:
                return sqlType;
        }
    }

    public static String getStringCellValue(Cell cell) {

        if (Objects.isNull(cell)) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return cell.getStringCellValue();
        }
    }

    public static String getHostName(String host, String tableNameRaw) {

        String databaseName = getDatabaseName(tableNameRaw);

        if (StringUtils.isEmpty(databaseName)) {
            return host;
        }

        if (StringUtils.contains(host, "sqlserver")) {
            return host + ";databaseName=" + databaseName;
        }

        return host;
    }

    public static String getJdbcDriver(String host) {

        if (StringUtils.contains(host, "sqlserver")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (StringUtils.contains(host, "oracle")) {
            return "oracle.jdbc.OracleDriver";
        }

        throw new IllegalStateException("Unable to identify the JdbcDriver for host: " + host);
    }

    public static String getUserName(String host, String tableNameRaw, String userName) {

        String databaseName = getDatabaseName(tableNameRaw);

        if (StringUtils.isEmpty(databaseName)) {
            return userName;
        }

        if (StringUtils.contains(host, "oracle")) {
            return databaseName;
        }

        return userName;
    }
}
