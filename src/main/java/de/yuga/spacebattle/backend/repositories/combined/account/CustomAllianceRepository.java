package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.combined.account.Alliance;

import javax.annotation.Nonnull;
import java.util.List;

public interface CustomAllianceRepository {

    List<Alliance> findAllAlliances();

    /**
     * Checks if the username is already in use.
     *
     * @param name the name to check
     * @return <code>true</code> if the username is blocked, <code>false</code> otherwise
     */
    boolean existsAllianceName(@Nonnull String name);

    /**
     * Checks if the eMail address is already in use.
     *
     * @param code the code to check
     * @return <code>true</code> if the eMail address is blocked, <code>false</code> otherwise
     */
    boolean existsAllianceCode(@Nonnull String code);
}
