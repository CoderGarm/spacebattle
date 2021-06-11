package de.yuga.spacebattle.gui.vaadin.research.details;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.ResourceDeposit;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.enums.EResolution;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.gui.vaadin.ViewHelper;
import de.yuga.spacebattle.gui.vaadin.misc.details.EResourceAmountDTO;
import de.yuga.spacebattle.gui.vaadin.misc.details.PlanetIconDisplay;
import de.yuga.spacebattle.gui.vaadin.misc.details.ResourceElementDisplay;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ResearchOutputDisplay extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ResearchOutputDisplay, User>, User> {

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
    private final HorizontalLayout titleLayout = new HorizontalLayout();


    public ResearchOutputDisplay() {
        final Label title = new Label("Research output");
        titleLayout.add(title, sum);
        add(titleLayout);
    }

    @Nullable
    private BigDecimal getTickOutput(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        final Construction construction = getConstruction(planet);

        if (construction != null) {
            final BigDecimal resourceFactorByType = planet.getResourceFactors().getResourceAmountByType(EResourceType.RESEARCH);
            return construction.getTickOutput(resourceFactorByType);
        }
        return null;
    }

    @Nullable
    private Construction getConstruction(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return planet.getConstructions().stream()
                .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.RESEARCH)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void clear() {
        planetResearchDisplayComponentMap.clear();
        planetPlanetDisplayComponentMap.clear();
        planetFullDisplayComponentMap.clear();
        removeAll();
        sum.setText("");
        add(titleLayout);
    }

    /**
     * Updates the display if called.
     *
     * @param planets the new input data
     */
    public void update(@Nonnull final Set<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets shouldn't be null!");

        final BigDecimal sumOfAllResearchPoints = planets.stream()
                .map(planet -> planet.getTickOutputForResourceType(EResourceType.RESEARCH))
                .collect(Collectors.toSet())
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sum.setText("in sum: " + sumOfAllResearchPoints);
        planetPlanetDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));
        planetResearchDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));

        planetFullDisplayComponentMap.keySet().stream()
                .filter(planet -> !planets.contains(planet))
                .map(planetFullDisplayComponentMap::get)
                .forEach(this::remove);

        planetFullDisplayComponentMap.keySet().removeIf(planet -> !planets.contains(planet));

        final int planetDisplayIndex = 0;
        final int resourceDisplayIndex = 1;
        planets.stream().sorted(Comparator.comparingInt(AbstractEntityKey::getId)
        ).forEach(planet -> {
            HorizontalLayout layout = planetFullDisplayComponentMap.get(planet);
            if (layout == null) {
                layout = new HorizontalLayout();
                planetFullDisplayComponentMap.put(planet, layout);
                add(layout);
            }

            PlanetIconDisplay planetIconDisplay = planetPlanetDisplayComponentMap.get(planet);
            if (planetIconDisplay == null) {
                planetIconDisplay = new PlanetIconDisplay();
                planetPlanetDisplayComponentMap.put(planet, planetIconDisplay);
            }
            planetIconDisplay.update(planet);

            final ResourceDeposit resourceDeposit = planet.getResourceDeposit();
            final Map<EResourceType, BigDecimal> resources = resourceDeposit.getResources();
            ResourceElementDisplay resourceElementDisplay = planetResearchDisplayComponentMap.get(planet);
            final BigDecimal amount = resources.get(EResourceType.RESEARCH);
            final BigDecimal tickOutput = getTickOutput(planet);
            if (resourceElementDisplay == null) {
                resourceElementDisplay = new ResourceElementDisplay(EResolution.PX32);
                planetResearchDisplayComponentMap.put(planet, resourceElementDisplay);
            }
            resourceElementDisplay.update(new EResourceAmountDTO(EResourceType.RESEARCH, amount, tickOutput));

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
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ResearchOutputDisplay, User>> listener) {
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