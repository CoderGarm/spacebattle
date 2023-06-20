package de.yuga.spacebattle.backend.repositories.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
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

    @Override
    public boolean existsAllianceName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        return !em.createNamedQuery("Alliance.findByNameExact", Integer.class)
                .setParameter("name", name).getResultList().isEmpty();
    }

    @Override
    public boolean existsAllianceCode(@Nonnull final String code) {
        Preconditions.checkNotNull(code, "code shouldn't be null!");

        return !em.createNamedQuery("Alliance.findByCodeExact", Integer.class)
                .setParameter("code", code).getResultList().isEmpty();
    }
}
