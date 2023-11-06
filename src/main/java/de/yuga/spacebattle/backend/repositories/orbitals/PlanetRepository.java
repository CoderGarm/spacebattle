package de.yuga.spacebattle.backend.repositories.orbitals;


import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.turn.resources.MiningFactors;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface PlanetRepository extends JpaRepository<Planet, Integer>, CustomPlanetRepository {

    @Nullable
    @Query("SELECT p.owner.id FROM Planet p WHERE p.id = :idPlanet AND p.isMain = true")
    Integer findAllById(@Param("idPlanet") final int idPlanet);

    @Nullable
    @Query("SELECT DISTINCT p FROM Planet p WHERE p.owner.id = :idUser " +
            "AND true = (SELECT CASE WHEN (COUNT(c) > 0) THEN TRUE ELSE FALSE END FROM Construction c WHERE c.planet = p AND c.building.productionType.productionTarget = de.yuga.spacebattle.backend.enums.EResourceType.RESEARCH) " +
            "ORDER BY p.colonizedAt")
    List<Planet> findAllColonizedByWithResearchLab(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT DISTINCT r FROM Planet p JOIN p.resourceDeposit r WHERE p.owner.id = :idUser AND :resourceType IN (KEY(r.resources))")
    List<ResourceDeposit> findResourceDepositOfColonizedPlanets(final int idUser, @Nonnull final String resourceType); // as string, not enum because collection search

    @Nullable
    @Query("SELECT p.resourceDeposit FROM Planet p WHERE p.id = :idPlanet")
    ResourceDeposit findResourceDeposit(int idPlanet);

    @Nullable
    @Query("SELECT p.miningFactors FROM Planet p WHERE p.id = :idPlanet")
    MiningFactors findMiningFactors(int idPlanet);

    @Nullable
    @Query("SELECT p.id FROM Planet p WHERE p.owner.id = :idUser")
    List<Integer> findAllColonizedByForID(int idUser);

    @Nullable
    @Query("SELECT DISTINCT p FROM Planet p LEFT JOIN FETCH p.constructions c WHERE p.owner IS NOT NULL AND p.owner.dType = de.yuga.spacebattle.backend.enums.OwnerType.USER")
    List<Planet> findAllForTick();

    @Nullable
    @Query("SELECT DISTINCT p.system.id FROM Planet p WHERE p.owner.id = :idUser")
    Set<Integer> findAllSystemIDsForUser(final int idUser);
}
