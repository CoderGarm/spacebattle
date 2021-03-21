package de.yuga.spacebattle.gui.vaadin.orbitals;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.binder.Binder;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.turn.JobDisplayMulti;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanetJobDisplay extends PlanetLayout {

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

    public void update(@Nullable final Planet planet) {
        binderPlanet.readBean(planet);
        Set<Job> jobSet = planet.getConstructions().stream()
                .filter(construction -> construction.getJob() != null)
                .map(Construction::getJob)
                .collect(Collectors.toSet());
        binderJobs.readBean(jobSet);
    }

}
