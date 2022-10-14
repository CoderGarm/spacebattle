package de.yuga.spacebattle.backend.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EStarSystemCatalogueTest {

    @ParameterizedTest
    @EnumSource(value = EStarSystemCatalogue.class)
    void TestRegex(final EStarSystemCatalogue random) {
        //final EStarSystemCatalogue random = EStarSystemCatalogue.getRandom();
        final String result = random.generateCatalogueName();
        assertNotNull(result);
    }

}