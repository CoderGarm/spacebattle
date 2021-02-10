package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.combined.account.Alliance;

import java.util.List;

public interface CustomAllianceRepository {

    List<Alliance> findAllAlliances();
}
