package de.yuga.spacebattle.gui.impl.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.repositories.orbitals.StarsystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

//@RestController
//@RequestMapping(value = "/rest/sb/starsystem")
public class StarsystemApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(StarsystemApiImpl.class);

    @Nonnull
    private final StarsystemRepository starsystemController;

    //@Autowired
    public StarsystemApiImpl(@Nonnull final StarsystemRepository starsystemController) {
        Preconditions.checkNotNull(starsystemController, "starsystemC shouldn't be null!");

        this.starsystemController = starsystemController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getStarsystem() {
        final List<StarSystem> all = starsystemController.findAllStarsystems();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idStarsystem}")
    @ResponseBody
    public ResponseEntity<?> getStarsystem(@PathVariable("idStarsystem") final int idStarsystem) {
        StarSystem starsystem = starsystemController.findById(idStarsystem).get();
        if (starsystem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(starsystem);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putStarsystem(@RequestBody StarSystem starsystem) {
        if (starsystem.getId() < -1 || starsystem.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        starsystemController.save(starsystem);
        return ResponseEntity.ok(starsystem);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteStarsystem(@RequestBody StarSystem starsystem) {
        if (starsystem.getId() < -1 || starsystem.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        starsystemController.delete(starsystem);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
