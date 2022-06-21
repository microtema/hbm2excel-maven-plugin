package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ProjectData {

    private String outputDirectory;

    private Map<String, String> fieldMapping;
}
