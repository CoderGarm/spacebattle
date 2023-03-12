package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.combat.dto.FleetClash;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.orbitals.FleetOrbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
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
    public Set<Fleet> findAllFleetsWithoutInterstellarMovement(final int idUser) {
        final List<Fleet> fleets = em.createNamedQuery("Fleet.getAllWithoutInterstellarMovement", Fleet.class).getResultList();
        final List<Fleet> fleets1 = em.createNamedQuery("Fleet.getAllWithoutMovement", Fleet.class).getResultList();
        fleets.addAll(fleets1);
        fleets.removeIf(f -> shouldNotAppearOnMap(f, idUser));
        return new HashSet<>(fleets);
    }

    private static boolean shouldNotAppearOnMap(@Nonnull final Fleet fleet,
                                                final int idUser) {
        Preconditions.checkNotNull(fleet, "fleet must not be empty");
        Preconditions.checkNotNull(fleet.getOrbit(), "fleet.getOrbit() must not be empty");
        Preconditions.checkNotNull(fleet.getOrbit().getSystem(), "fleet.getOrbit().getSystem() must not be empty");

        final StarSystem system = fleet.getOrbit().getSystem();
        final Set<Planet> planets = system.getPlanets();
        final boolean fleetInSystemOfUser = planets.stream().filter(p -> p.getOwner() != null).anyMatch(p -> p.getOwner().getId() == idUser);
        if (fleetInSystemOfUser) {
            return false;
        }

        final Set<Fleet> fleets = system.getFleets();
        final boolean systemContainsUserFleet = fleets.stream().anyMatch(f -> f.getOwner().getId() == idUser);
        if (systemContainsUserFleet) {
            return false;
        }

        return fleet.getOwner().getId() != idUser;
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
    public List<Fleet> findAllFleetsWithInterstellarMovement(final int idUser) {
        return em.createNamedQuery("Fleet.getFleetsWithInterstellarMovement", Fleet.class)
                .setParameter("idUser", idUser)
                .getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsBy(final int idUser) {
        return em.createNamedQuery("Fleet.getAllByUser", Fleet.class).setParameter("idUser", idUser).getResultList();
    }

    @Nonnull
    @Override
    public List<Fleet> findAllFleetsByStarSystemAndOwner(final int idStarSystem, final int idOwner) {
        return em.createNamedQuery("Fleet.getAllByUserAndSystem", Fleet.class)
                .setParameter("idStarSystem", idStarSystem)
                .setParameter("idOwner", idOwner)
                .getResultList();
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
    public Set<Fleet> findAllAnchoredForPlanet(@Nonnull final Planet planet) {
        Preconditions.checkNotNull(planet, "planet shouldn't be null!");

        return new HashSet<>(em.createNamedQuery("Fleet.getAllAnchoredForPlanet", Fleet.class)
                .setParameter("xCoordinate", planet.getOrbit().getXCoordinate())
                .setParameter("yCoordinate", planet.getOrbit().getYCoordinate())
                .setParameter("system", planet.getSystem())
                .getResultList());
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
                .filter(Fleet::isActive)
                .filter(f -> f.getOrbit() != null)
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
