package de.yuga.spacebattle.gui.vaadin.turn;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.orbitals.PlanetLayout;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JobDisplayMulti extends PlanetLayout<Planet> implements HasValue<AbstractField.ComponentValueChangeEvent<JobDisplayMulti, Set<Job>>, Set<Job>> {

    public JobDisplayMulti() {
        add(new Label("Jobs"));
    }

    @Override
    public void updateStatistics(Planet value) {
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
        if (value != null) {
            value.forEach(job -> {
                final JobDisplay jobDisplay = new JobDisplay(job);
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
