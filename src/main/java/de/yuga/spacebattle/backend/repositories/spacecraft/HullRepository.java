package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.enums.EHullType;
import de.yuga.spacebattle.backend.repositories.spacecraft.custom.CustomHullRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface HullRepository extends CrudRepository<Hull, Integer>, CustomHullRepository {

    @Nullable
    @Query("SELECT h FROM Hull h WHERE h.hullType = :hullType")
    List<Hull> findByHullType(@Param("hullType") @Nonnull final EHullType hullType);
}
