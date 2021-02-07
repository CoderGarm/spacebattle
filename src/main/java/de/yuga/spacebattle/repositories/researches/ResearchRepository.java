package de.yuga.spacebattle.repositories.researches;

import de.yuga.spacebattle.entities.researches.Research;
import org.springframework.data.repository.CrudRepository;

public interface ResearchRepository extends CrudRepository<Research, Integer>, CustomResearchRepository {
}
