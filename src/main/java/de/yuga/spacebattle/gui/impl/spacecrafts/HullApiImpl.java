package de.yuga.spacebattle.gui.impl.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.repositories.spacecraft.HullRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/rest/sb/hull")
public class HullApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(HullApiImpl.class);

    @Nonnull
    private final HullRepository hullController;

    @Autowired
    public HullApiImpl(@Nonnull final HullRepository hullController) {
        Preconditions.checkNotNull(hullController, "hullC shouldn't be null!");

        this.hullController = hullController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getHull() {
        final List<Hull> all = hullController.findAllHulls();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idHull}")
    @ResponseBody
    public ResponseEntity<?> getHull(@PathVariable("idHull") final int idHull) {
        Hull hull = hullController.findById(idHull).get();
        if (hull == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(hull);
    }
}
