package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomAllianceRepositoryImpl implements CustomAllianceRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Alliance> findAllAlliances() {
        final List<Alliance> resultList = em.createNamedQuery("Alliance.getAll", Alliance.class).getResultList();
        return resultList;
    }
}
