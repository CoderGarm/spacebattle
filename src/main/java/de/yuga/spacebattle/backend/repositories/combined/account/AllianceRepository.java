package de.yuga.spacebattle.backend.repositories.combined.account;

import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.annotation.Nullable;
import java.util.List;

public interface AllianceRepository extends CrudRepository<Alliance, Integer>, CustomAllianceRepository {

    @Nullable
    @Query(name = "Alliance.findAllWithMembers")
    List<Alliance> findAllWithMembers();

    @Nullable
    @Query(name = "Alliance.findByIdWithMembers")
    Alliance findWithMembers(@Param("idAlliance") final int idAlliance);

    @Nullable
    @Query(name = "Alliance.findByIdWithApplications")
    Alliance findWithApplications(@Param("idAlliance") final int idAlliance);

    @Nullable
    @Query(name = "Alliance.hasOpenApplication")
    Alliance hasOpenApplication(@Param("idUser") final int idUser);
}
