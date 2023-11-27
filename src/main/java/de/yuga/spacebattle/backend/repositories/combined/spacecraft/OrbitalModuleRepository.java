package de.yuga.spacebattle.backend.repositories.combined.spacecraft;

import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.annotation.Nullable;
import java.util.Set;

public interface OrbitalModuleRepository extends JpaRepository<OrbitalModule, Integer> {

    @Nullable
    @Query("SELECT m FROM OrbitalModule m LEFT JOIN ResearchLevel rl ON (rl.research = m.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= m.unlockedThroughLevel")
    Set<OrbitalModule> findOrbitalModulesByUser(int idUser);
}
