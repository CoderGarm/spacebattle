package de.yuga.spacebattle.gui.vaadin.research.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.calculator.resource.ResourceControlCalculator;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.StatisticsDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.PlanetIconDisplay;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceAmountDTO;
import de.yuga.spacebattle.gui.vaadin.turn.resource.ResourceElementDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ResearchStatisticsDisplay extends StatisticsDisplay implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchStatisticsDisplay, User>, User> {

    @Nonnull
    private final UserService userService = ViewHelper.getService(UserService.class);

    @Nonnull
    private final PlanetService planetService = ViewHelper.getService(PlanetService.class);

    @Nonnull
    private final Map<Planet, ResourceElementDisplay> planetResearchDisplayComponentMap = new HashMap<>();

    @Nonnull
    private final Map<Planet, PlanetIconDisplay> planetPlanetDisplayComponentMap = new HashMap<>();

    @Nonnull
    private final Map<Planet, HorizontalLayout> planetFullDisplayComponentMap = new HashMap<>();

    @Nonnull
    private final Label sum = new Label();

    @Nonnull
    private final VerticalLayout content = new VerticalLayout();

    public ResearchStatisticsDisplay() {
        addSlide("Research output", content);
        sum.addClassName("research-sum");
        content.add(sum);
        content.addClassName("statsSelectorContainer");
    }

    @Override
    public void clear() {
        planetResearchDisplayComponentMap.clear();
        planetPlanetDisplayComponentMap.clear();
        planetFullDisplayComponentMap.clear();
        removeAll();
        sum.setText("");
    }

    /**
     * Updates the display if called.
     *
     * @param planets the new input data
     */
    public void update(@Nonnull final Set<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets shouldn't be null!");

        final long sumOfAllResearchPoints = planets.stream()
                .filter(p -> p.hasProductionTarget(EResourceType.RESEARCH))
                .map(planet -> ResourceControlCalculator.getTickOutput(planet, EResourceType.RESEARCH))
                .reduce(0L, Long::sum);

        sum.setText("in sum: " + sumOfAllResearchPoints);
        planetPlanetDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));
        planetResearchDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));

        planetFullDisplayComponentMap.keySet().stream()
                .filter(planet -> !planets.contains(planet))
                .map(planetFullDisplayComponentMap::get)
                .forEach(content::remove);

        planetFullDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));

        final int planetDisplayIndex = 0;
        final int resourceDisplayIndex = 1;
        planets.stream().sorted(Comparator.comparingInt(AbstractEntityKey::getId)
        ).forEach(planet -> {
            HorizontalLayout layout = planetFullDisplayComponentMap.get(planet);
            if (layout == null) {
                layout = new HorizontalLayout();
                layout.addClassName("statistics-tight");
                planetFullDisplayComponentMap.put(planet, layout);
                content.add(layout);
            }

            PlanetIconDisplay planetIconDisplay = planetPlanetDisplayComponentMap.get(planet);
            if (planetIconDisplay == null) {
                planetIconDisplay = new PlanetIconDisplay();
                planetPlanetDisplayComponentMap.put(planet, planetIconDisplay);
            }
            planetIconDisplay.update(planet);

            final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
            ResourceElementDisplay resourceElementDisplay = planetResearchDisplayComponentMap.get(planet);
            final long amount = resourceDeposit.getResourceAmountByType(EResourceType.RESEARCH);
            final Long tickOutput = ResourceControlCalculator.getTickOutput(planet, EResourceType.RESEARCH);
            if (resourceElementDisplay == null) {
                resourceElementDisplay = new ResourceElementDisplay(EResolution.PX24);
                planetResearchDisplayComponentMap.put(planet, resourceElementDisplay);
            }
            resourceElementDisplay.setValue(new ResourceAmountDTO(EResourceType.RESEARCH, amount, tickOutput));

            layout.addComponentAtIndex(planetDisplayIndex, planetIconDisplay);
            layout.addComponentAtIndex(resourceDisplayIndex, resourceElementDisplay);
        });
    }

    @Override
    public void setValue(User value) {
        if (value == null) {
            clear();
            return;
        }
        final Set<Planet> allColonizedBy = new HashSet<>(planetService.findAllColonizedBy(value));
        update(allColonizedBy);
    }

    @Nullable
    @Override
    public User getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchStatisticsDisplay, User>> listener) {
        // not necessary
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // not necessary
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        // not necessary
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}