package de.yuga.spacebattle.backend.services.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.repositories.combined.account.AllianceRepository;
import de.yuga.spacebattle.backend.services.account.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class AllianceService {

    @Nonnull
    private final AllianceRepository allianceRepository;

    @Nonnull
    private final UserService userService;

    @Autowired

    public AllianceService(@Nonnull final AllianceRepository allianceRepository,
                           @Nonnull final UserService userService) {
        Preconditions.checkNotNull(allianceRepository, "allianceR shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.allianceRepository = allianceRepository;
        this.userService = userService;
    }

    @Nonnull
    public final Alliance save(@Nonnull final Alliance entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return allianceRepository.save(entity);
    }

    @Nonnull
    public List<Alliance> findAll() {
        return allianceRepository.findAllAlliances();
    }

    @Nullable
    public Alliance find(@Nonnull final Integer idAlliance) {
        Preconditions.checkNotNull(idAlliance, "idAlliance shouldn't be null!");
        return allianceRepository.findById(idAlliance).orElse(null);
    }

    @Nonnull
    public Alliance createAlliance(@Nonnull final String name, @Nonnull final String code) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(code, "code shouldn't be null!");

        return allianceRepository.save(new Alliance(name, code));
    }

    public void delete(@Nonnull final Alliance entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        allianceRepository.delete(entity);
    }
}
