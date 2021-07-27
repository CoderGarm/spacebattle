package de.yuga.spacebattle.rest.dto.turn;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class JobList extends ArrayList<Job> {

    public JobList(Collection<de.yuga.spacebattle.backend.entities.turn.Job> jobs) {
        Preconditions.checkNotNull(jobs, "jobs shouldn't be null!");

        final Set<Job> collect = jobs.stream().map(Job::new).collect(Collectors.toSet());
        addAll(collect);
    }
}
