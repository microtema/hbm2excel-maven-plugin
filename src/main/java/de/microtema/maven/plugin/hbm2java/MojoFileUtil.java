package de.microtema.maven.plugin.hbm2java;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MojoFileUtil {

    public static String cleanupTableName(String tableName) {

        return tableName.replace("[", "").replace("]", "").replaceAll(File.separator, "");
    }

    public static String getTableName(String tableName) {

        String[] parts = tableName.split("\\:");

        if (parts.length == 2) {
            tableName = parts[1];
        }

        return tableName;
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
            default:
                return sqlType;
        }
    }
}
