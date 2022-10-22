package de.yuga.spacebattle.backend.repositories.misc;

import de.yuga.spacebattle.backend.entities.misc.DBPatch;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nonnull;
import java.util.List;

public interface DBPatchRepository extends CrudRepository<DBPatch, Integer> {

    @Query("SELECT CASE WHEN (COUNT(p) > 0) THEN TRUE ELSE FALSE END FROM DBPatch p WHERE p.version IN (:dbPatches)")
    boolean isEveryPatchPresent(@Param("dbPatches") @Nonnull List<String> dbPatches);
}
