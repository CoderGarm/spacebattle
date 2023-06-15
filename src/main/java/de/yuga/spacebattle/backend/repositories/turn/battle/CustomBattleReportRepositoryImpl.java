package de.yuga.spacebattle.backend.repositories.turn.battle;

import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User_;
import de.yuga.spacebattle.backend.entities.turn.Tick_;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport;
import de.yuga.spacebattle.backend.entities.turn.battle.BattleReport_;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReportStatistics;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomBattleReportRepositoryImpl implements CustomBattleReportRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public int countAllWithUser(final int idUser) {
        return em.createNamedQuery("BattleReport.countAllWithUser", Long.class)
                .setParameter("idUser", idUser)
                .getSingleResult().intValue();
    }

    @Nullable
    @Override
    public BattleReport findByIdWithAllData(final int idUser, final int idBattleReport) {
        try {
            return em.createNamedQuery("BattleReport.findByIdWithAllData", BattleReport.class)
                    .setParameter("idBattleReport", idBattleReport)
                    .setParameter("idUser", idUser)
                    .getSingleResult();
        } catch (final NoResultException e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public List<BattleReportStatistics> findReportBasicInformationByPaging(final int idUser, final int page, final int size) {
        final int startPosition = page * size;

        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<BattleReport> cq = cb.createQuery(BattleReport.class);

        final Root<BattleReport> root = cq.from(BattleReport.class);

        final List<Order> orderList = new ArrayList<>();
        orderList.add(cb.desc(root.get(BattleReport_.tick)));
        orderList.add(cb.desc(root.get(Tick_.id)));

        final SetJoin<BattleReport, Owner> userJoin = root.join(BattleReport_.participatingUsers);
        userJoin.on(cb.equal(userJoin.get(User_.id), idUser));
        cq.select(root).where(userJoin.getOn());

        cq.orderBy(orderList);
        final TypedQuery<BattleReport> query = em.createQuery(cq)
                .setFirstResult(startPosition)
                .setMaxResults(size);
        final List<BattleReport> resultList = query.getResultList();
        return resultList.stream().filter(battleReport -> battleReport.getLastRound().getNo() > 1).map(BattleReportStatistics::new).collect(Collectors.toList());
    }
}
