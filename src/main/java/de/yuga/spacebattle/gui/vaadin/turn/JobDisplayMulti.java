package de.yuga.spacebattle.gui.vaadin.turn;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class JobDisplayMulti extends PlanetLayout<Planet> implements HasValue<AbstractField.ComponentValueChangeEvent<JobDisplayMulti, Set<Job>>, Set<Job>> {

    @Nonnull
    private final Map<Job, JobDisplay> componentsMap = new HashMap<>();

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    @Nonnull
    private final EResourceType[] jobTypes = {EResourceType.CONSTRUCTION, EResourceType.ORBITALCONSTRUCTION, EResourceType.RESEARCH};

    public JobDisplayMulti() {
        add(new Label("Jobs"));
        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);
    }

    @Override
    public void update(Planet value) {
        Set<Job> jobSet = new HashSet<>();
        if (value != null) {
            jobSet = value.getConstructions().stream()
                    .filter(construction -> !construction.getJobs().isEmpty())
                    .map(Construction::getJobs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }
        setValue(jobSet);
    }

    @Override
    public void setValue(@Nullable final Set<Job> value) {

        removeAll();
        add(new Label("Jobs"));
        componentsMap.clear();
        if (value != null) {
            value.forEach(job -> {
                JobDisplay jobDisplay = new JobDisplay(job);
                componentsMap.put(job, jobDisplay);
                add(jobDisplay);
            });
        }
    }

    @Override
    public Set<Job> getValue() {
        return null;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<JobDisplayMulti, Set<Job>>> listener) {
        return null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
