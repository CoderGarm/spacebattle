package de.yuga.spacebattle.restapi.impl.combined.spacecraft;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.entities.turn.Move;
import de.yuga.spacebattle.logic.combined.spacecraft.FleetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/fleet")
public class FleetApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(FleetApiImpl.class);

    @Nonnull
    private final FleetService fleetRepository;

    @Autowired
    public FleetApiImpl(@Nonnull final FleetService fleetRepository) {
        Preconditions.checkNotNull(fleetRepository, "fleetC shouldn't be null!");

        this.fleetRepository = fleetRepository;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getFleet() {
        final List<Fleet> all = fleetRepository.findAllFleets();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/=idFleet={idFleet}")
    @ResponseBody
    public ResponseEntity<?> getFleet(@PathVariable("idFleet") final int idFleet) {
        Fleet fleet = fleetRepository.findById(idFleet);
        if (fleet == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(fleet);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putFleet(@RequestBody Fleet fleet) {
        if (fleet.getId() < -1 || fleet.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        fleetRepository.save(fleet);
        return ResponseEntity.ok(fleet);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteFleet(@RequestBody Fleet fleet) {
        if (fleet.getId() < -1 || fleet.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        fleetRepository.delete(fleet);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{idFleet1}/{idFLeet2}")
    @ResponseBody
    public ResponseEntity<?> mergeFleets(@PathVariable("idFleet1") Integer idFleet1,
                                         @PathVariable("idFLeet2") Integer idFLeet2) {

        if (idFleet1 == null || idFleet1 < 1 || idFLeet2 == null || idFLeet2 < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            Fleet mergedFleet = fleetRepository.mergeFleets(idFleet1, idFLeet2);
            return ResponseEntity.ok(mergedFleet);
        } catch (final NotifySBUserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/range/?idFLeet={idFleet}")
    @ResponseBody
    public ResponseEntity<?> getTickRange(@PathVariable("idFleet") Integer idFleet) {
        if (idFleet == null || idFleet < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Fleet fleet = fleetRepository.findById(idFleet);
        if (fleet != null) {
            BigDecimal ftlRangePerTick = fleet.getFTLRangePerTick();
            return ResponseEntity.ok(ftlRangePerTick);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/checkRange/{idFleet}/{idPlanet}")
    @ResponseBody
    public ResponseEntity<?> checkRange(@PathVariable("idFleet") Integer idFleet,
                                        @PathVariable("idPlanet") Integer idPlanet) {
        if (idFleet == null || idFleet < 1 || idPlanet == null || idPlanet < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        int calculateDistance = fleetRepository.calculateDistance(idFleet, idPlanet);
        return ResponseEntity.ok(calculateDistance);
    }

    @PutMapping("/move/{idFleet}/{idPlanet}")
    @ResponseBody
    public ResponseEntity<?> move(@PathVariable("idFleet") Integer idFleet,
                                  @PathVariable("idPlanet") Integer idPlanet) {
        if (idFleet == null || idFleet < 1 || idPlanet == null || idPlanet < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Move move = fleetRepository.moveFleet(idFleet, idPlanet);
        return ResponseEntity.ok(move);
    }

}
