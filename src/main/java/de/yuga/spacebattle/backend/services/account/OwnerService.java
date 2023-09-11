package de.yuga.spacebattle.backend.services.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.NonPlayerCharacter;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.repositories.account.NonPlayerCharacterRepository;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerService {

    @Nonnull
    private final NonPlayerCharacterRepository npcRepository;

    @Nonnull
    private final UserRepository userRepository;

    @Autowired
    public OwnerService(@Nonnull final NonPlayerCharacterRepository npcRepository,
                        @Nonnull final UserRepository userRepository) {
        this.npcRepository = Preconditions.checkNotNull(npcRepository, "userRepository shouldn't be null!");
        this.userRepository = Preconditions.checkNotNull(userRepository, "userRepository must not be empty");
    }

    @Nonnull
    public List<Owner> findAll() {
        final List<Owner> result = new ArrayList<>();
        result.addAll(userRepository.findAll());
        result.addAll(npcRepository.findAll());
        return result;
    }

    @Nullable
    public Owner find(@Nonnull final Integer idUser) {
        Preconditions.checkNotNull(idUser, "idUser shouldn't be null!");

        final Owner owner = userRepository.findById(idUser).orElse(null);
        if (owner != null) {
            return owner;
        }
        return npcRepository.findById(idUser).orElse(null);

    }

    @Nullable
    public Owner findByUsername(@Nullable final String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        return npcRepository.findByUsername(username);
    }

    @Nonnull
    public Owner getRandomNPC() {
        return npcRepository.getNPC(PageRequest.of(0, 2)).get(1);
    }

    @Nonnull
    public List<NonPlayerCharacter> findAllNPC() {
        return npcRepository.findAll();
    }

    @Nonnull
    public List<NonPlayerCharacter> findAllNPCWithPlanet() {
        return npcRepository.findAll().stream()
                .filter(npc -> !npc.getUsername().equals(MasterOfTheUniverseService.DEFEATED_OPPONENT))
                .filter(npc -> !npc.getUsername().equals(MasterOfTheUniverseService.PIRATE))
                .collect(Collectors.toList());
    }
}
