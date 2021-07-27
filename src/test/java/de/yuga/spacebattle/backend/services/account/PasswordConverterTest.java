package de.yuga.spacebattle.backend.services.account;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordConverterTest {

    public static final String[] testString1 = {"password", "e9a75486736a550af4fea861e2378305c4a555a05094dee1dca2f68afea49cc3a50e8de6ea131ea521311f4d6fb054a146e8282f8e35ff2e6368c1a62e909716"};

    private PasswordConverter testObject;

    @BeforeEach
    public void beforeClass() {
        testObject = new PasswordConverter();
    }

    @AfterEach
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
