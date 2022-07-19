package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.excel.template.ExcelTemplateService;
import de.microtema.maven.plugin.hbm2java.jdbc.JdbcMetadataService;
import de.microtema.maven.plugin.hbm2java.model.ColumnDescription;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import de.microtema.maven.plugin.hbm2java.model.ProjectData;
import de.microtema.maven.plugin.hbm2java.model.TableDescription;
import de.microtema.model.converter.util.ClassUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        List<TableDescription> mergeTableDescriptions = Collections.emptyList();

        File file = new File(outputFilePath);
        if (file.exists()) {

            mergeTableDescriptions = excelTemplateService.getFieldMappings(outputFilePath);
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

            tableName = StringUtils.trim(tableName);

            tableName = MojoFileUtil.getTableName(tableName);

            logMessage("tableName: " + tableName);

            List<ColumnDescription> columnDescriptions = jdbcMetadataService.getListColumnDescriptions(databaseConfig, tableName);

            TableDescription tableDescription = new TableDescription();
            tableDescription.setName(tableName);
            tableDescription.setColumns(columnDescriptions);

            tableDescriptions.add(tableDescription);
        }

        ProjectData projectData = new ProjectData();

        projectData.setOutputFile(outputFilePath);
        projectData.setMergeTableDescriptions(mergeTableDescriptions);

        excelTemplateService.writeTemplates(tableDescriptions, projectData);
    }

    void logMessage(String message) {

        Log log = getLog();

        log.info("+----------------------------------+");
        log.info(message);
        log.info("+----------------------------------+");
    }
}
