package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.excel.template.ExcelTemplateService;
import de.microtema.maven.plugin.hbm2java.jdbc.JdbcMetadataService;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class Hbm2JavaGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "table-names", required = true)
    List<String> tableNames = new ArrayList<>();

    @Parameter(property = "host", required = true)
    String host;

    @Parameter(property = "user-name", required = true)
    String userName;

    @Parameter(property = "password", required = true)
    String password;

    @Parameter(property = "output-dir")
    String outputDir = "./Resources/mapping";

    @Parameter(property = "input-file")
    String inputFile;

    @Parameter(property = "field-mapping")
    Properties fieldMapping = new Properties();

    JdbcMetadataService jdbcMetadataService = ClassUtil.createInstance(JdbcMetadataService.class);
    ExcelTemplateService excelTemplateService = ClassUtil.createInstance(ExcelTemplateService.class);

    @SneakyThrows
    public void execute() {

        String appName = Optional.ofNullable(project.getName()).orElse(project.getArtifactId());
        String outputFilePath = outputDir + "/" + project.getArtifactId() + "-mapping.xlsx";

        // Skip maven sub modules
        if (tableNames.isEmpty()) {

            logMessage("Skip maven module: " + appName + " since it does not provide table name!");

            return;
        }

        File file = new File(outputFilePath);
        if (file.exists()) {

            logMessage("Skip maven module: " + appName + " since " + file.getName() + " already exist!");

            return;
        }

        File dir = new File(outputDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        logMessage("Generate Excel from DDL for " + appName + " -> " + outputFilePath);

        DatabaseConfig databaseConfig = new DatabaseConfig();

        databaseConfig.setHost(host);
        databaseConfig.setUserName(userName);
        databaseConfig.setPassword(password);

        List<TableDescription> tableDescriptions = new ArrayList<>();

        for (String tableName : tableNames) {

            List<ColumnDescription> columnDescriptions = jdbcMetadataService.getListColumnDescriptions(databaseConfig, tableName);

            TableDescription tableDescription = new TableDescription();
            tableDescription.setName(tableName);
            tableDescription.setColumns(columnDescriptions);

            tableDescriptions.add(tableDescription);
        }

        ProjectData projectData = new ProjectData();

        projectData.setFieldMapping(getFieldMappings());
        projectData.setOutputFile(outputFilePath);

        excelTemplateService.writeTemplates(tableDescriptions, projectData);
    }

    private Map<String, String> getFieldMappings() {

        Map<String, String> fieldMappings = streamConvert(fieldMapping);

        if (Objects.nonNull(inputFile)) {

            Map<String, String> fieldMappings2 = excelTemplateService.getFieldMappings(inputFile);

            fieldMappings.putAll(fieldMappings2);
        }

        return fieldMappings;
    }

    public Map<String, String> streamConvert(Properties prop) {
        return prop.entrySet().stream().collect(
                Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue()),
                        (prev, next) -> next, HashMap::new
                ));
    }

    void logMessage(String message) {

        Log log = getLog();

        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }
}
