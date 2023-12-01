package de.yuga.spacebattle.backend.repositories.spacecraft;

import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MissileRepository extends CrudRepository<Missile, Integer> {

    @Query(name = "Missile.getAllByResearches")
    List<Missile> findAllByUser(int idUser);
}
