package de.yuga.spacebattle.backend.services.account;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PasswordConverterTest {

    public static final String[] testString1 = {"password", "c0067d4af4e87f00dbac63b6156828237059172d1bbeac67427345d6a9fda484"};

    private PasswordConverter testObject;

    @BeforeClass
    public void beforeClass() {
        testObject = new PasswordConverter();
    }

    @AfterClass
    public void afterClass() {
        testObject = null;
    }

    @Test
    void testConvertToDatabaseColumn() {
        final String result = testObject.convertToDatabaseColumn(testString1[0]);
        assertEquals(result, testString1[1]);
    }

    @Test
    void testConvertToEntityAttribute() {
        final String result = testObject.convertToEntityAttribute(testString1[1]);
        assertEquals(result, testString1[1]);
    }
}