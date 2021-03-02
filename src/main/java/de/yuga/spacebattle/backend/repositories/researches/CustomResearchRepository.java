package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;

import java.util.List;

public interface CustomResearchRepository {

    List<Research> findAll();
}
