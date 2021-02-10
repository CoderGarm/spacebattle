package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import java.util.List;

public interface CustomModuleRepository {

    List<Module> findAllModules();
}
