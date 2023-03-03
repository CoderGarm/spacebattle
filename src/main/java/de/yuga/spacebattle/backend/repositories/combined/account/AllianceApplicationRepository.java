package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.combined.account.AllianceApplication;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.enums.EApplicationState;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public interface AllianceApplicationRepository extends CrudRepository<AllianceApplication, Integer> {

    @Nullable
    @Query("SELECT a FROM AllianceApplication a WHERE a.alliance.id = :idAlliance AND a.applicant.id = :idApplicant AND a.applicationState = :state")
    AllianceApplication findByIdAllianceAndIdUserAndState(final int idAlliance, final int idApplicant, @Nonnull final EApplicationState state);

    @Nullable
    @Query("SELECT DISTINCT ap.applicant FROM AllianceApplication ap LEFT JOIN User u ON (u.id = :idAdmin) WHERE ap.alliance.id = u.alliance.id AND ap.applicationState = :state")
    Set<User> findByAllianceOfAdmin(int idAdmin, @Nonnull final EApplicationState state);

    @Nullable
    @Query("SELECT DISTINCT ap.alliance FROM AllianceApplication ap WHERE ap.applicant.id = :idUser AND ap.applicationState = :state")
    Set<Alliance> hasOpenApplication(final int idUser, @Nonnull final EApplicationState state);

    @Modifying
    @Transactional
    @Query("UPDATE AllianceApplication ap SET ap.applicationState = :revoked, ap.decidedAt = :today WHERE ap.applicant.id = :idUser AND ap.applicationState = :open")
    void closeAllOpenApplications(final int idUser, @Nonnull final Tick today, @Nonnull final EApplicationState open, @Nonnull final EApplicationState revoked);
}
