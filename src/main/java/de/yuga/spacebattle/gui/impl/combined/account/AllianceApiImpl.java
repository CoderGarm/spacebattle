package de.yuga.spacebattle.gui.impl.combined.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.services.account.UserService;
import de.yuga.spacebattle.backend.services.combined.account.AllianceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/rest/sb/alliance")
public class AllianceApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(AllianceApiImpl.class);

    @Nonnull
    private final AllianceService allianceService;

    @Nonnull
    private final UserService userService;

    @Autowired
    public AllianceApiImpl(@Nonnull final AllianceService allianceService,
                           @Nonnull final UserService userService) {
        Preconditions.checkNotNull(allianceService, "allianceService shouldn't be null!");
        Preconditions.checkNotNull(userService, "userService shouldn't be null!");

        this.allianceService = allianceService;
        this.userService = userService;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAlliance() {
        final List<Alliance> all = allianceService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idAlliance}")
    @ResponseBody
    public ResponseEntity<?> getAlliance(@PathVariable("idAlliance") final int idAlliance) {
        Alliance alliance = allianceService.find(idAlliance);
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
        allianceService.save(alliance);
        return ResponseEntity.ok(alliance);
    }

    @PutMapping("/user")
    @ResponseBody
    public ResponseEntity<?> putUser(@RequestBody Alliance alliance) {
        if (alliance.getId() < -1 || alliance.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        allianceService.save(alliance);
        return ResponseEntity.ok(alliance);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteAlliance(@RequestBody Alliance alliance) {
        if (alliance.getId() < -1 || alliance.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        allianceService.delete(alliance);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
