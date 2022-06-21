package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.Map;

@Data
public class ProjectData {

    private String outputFile;

    private Map<String, String> fieldMapping;
}
