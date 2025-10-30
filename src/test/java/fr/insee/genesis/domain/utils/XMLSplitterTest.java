package fr.insee.genesis.domain.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XMLSplitterTest {

    @TempDir
    Path tempDir;

    @Test
    void split_splits_in_chunks_and_preserves_headers() throws Exception {
        // Given
        Path inDir = tempDir.resolve("in");
        Path outDir = tempDir.resolve("out");
        Files.createDirectories(inDir);
        Files.createDirectories(outDir);

        Path input = inDir.resolve("input.xml");
        writeFile(input, sampleSurveyXML(5)); // 5 SurveyUnit

        // When : 2 elements by file, we expected 3 files (2+2+1)
        XMLSplitter.split(inDir + File.separator, "input.xml",
                outDir + File.separator, "SurveyUnit", 2);

        // Then
        Path f1 = outDir.resolve("split1.xml");
        Path f2 = outDir.resolve("split2.xml");
        Path f3 = outDir.resolve("split3.xml");

        assertTrue(Files.exists(f1), "split1.xml should exists");
        assertTrue(Files.exists(f2), "split2.xml should exists");
        assertTrue(Files.exists(f3), "split3.xml should exists");

        // Each file is well-formed
        assertParsableXML(f1);
        assertParsableXML(f2);
        assertParsableXML(f3);

        // Comptes d'éléments par fichier
        assertEquals(2, countStartElements(f1, "SurveyUnit"));
        assertEquals(2, countStartElements(f2, "SurveyUnit"));
        assertEquals(1, countStartElements(f3, "SurveyUnit"));

        // Les headers (Root/Header/SurveyUnits) sont présents dans chaque fichier
        assertEquals(1, countStartElements(f1, "Root"));
        assertEquals(1, countStartElements(f1, "Header"));
        assertEquals(1, countStartElements(f1, "SurveyUnits"));

        assertEquals(1, countStartElements(f2, "Root"));
        assertEquals(1, countStartElements(f2, "Header"));
        assertEquals(1, countStartElements(f2, "SurveyUnits"));

        assertEquals(1, countStartElements(f3, "Root"));
        assertEquals(1, countStartElements(f3, "Header"));
        assertEquals(1, countStartElements(f3, "SurveyUnits"));
    }

    @Test
    void split_with_large_chunk_makes_single_file_with_all_items() throws Exception {
        // Given
        Path inDir = tempDir.resolve("in2");
        Path outDir = tempDir.resolve("out2");
        Files.createDirectories(inDir);
        Files.createDirectories(outDir);

        Path input = inDir.resolve("input.xml");
        writeFile(input, sampleSurveyXML(3)); // 3 SurveyUnit

        // When: 10 par fichier -> un seul split
        XMLSplitter.split(inDir + File.separator, "input.xml",
                outDir + File.separator, "SurveyUnit", 10);

        // Then
        Path f1 = outDir.resolve("split1.xml");
        assertTrue(Files.exists(f1), "Expected one file");
        assertParsableXML(f1);
        assertEquals(3, countStartElements(f1, "SurveyUnit"));
        assertFalse(Files.exists(outDir.resolve("split2.xml")), "No second file expected");
    }

    @Test
    void split_when_no_condition_element_creates_no_file() throws Exception {
        // Given: XML without SurveyUnit
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <Root>
                  <Header><Title>No Units</Title></Header>
                  <SurveyUnits></SurveyUnits>
                </Root>
                """;
        Path inDir = tempDir.resolve("in3");
        Path outDir = tempDir.resolve("out3");
        Files.createDirectories(inDir);
        Files.createDirectories(outDir);
        Path input = inDir.resolve("input.xml");
        writeFile(input, xml);

        // When
        XMLSplitter.split(inDir + File.separator, "input.xml",
                outDir + File.separator, "SurveyUnit", 2);

        // Then
        try (var stream = Files.list(outDir)) {
            long count = stream
                    .filter(p -> p.getFileName().startsWith("split"))
                    .count();
            assertEquals(0L, count, "No file named split* expected");
        }
    }

    @Test
    void split_respects_exact_boundary_between_files() throws Exception {
        // Given: 4 unités, chunk de 2 -> exactly 2 files
        Path inDir = tempDir.resolve("in4");
        Path outDir = tempDir.resolve("out4");
        Files.createDirectories(inDir);
        Files.createDirectories(outDir);
        writeFile(inDir.resolve("input.xml"), sampleSurveyXML(4));

        // When
        XMLSplitter.split(inDir + File.separator, "input.xml",
                outDir+ File.separator, "SurveyUnit", 2);

        // Then
        Path f1 = outDir.resolve("split1.xml");
        Path f2 = outDir.resolve("split2.xml");
        assertTrue(Files.exists(f1));
        assertTrue(Files.exists(f2));
        assertFalse(Files.exists(outDir.resolve("split3.xml")), "No file split3 expected");

        assertParsableXML(f1);
        assertParsableXML(f2);
        assertEquals(2, countStartElements(f1, "SurveyUnit"));
        assertEquals(2, countStartElements(f2, "SurveyUnit"));
    }

    // --------- Utilities methods ---------
    private void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private int countStartElements(Path xml, String localName) throws Exception {
        XMLInputFactory xif = XMLInputFactory.newInstance();
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        try (Reader r = Files.newBufferedReader(xml, StandardCharsets.UTF_8)) {
            XMLEventReader xer = xif.createXMLEventReader(r);
            int count = 0;
            while (xer.hasNext()) {
                XMLEvent ev = xer.nextEvent();
                if (ev.isStartElement() && ev.asStartElement().getName().getLocalPart().equals(localName)) {
                    count++;
                }
            }
            xer.close();
            return count;
        }
    }

    private void assertParsableXML(Path xml) throws Exception {
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        dbf.newDocumentBuilder().parse(xml.toFile());
    }

    private String sampleSurveyXML(int n) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Root>
                  <Header>
                    <Title>Test</Title>
                  </Header>
                  <SurveyUnits>
                """);
        for (int i = 1; i <= n; i++) {
            sb.append("""
                    <SurveyUnit id="%d"><Name>U%d</Name></SurveyUnit>
                    """.formatted(i, i));
        }
        sb.append("""
                  </SurveyUnits>
                </Root>
                """);
        return sb.toString();
    }
}
