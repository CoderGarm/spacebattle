package de.yuga.spacebattle.backend.repositories.researches;

import de.yuga.spacebattle.backend.entities.researches.Research;
import org.springframework.data.repository.CrudRepository;

public interface ResearchRepository extends CrudRepository<Research, Integer>, CustomResearchRepository {
}
