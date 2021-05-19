package de.yuga.spacebattle.backend.repositories.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomElectronicWarfareRepositoryImpl implements CustomElectronicWarfareRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<ElectronicWarfare> findAll() {
        final List<ElectronicWarfare> resultList = em.createNamedQuery("ElectronicWarfare.getAll", ElectronicWarfare.class).getResultList();
        return resultList;
    }

    @Nonnull
    @Override
    public List<ElectronicWarfare> findAllByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        final Set<Research> researches = user.getResearches().keySet();
        if (researches.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createNamedQuery("ElectronicWarfare.getAllByResearches", ElectronicWarfare.class)
                .setParameter("researches", researches)
                .getResultList();
    }
}
