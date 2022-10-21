package de.yuga.spacebattle.backend.transformer;

import de.yuga.spacebattle.TestDataProviderUtils;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.yuga.spacebattle.backend.transformer.BuildingCsvTransformer.CSV_SEPARATOR;
import static de.yuga.spacebattle.backend.transformer.CSVTransformer.LN;
import static org.junit.jupiter.api.Assertions.*;

class CsvTransformerTest {

    @Test
    void convertTest() {
        final Building b = TestDataProviderUtils.building();
        final String result = new BuildingCsvTransformer("en").convert(b);
        assertNotNull(result);
        System.out.println(result);
        assertFalse(result.isBlank());
        final String[] splitByLine = result.split(LN);
        assertEquals(2, splitByLine.length);
        assertNotEquals("" + splitByLine[0].charAt(splitByLine[0].length() - 1), CSV_SEPARATOR);
    }

    @Test
    void convertTestCollection() {
        final List<Building> list = List.of(TestDataProviderUtils.building(), TestDataProviderUtils.building());
        final String result = new BuildingCsvTransformer("en").convert(list);
        assertNotNull(result);
        System.out.println(result);
        assertFalse(result.isBlank());
        final String[] splitByLine = result.split(LN);
        assertEquals(3, splitByLine.length);
        assertNotEquals("" + splitByLine[0].charAt(splitByLine[0].length() - 1), CSV_SEPARATOR);
    }
}