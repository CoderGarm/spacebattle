package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CustomFleetRepositoryImpl implements CustomFleetRepository {

    @PersistenceContext
    private EntityManager em;

    @Nonnull
    @Override
    public List<Fleet> findAllFleets() {
        return em.createNamedQuery("Fleet.getAll", Fleet.class).getResultList();
    }

    @Nonnull
    @Override
    @Transactional
    public Set<Fleet> findAllFleetsWithoutInterstellarMovement() {
        final List<Fleet> fleets = em.createNamedQuery("Fleet.getAllWithoutInterstellarMovement", Fleet.class).getResultList();
        final List<Fleet> fleets1 = em.createNamedQuery("Fleet.getAllWithoutMovement", Fleet.class).getResultList();
        fleets.addAll(fleets1);
        return new HashSet<>(fleets);
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsWithoutMovement() {
        return em.createNamedQuery("Fleet.getAllWithoutMovement", Fleet.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsWithMovement(final int idUser) {
        return em.createNamedQuery("Fleet.getAllWithMovement", Fleet.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsWithInterstellarMovement() {
        return em.createNamedQuery("Fleet.getAllFleetsWithInterstellarMovement", Fleet.class).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsBy(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return em.createNamedQuery("Fleet.getAllByUser", Fleet.class).setParameter("owner", user).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsByStarSystemAndOwner(final int idStarSystem, final int idOwner) {
        return em.createNamedQuery("Fleet.getAllByUserAndSystem", Fleet.class)
                .setParameter("idStarSystem", idStarSystem)
                .setParameter("idOwner", idOwner)
                .getResultList();
    }

    /**
     * The normal {@link JpaRepository#save(Object)} is not definitely safe to return not null
     * but storing the entity nevertheless. Strange but sadly true.
     *
     * @param fleet the fleet to store
     * @return the stored fleet
     */
    @Override
    public Fleet saveAndFlush(@Nonnull final Fleet fleet) {
        Preconditions.checkNotNull(fleet, "fleet shouldn't be null!");

        if (fleet.getId() != -1) {
            em.merge(fleet);
        } else {
            em.persist(fleet);

        }
        em.flush();
        return fleet;
    }

    @Nonnull
    @Override
    public Set<Fleet> findAllFleetsByPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return new HashSet<>(em.createNamedQuery("Fleet.getAllForPlanet", Fleet.class)
                .setParameter("xCoordinate", planet.getOrbit().getXCoordinate())
                .setParameter("yCoordinate", planet.getOrbit().getYCoordinate())
                .setParameter("system", planet.getSystem())
                .getResultList());
    }

    @Nonnull
    @Override
    public Set<Fleet> findAllDamagedFleetsByPlanetAndOwner(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return em.createNamedQuery("Fleet.getAllDamagedForPlanetAndOwner", Fleet.class)
                .setParameter("xCoordinate", planet.getOrbit().getXCoordinate())
                .setParameter("yCoordinate", planet.getOrbit().getYCoordinate())
                .setParameter("system", planet.getSystem())
                .getResultList()
                .stream()
                .filter(f -> f.getOwner().equals(planet.getOwner()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isShipClassInUse(final int idShipClass) {
        final Long amount = em.createNamedQuery("Fleet.checkShipInUse", Long.class)
                .setParameter("idShipClass", idShipClass)
                .getSingleResult();

        return amount > 0;
    }

    @Nonnull
    @Override
    public List<FleetClash> findAllFleetClashes() {
        final List<Fleet> nonMovingFleets = findAllFleetsWithoutMovement();
        final Map<FleetOrbit, List<Fleet>> fleetsToOrbit = nonMovingFleets.stream()
                .collect(Collectors.groupingBy(Fleet::getOrbit, Collectors.mapping(Function.identity(), Collectors.toList())));

        return fleetsToOrbit.entrySet().stream()
                .filter(entry -> {
                    final List<Fleet> fleets = entry.getValue();
                    final Set<User> users = fleets.stream().map(Fleet::getOwner).collect(Collectors.toSet());
                    if (users.size() != 2) {
                        // todo implement 3-way combat anyhow
                        return false;
                    }
                    final boolean userWithAlliancePresent = users.stream().anyMatch(user -> user.getAlliance() != null);
                    final boolean userWithoutAlliancePresent = users.stream().anyMatch(user -> user.getAlliance() == null);
                    if (userWithAlliancePresent && userWithoutAlliancePresent) {
                        return true;
                    }
                    final Set<Alliance> participatingAlliances = users.stream().map(User::getAlliance).filter(Objects::nonNull).collect(Collectors.toSet());
                    if (participatingAlliances.size() > 1) {
                        return true;
                    }
                    final Set<User> usersWithoutAlliances = users.stream().filter(user -> user.getAlliance() == null).collect(Collectors.toSet());
                    return usersWithoutAlliances.size() > 1;
                })
                .map(FleetClash::new).collect(Collectors.toList());
    }
}
