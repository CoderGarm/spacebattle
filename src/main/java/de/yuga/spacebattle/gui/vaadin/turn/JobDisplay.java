package de.yuga.spacebattle.gui.vaadin.turn;


import com.google.common.base.Preconditions;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.gui.vaadin.constructables.ConstructableDisplay;

import javax.annotation.Nonnull;

public class JobDisplay extends VerticalLayout {

    public JobDisplay(@Nonnull final Job job) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        final Constructable constructable = job.getConstructable();
        final ConstructableDisplay constructableDisplay = new ConstructableDisplay(constructable);

        final long jobDoneAtZero = job.getJobDoneAtZero();
        final Label jobDoneAtZeroL = new Label("Points to finish left: " + jobDoneAtZero);
        add(constructableDisplay, jobDoneAtZeroL);
    }
}
