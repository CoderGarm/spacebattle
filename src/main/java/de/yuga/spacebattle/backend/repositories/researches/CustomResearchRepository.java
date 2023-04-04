package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomResearchRepository {

    @Nonnull
    List<Research> findAll();
}
