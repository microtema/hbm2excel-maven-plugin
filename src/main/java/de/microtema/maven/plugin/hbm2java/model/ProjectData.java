package de.microtema.maven.plugin.hbm2java.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectData {

    private String outputFile;

    private List<TableDescription> mergeTableDescriptions = new ArrayList<>();
}
