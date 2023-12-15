package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface StarSystemRepository extends JpaRepository<StarSystem, Integer>, CustomStarSystemRepository {

    @Nullable
    @Query("SELECT s FROM StarSystem s WHERE s.name = :name")
    StarSystem findByName(@Param("name") @Nonnull final String name);

    @Nullable
    @Query("SELECT s FROM StarSystem s WHERE s.name IN (:names)")
    Set<StarSystem> findByNames(@Nonnull final Set<String> names);
}
