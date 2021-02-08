package de.yuga.spacebattle.restapi.impl.orbitals;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.orbitals.Planet;
import de.yuga.spacebattle.repositories.orbitals.PlanetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/planet")
public class PlanetApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(PlanetApiImpl.class);

    @Nonnull
    private final PlanetRepository planetController;

    @Autowired
    public PlanetApiImpl(@Nonnull final PlanetRepository planetController) {
        Preconditions.checkNotNull(planetController, "planetC shouldn't be null!");

        this.planetController = planetController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getPlanet() {
        final List<Planet> all = planetController.findAllPlanets();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idPlanet}")
    @ResponseBody
    public ResponseEntity<?> getPlanet(@PathVariable("idPlanet") final int idPlanet) {
        Planet planet = planetController.findById(idPlanet).get();
        if (planet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(planet);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putPlanet(@RequestBody Planet planet) {
        if (planet.getId() < -1 || planet.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        planetController.save(planet);
        return ResponseEntity.ok(planet);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deletePlanet(@RequestBody Planet planet) {
        if (planet.getId() < -1 || planet.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        planetController.delete(planet);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
