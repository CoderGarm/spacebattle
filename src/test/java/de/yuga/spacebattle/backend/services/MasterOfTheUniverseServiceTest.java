package de.yuga.spacebattle.backend.services;

import de.yuga.spacebattle.SpringBootTestProfile;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled("not needed for unit or integration testing")
@SpringBootTestProfile
public class MasterOfTheUniverseServiceTest {

    @Autowired
    private MasterOfTheUniverseService masterOfTheUniverseService;

    @Test
    public void createInitialData() {
        masterOfTheUniverseService.createInitialData();
    }
}
