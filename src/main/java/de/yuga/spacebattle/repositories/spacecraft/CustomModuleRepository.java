package de.yuga.spacebattle.repositories.spacecraft;

import de.yuga.spacebattle.entities.spacecrafts.Module;

import java.util.List;

public interface CustomModuleRepository {

    List<Module> findAllModules();
}
