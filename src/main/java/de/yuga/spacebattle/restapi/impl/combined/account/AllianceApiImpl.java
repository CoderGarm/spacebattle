package de.yuga.spacebattle.restapi.impl.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.combined.account.Alliance;
import de.yuga.spacebattle.repositories.account.UserRepository;
import de.yuga.spacebattle.repositories.combined.account.AllianceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping("/sb/alliance")
public class AllianceApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(AllianceApiImpl.class);

    @Nonnull
    private final AllianceRepository allianceRepository;

    @Nonnull
    private final UserRepository userRepository;

    @Autowired
    public AllianceApiImpl(@Nonnull final AllianceRepository allianceRepository,
                           @Nonnull final UserRepository userRepository) {
        Preconditions.checkNotNull(allianceRepository, "allianceC shouldn't be null!");
        Preconditions.checkNotNull(userRepository, "userRepository shouldn't be null!");

        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAlliance() {
        final List<Alliance> all = allianceRepository.findAllAlliances();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idAlliance}")
    @ResponseBody
    public ResponseEntity<?> getAlliance(@PathVariable("idAlliance") final int idAlliance) {
        Alliance alliance = allianceRepository.findById(idAlliance).get();
        if (alliance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(alliance);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putAlliance(@RequestBody Alliance alliance) {
        if (alliance.getId() < -1 || alliance.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        allianceRepository.save(alliance);
        return ResponseEntity.ok(alliance);
    }

    @PutMapping("/user")
    @ResponseBody
    public ResponseEntity<?> putUser(@RequestBody Alliance alliance) {
        if (alliance.getId() < -1 || alliance.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        allianceRepository.save(alliance);
        return ResponseEntity.ok(alliance);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteAlliance(@RequestBody Alliance alliance) {
        if (alliance.getId() < -1 || alliance.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        allianceRepository.delete(alliance);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
