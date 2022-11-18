package de.yuga.spacebattle.backend.repositories.misc;

import de.yuga.spacebattle.backend.entities.misc.DBPatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DBPatchRepository extends JpaRepository<DBPatch, Integer> {
}
