package de.yuga.spacebattle.backend.calculator.resource;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.TestDataProviderUtils;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.ECalculationType;
import de.yuga.spacebattle.backend.enums.EDepositType;
import de.yuga.spacebattle.backend.enums.ERefinementSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.yuga.spacebattle.backend.enums.EEducationType.*;
import static de.yuga.spacebattle.backend.enums.ERefinementSequence.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PopulationControlCalculatorTest {

    private Planet planet = TestDataProviderUtils.planet(1, 1);

    private final List<EducationAmountDTO> result = new ArrayList<>();
    private final Map<ERefinementSequence, Long> educationCapacity = new HashMap<>();

    @BeforeEach
    void prepare() {
        result.clear();
        educationCapacity.clear();

        planet = TestDataProviderUtils.planet(1, 1);
        planet.getResourceDeposit().updateCrew(planet.getResourceDeposit().getCrewRequirement(), ECalculationType.SUBTRACT);
    }

    @ParameterizedTest
    @EnumSource(value = ERefinementSequence.class)
    void doGuidedEducationWithoutHumansPresent(@Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        // create demand
        final ResourceDeposit demand = new ResourceDeposit(EDepositType.DEMAND);
        demand.updateCrewRequirement(refinementSequence.getProduct(), 1);
        // create capacity
        educationCapacity.put(refinementSequence, 1L);

        // test
        PopulationControlCalculator.doGuidedEducation(planet, demand, result, educationCapacity);

        validate(result, refinementSequence, 0);
    }

    @ParameterizedTest
    @EnumSource(value = ERefinementSequence.class)
    void doGuidedEducationWithHumansPresent(@Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        // create population
        planet.getResourceDeposit().updateCrewRequirement(refinementSequence.getEduct(), 1);
        // create demand
        final ResourceDeposit demand = new ResourceDeposit(EDepositType.DEMAND);
        demand.updateCrewRequirement(refinementSequence.getProduct(), 1);
        // create capacity
        educationCapacity.put(refinementSequence, 1L);

        // test
        PopulationControlCalculator.doGuidedEducation(planet, demand, result, educationCapacity);

        validate(result, refinementSequence, 1L);
    }

    @Test
    void doGuidedEducationWithStayingDemand() {
        // create population
        planet.getResourceDeposit().updateCrewRequirement(COLLEGE, 3);
        // create demand
        final ResourceDeposit demand = new ResourceDeposit(EDepositType.DEMAND);
        demand.updateCrewRequirement(COLLEGE, 3);
        demand.updateCrewRequirement(UNIVERSITY, 1);
        // create capacity
        educationCapacity.put(EDUCATION_CIVIL_III, 1L);

        // test
        PopulationControlCalculator.doGuidedEducation(planet, demand, result, educationCapacity);

        validate(result, EDUCATION_CIVIL_III, 0L);
    }

    @Test
    void doGuidedEducationWithPrioritizedDemand() {
        // create population
        planet.getResourceDeposit().updateCrewRequirement(COLLEGE, 1);
        // create demand
        final ResourceDeposit demand = new ResourceDeposit(EDepositType.DEMAND);
        demand.updateCrewRequirement(UNIVERSITY, 1);
        demand.updateCrewRequirement(ENLISTED, 1);
        // create capacity
        educationCapacity.put(EDUCATION_CIVIL_III, 1L);
        educationCapacity.put(EDUCATION_MILITARY_I, 1L);

        // test
        PopulationControlCalculator.doGuidedEducation(planet, demand, result, educationCapacity);

        validate(result, EDUCATION_MILITARY_I, 1L);
        validate(result, EDUCATION_CIVIL_II, 0L);
    }

    @ParameterizedTest
    @EnumSource(value = ERefinementSequence.class)
    void doUnguidedEducationWithoutHumansPresent(@Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        // create capacity
        educationCapacity.put(refinementSequence, 1L);

        // test
        PopulationControlCalculator.doUnguidedEducation(planet, result, educationCapacity);

        validate(result, refinementSequence, 0L);
    }

    @ParameterizedTest
    @EnumSource(value = ERefinementSequence.class)
    void doUnguidedEducationWithHumansPresent(@Nonnull final ERefinementSequence refinementSequence) {
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        // create population
        planet.getResourceDeposit().updateCrewRequirement(refinementSequence.getEduct(), 1L);
        // create capacity
        educationCapacity.put(refinementSequence, 1L);

        // test
        PopulationControlCalculator.doUnguidedEducation(planet, result, educationCapacity);

        validate(result, refinementSequence, 1L);
    }

    private void validate(@Nonnull final List<EducationAmountDTO> result,
                          @Nonnull final ERefinementSequence refinementSequence,
                          final long pupilsAmount) {
        Preconditions.checkNotNull(result, "result must not be empty");
        Preconditions.checkNotNull(refinementSequence, "refinementSequence must not be empty");

        final boolean noneMatch = result.stream().noneMatch(dto -> dto.matches(refinementSequence));
        if (pupilsAmount == 0L && noneMatch) {
            // success
            return;
        }

        result.stream()
                .filter(dto -> dto.matches(refinementSequence))
                .filter(dto -> dto.getHowManyPupils() == pupilsAmount)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Education result doe not match expectation of '" + pupilsAmount + "' '" + refinementSequence.name() + "'."));
    }

    @Test
    void educateWithEmptyDeposit() {
        final EducationAmountDTO input = new EducationAmountDTO(1, EDUCATION_CIVIL_I);

        // test
        PopulationControlCalculator.educate(planet, input);

        assertEquals(0L, planet.getResourceDeposit().getCrewAmountByType(EDUCATION_CIVIL_I.getProduct()));
    }

    @Test
    void educate() {
        planet.getResourceDeposit().setAbsolutePopulation(EDUCATION_CIVIL_I.getEduct(), 1L);
        final EducationAmountDTO input = new EducationAmountDTO(1, EDUCATION_CIVIL_I);

        // test
        PopulationControlCalculator.educate(planet, input);

        assertEquals(1L, planet.getResourceDeposit().getCrewAmountByType(EDUCATION_CIVIL_I.getProduct()));
    }
}
