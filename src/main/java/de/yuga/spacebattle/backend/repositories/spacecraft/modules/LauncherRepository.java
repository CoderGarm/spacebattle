package de.yuga.spacebattle.backend.repositories.spacecraft.modules;

import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Launcher;
import de.yuga.spacebattle.backend.repositories.spacecraft.modules.custom.CustomLauncherRepository;
import org.springframework.data.repository.CrudRepository;

public interface LauncherRepository extends CrudRepository<Launcher, Integer>, CustomLauncherRepository {
}
