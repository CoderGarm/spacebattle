package de.yuga.spacebattle.repositories.combined.account;

import de.yuga.spacebattle.entities.combined.account.Alliance;

import java.util.List;

public interface CustomAllianceRepository {

    List<Alliance> findAllAlliances();
}
