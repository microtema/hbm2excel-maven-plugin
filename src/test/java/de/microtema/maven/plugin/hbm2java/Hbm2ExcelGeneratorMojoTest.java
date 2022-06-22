package de.microtema.maven.plugin.hbm2java;

import de.microtema.maven.plugin.hbm2java.excel.template.HeaderType;
import de.microtema.maven.plugin.hbm2java.model.DatabaseConfig;
import org.apache.maven.project.MavenProject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Hbm2ExcelGeneratorMojoTest {

    @InjectMocks
    Hbm2JavaGeneratorMojo sut;

    @Mock
    MavenProject project;

    File outputDir;

    @BeforeEach
    void setUp() {

        sut.project = project;
    }

    @Test
    void createAndTestCustomerMapping() throws IOException {

        when(project.getArtifactId()).thenReturn("customer");

        sut.outputDir = "./target/Resources/mapping";

        outputDir = new File(sut.outputDir);

        sut.project = project;

        DatabaseConfig databaseConfig = new DatabaseConfig();

        sut.tableNames = Arrays.asList(
                "[SQL_A1_EDEBIT]",
                "[Versatel Germany$Customer]",
                "[VTB_EC$Customer]",
                "[tesion GmbH$Customer]",
                "[KomTel GmbH$Customer]",
                "[VTW_EC$Customer]");
        sut.host = databaseConfig.getHost();
        sut.userName = databaseConfig.getUserName();
        sut.password = databaseConfig.getPassword();

        sut.execute();

        assertTrue(outputDir.exists());
        File[] files = outputDir.listFiles();
        assertNotNull(files);
        assertEquals(1, files.length);
        assertNotNull(files[0]);
        FileInputStream file = new FileInputStream(files[0]);
        // Load Workbook from file/resource
        Workbook workbook = new XSSFWorkbook(file);
        // Get Sheets from Workbook
        // Assert Sheets number against Table Names +1 (for Commons)
        assertEquals(workbook.getNumberOfSheets(), sut.tableNames.size()+1);

        // For each Sheet assert Sheet Name against Table Name (needs to be cleaned up)
        int index = 0;
        while(index++ < sut.tableNames.size()){
            String cleanedTableName = MojoFileUtil.cleanupTableName(sut.tableNames.get(index-1));
            assertNotNull(workbook.getSheet(cleanedTableName));
            assertEquals(workbook.getSheetAt(index).getSheetName(), cleanedTableName);
        }

        // For First Sheet (Commons) assert Headers against HeaderTypes
        Sheet firstSheet = workbook.getSheetAt(0);
        Row headerRow = firstSheet.getRow(0);

        assertEquals(headerRow.getLastCellNum(), HeaderType.values().length);

        int cellIndex = 0;
        while (cellIndex < headerRow.getLastCellNum()-1) {
            String cellValue = headerRow.getCell(cellIndex).getStringCellValue();
            String headerTypeValue = HeaderType.values()[cellIndex].getName();
            assertEquals(cellValue, headerTypeValue);
            cellIndex++;
        }

    }
}
