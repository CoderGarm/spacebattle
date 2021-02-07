package de.yuga.spacebattle.repositories.researches;

import de.yuga.spacebattle.entities.researches.Research;

import java.util.List;

public interface CustomResearchRepository {

    List<Research> findAllResearchs();
}
