package de.yuga.spacebattle.backend.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class EGameUserRoleTest {

    @Test
    void testDelimiter() {
        final boolean delimiterPresent = Arrays.stream(EGameUserRole.values()).anyMatch(role -> role.getName().contains("|"));
        assertFalse(delimiterPresent, "The delimiter must not be present in a roles name.");
    }
}
