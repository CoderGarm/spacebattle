package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.repositories.account.NonPlayerCharacterRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class NonPlayerCharacterService {

    @Nonnull
    private final NonPlayerCharacterRepository npcRepository;

    @Autowired
    public NonPlayerCharacterService(@Nonnull final NonPlayerCharacterRepository npcRepository) {
        this.npcRepository = Preconditions.checkNotNull(npcRepository, "userRepository shouldn't be null!");
    }

    @Nonnull
    public List<NonPlayerCharacter> findAll() {
        return npcRepository.findAll();
    }

    @Nullable
    public NonPlayerCharacter find(@Nonnull final Integer idUser) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");

        return npcRepository.findById(idUser).orElse(null);
    }

    @Nonnull
    public NonPlayerCharacter save(@Nonnull final NonPlayerCharacter entity) {
        Preconditions.checkNotNull(entity, "entity shouldn't be null!");

        return npcRepository.save(entity);
    }

    @Nonnull
    @Deprecated(since = "productive")
    public NonPlayerCharacter createNPC(@Nonnull final String username, @Nonnull final String shipPrefix) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(shipPrefix, "shipPrefix must not be empty");

        return this.save(new NonPlayerCharacter(username, shipPrefix));
    }

    @Nullable
    public NonPlayerCharacter findByUsername(@Nullable final String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        return npcRepository.findByUsername(username);
    }
}
