package de.yuga.spacebattle.backend.services.researches;

import de.yuga.spacebattle.SpringBootTestProfile;
import de.yuga.spacebattle.rest.dto.researches.ResearchTree;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("not needed for unit or integration testing")
@SpringBootTestProfile
class ResearchServiceTest {

    @Autowired
    private ResearchService researchService;

    @Test
    void testFindAllAsTuple() {
        final List<de.yuga.spacebattle.backend.dto.research.ResearchTreeElement> allAsTuple = researchService.findAllAsTuple();
        assertNotNull(allAsTuple);
        assertFalse(allAsTuple.isEmpty());
    }

    @Test
    void testGetResearchTree() {
        final ResearchTree researchTree = researchService.getResearchTree();
        assertNotNull(researchTree);
        assertFalse(researchTree.getTreeElements().isEmpty());
        assertFalse(researchTree.getResearches().isEmpty());
    }
}
