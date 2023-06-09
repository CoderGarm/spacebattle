package de.yuga.spacebattle.backend.repositories.orbitals;

import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface StarSystemRepository extends CrudRepository<StarSystem, Integer>, CustomStarSystemRepository {

    @Nullable
    @Query("SELECT s FROM StarSystem s WHERE s.name = :name")
    StarSystem findByName(@Param("name") @Nonnull final String name);
}
