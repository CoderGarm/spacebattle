package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import org.springframework.data.repository.CrudRepository;

public interface AllianceRepository extends CrudRepository<Alliance, Integer>, CustomAllianceRepository {
}
