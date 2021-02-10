package de.yuga.spacebattle.backend.repositories.turn;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.turn.Job;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomJobRepositoryImpl implements CustomJobRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Job> findAllJobs() {
        final List<Job> resultList = em.createNamedQuery("Job.getAll", Job.class).getResultList();
        return resultList;
    }

    @Override
    public boolean researchPossible(User user) {
        Integer singleResult = em.createNamedQuery("Job.researchPossibleForOwner", Integer.class).getSingleResult();
        if (singleResult > 0) {
            return false;
        }
        return true;
    }
}
