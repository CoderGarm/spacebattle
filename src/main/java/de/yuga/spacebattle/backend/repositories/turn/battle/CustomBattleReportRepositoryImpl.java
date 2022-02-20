package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
public class CustomBattleReportRepositoryImpl implements CustomBattleReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<BattleReport> findAllWithUser(int idUser) {
        return em.createNamedQuery("BattleReport.findAllWithUser", BattleReport.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Override
    public BattleReport findLatestWithUser(int idUser) {
        try {
            return em.createNamedQuery("BattleReport.findLatestWithUser", BattleReport.class)
                    .setParameter("idUser", idUser)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public List<BattleReport> findReportsWithUserWithPaging(final int idUser, final int page, final int size) {
        final int startPosition = page * size;
        final int endPosition = page * size + size;
        return em.createNamedQuery("BattleReport.findLatestWithUser", BattleReport.class)
                .setParameter("idUser", idUser)
                .setFirstResult(startPosition)
                .setMaxResults(endPosition)
                .getResultList();
    }
}
