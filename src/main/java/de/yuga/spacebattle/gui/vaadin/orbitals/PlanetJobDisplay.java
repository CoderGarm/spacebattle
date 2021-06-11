package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.turn.JobDisplayMulti;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanetJobDisplay extends PlanetLayout<Planet> {

    @Nonnull
    private final Binder<Planet> binderPlanet = new Binder<>(Planet.class);

    @Nonnull
    private final Binder<Set<Job>> binderJobs = new Binder<>();

    public PlanetJobDisplay() {

        binderPlanet.forField(getPlanetResourceDisplay()).bind(planet -> planet, null);

        Label l = new Label("Jobs");
        add(l);

        JobDisplayMulti jobDisplayMulti = new JobDisplayMulti();
        binderJobs.forField(jobDisplayMulti).bind(jobs -> jobs, null);
        add(jobDisplayMulti);
    }

    @Override
    public void updateStatistics(@Nullable final Planet planet) {
        binderPlanet.readBean(planet);
        Set<Job> jobSet = new HashSet<>();
        if (planet != null) {
            jobSet = planet.getConstructions().stream()
                    .filter(construction -> !construction.getJobs().isEmpty())
                    .map(Construction::getJobs)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
        }
        binderJobs.readBean(jobSet);
    }

}
