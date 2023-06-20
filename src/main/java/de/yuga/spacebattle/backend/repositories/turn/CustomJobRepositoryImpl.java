package de.yuga.spacebattle.backend.repositories.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.researches.ActiveResearchTuple;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class CustomJobRepositoryImpl implements CustomJobRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
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

    @Nonnull
    @Override
    public List<Job> findAllJobsByPlanet(final int idPlanet) {
        return em.createNamedQuery("Job.getAllForPlanet", Job.class)
                .setParameter("idPlanet", idPlanet).getResultList();
    }

    @Nonnull
    @Override
    public List<Job> findAllJobsForUser(final int idUser) {
        return em.createNamedQuery("Job.getAllByOwner", Job.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Override
    public boolean isJobActiveFor(@Nonnull final Research research) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        return em.createNamedQuery("Job.isPresentForResearch", Boolean.class)
                .setParameter("research", research)
                .getSingleResult();
    }

    @Nullable
    @Override
    public List<ActiveResearchTuple> isJobActiveFor(@Nonnull final List<Research> researches) {
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        return em.createNamedQuery("Job.isPresentForResearch", ActiveResearchTuple.class)
                .setParameter("research", researches)
                .getResultList();
    }

}
