package de.yuga.spacebattle.repositories.combined.account;

import de.yuga.spacebattle.entities.combined.account.Alliance;
import org.springframework.data.repository.CrudRepository;

public interface AllianceRepository extends CrudRepository<Alliance, Integer>, CustomAllianceRepository {
}
