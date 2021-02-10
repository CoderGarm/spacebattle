package de.yuga.spacebattle.gui.impl.constructables.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.repositories.constructables.spacecraft.ShipClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/shipClass")
public class ShipClassApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassApiImpl.class);

    @Nonnull
    private final ShipClassRepository shipClassController;

    @Autowired
    public ShipClassApiImpl(@Nonnull final ShipClassRepository shipClassController) {
        Preconditions.checkNotNull(shipClassController, "shipClassC shouldn't be null!");

        this.shipClassController = shipClassController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getShipClass() {
        final List<ShipClass> all = shipClassController.findAllShipClasses();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idShipClass}")
    @ResponseBody
    public ResponseEntity<?> getShipClass(@PathVariable("idShipClass") final int idShipClass) {
        ShipClass shipClass = shipClassController.findById(idShipClass).get();
        if (shipClass == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(shipClass);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putShipClass(@RequestBody ShipClass shipClass) {
        if (shipClass.getId() < -1 || shipClass.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        shipClassController.save(shipClass);
        return ResponseEntity.ok(shipClass);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteShipClass(@RequestBody ShipClass shipClass) {
        if (shipClass.getId() < -1 || shipClass.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        shipClassController.delete(shipClass);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
