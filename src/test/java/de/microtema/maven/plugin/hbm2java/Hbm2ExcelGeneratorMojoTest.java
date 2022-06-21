package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class Hbm2ExcelGeneratorMojoTest {

    @InjectMocks
    Hbm2JavaGeneratorMojo sut;

    @Mock
    MavenProject project;

    File outputSpecFile;

    @BeforeEach
    void setUp() {

        sut.project = project;
    }

    @Test
    void executeOnNonUpdateFalse() {

        outputSpecFile = new File(sut.outputDir);

        DatabaseConfig databaseConfig = new DatabaseConfig();

        sut.tableNames = Arrays.asList("[tesion GmbH$Customer]", "[Versatel Germany$Customer]");
        sut.host = databaseConfig.getHost();
        sut.userName = databaseConfig.getUserName();
        sut.password = databaseConfig.getPassword();

        sut.execute();

        File[] files = outputSpecFile.listFiles();

        assertNotNull(files);
    }
}
