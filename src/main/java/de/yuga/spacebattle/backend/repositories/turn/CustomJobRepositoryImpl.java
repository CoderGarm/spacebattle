package de.yuga.spacebattle.backend.repositories.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.Job;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomJobRepositoryImpl implements CustomJobRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Job> findAllJobs() {
        return em.createNamedQuery("Job.getAll", Job.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Job> findAllJobsForConstruction(@Nonnull Construction facility) {
        Preconditions.checkNotNull(facility, "facility shouldn't be null!");

        return em.createNamedQuery("Job.getAllForConstruction", Job.class)
                .setParameter("facility", facility)
                .getResultList();
    }
}
