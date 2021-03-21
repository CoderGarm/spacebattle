package de.yuga.spacebattle.gui.vaadin.turn;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JobDisplayMulti extends PlanetLayout implements HasValue<AbstractField.ComponentValueChangeEvent<JobDisplayMulti, Set<Job>>, Set<Job>> {

    @Nonnull
    private final Map<Job, JobDisplay> componentsMap = new HashMap<>();

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    public JobDisplayMulti() {
        add(new Label("Jobs"));
        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);
    }

    @Override
    public void setValue(Set<Job> value) {

        removeAll();
        componentsMap.clear(); // todo implement
        add(new Label("Jobs"));
        value.forEach(job -> {
            JobDisplay jobDisplay = new JobDisplay(job);
            componentsMap.put(job, jobDisplay);
            add(jobDisplay);

        });
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
