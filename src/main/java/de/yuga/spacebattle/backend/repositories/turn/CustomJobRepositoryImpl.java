package de.yuga.spacebattle.backend.repositories.turn;

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
}
