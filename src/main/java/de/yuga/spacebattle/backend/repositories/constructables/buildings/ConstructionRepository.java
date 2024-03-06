package de.yuga.spacebattle.backend.repositories.constructables.buildings;

import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EProductionCategory;
import de.yuga.spacebattle.backend.enums.EResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface ConstructionRepository extends JpaRepository<Construction, Integer>, CustomConstructionRepository {

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.owner.id = :idUser AND c.level > c.operationalLevel")
    List<Construction> findInoperationalForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.owner.id = :idUser")
    List<Construction> findAllConstructionsForUser(@Param("idUser") final int idUser);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.id = :idPlanet AND c.level > c.operationalLevel")
    List<Construction> findInoperationalForPlanet(@Param("idPlanet") final int idPlanet);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.id = :idPlanet")
    Set<Construction> findAllConstructionsOnPlanet(final int idPlanet);

    @Nullable
    @Query("SELECT c FROM Construction c WHERE c.planet.id = :idPlanet AND c.building.productionType.productionTarget = :productionTarget")
    Set<Construction> findAllConstructionsOnPlanetForTarget(final int idPlanet, @Nonnull final EResourceType productionTarget);

    @Nullable
    @Query("SELECT c FROM Construction c LEFT JOIN FETCH c.jobs j WHERE c.planet.id = :idPlanet")
    Set<Construction> findAllConstructionsOnPlanetWithJobs(final int idPlanet);

    @Query("SELECT CASE WHEN (COUNT(c) > 0) THEN TRUE ELSE FALSE END FROM Construction c " +
            "WHERE c.planet.id = :idPlanet " +
            "AND c.building.productionType.productionTarget = :productionTarget " +
            "AND c.building.productionType.productionCategory = :productionCategory " +
            "AND c.operationalLevel >= 1")
    boolean hasPlanetProductionType(final int idPlanet, @Nonnull final EResourceType productionTarget, @Nonnull final EProductionCategory productionCategory);

    @Query("SELECT CASE WHEN (COUNT(c) > 0) THEN TRUE ELSE FALSE END FROM Construction c LEFT JOIN c.jobs j " +
            "WHERE c.planet.id = :idPlanet " +
            "AND c.building.productionType.productionTarget = :productionTarget " +
            "AND c.building.productionType.productionCategory = :productionCategory " +
            "AND j.isDeleted = false ")
    boolean isActiveJobPresentForTargetAtPlanet(final int idPlanet, @Nonnull final EResourceType productionTarget, @Nonnull final EProductionCategory productionCategory);

    @Nullable
    @Query("SELECT c FROM Construction c " +
            "LEFT JOIN FETCH c.jobs j " +
            "WHERE c.planet.id = :idPlanet " +
            "AND c.building.id = :idBuilding")
    Construction findByPlanetAndBuilding(int idPlanet, int idBuilding);

    @Nullable
    @Query("SELECT c FROM Construction c " +
            "LEFT JOIN FETCH c.jobs j " +
            "WHERE c.planet.id = :idPlanet " +
            "AND c.building.productionType.productionTarget = :productionTarget " +
            "AND c.building.productionType.productionCategory = :productionCategory")
    Construction findByPlanetProductionType(final int idPlanet, @Nonnull final EResourceType productionTarget, @Nonnull final EProductionCategory productionCategory);

    @Nullable
    @Query("SELECT c.building.productionType.productionTarget FROM Construction c WHERE c.planet.id = :idPlanet AND c.building.productionType.productionCategory = de.yuga.spacebattle.backend.enums.EProductionCategory.PRODUCE")
    Set<EResourceType> findProductionCapabilities(final int idPlanet);

    @Nullable
    @Query("SELECT w FROM Construction w WHERE w.planet.owner.id = :idUser AND w.activated = :today")
    List<Construction> findActivatedByUser(final int idUser, @Nonnull final Tick today);
}
