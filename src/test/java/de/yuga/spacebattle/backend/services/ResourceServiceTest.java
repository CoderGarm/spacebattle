package de.yuga.spacebattle.backend.services;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceServiceTest {

    @Test
    void getRandomWarshipName() {
        final String result = new ResourceService().getRandomWarshipName();
        assertTrue(StringUtils.isNotBlank(result));
        System.out.println(result);
    }

}