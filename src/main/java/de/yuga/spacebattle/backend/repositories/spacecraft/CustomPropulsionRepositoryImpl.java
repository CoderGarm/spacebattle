package de.yuga.spacebattle.backend.repositories.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomPropulsionRepositoryImpl implements CustomPropulsionRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Propulsion> findAll() {
        final List<Propulsion> resultList = em.createNamedQuery("Propulsion.getAll", Propulsion.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public List<Propulsion> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("Propulsion.getAllByResearches", Propulsion.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
