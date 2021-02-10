package de.yuga.spacebattle.gui.impl.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Tick;
import de.yuga.spacebattle.backend.logic.turn.TickService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/tick")
public class TickApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(TickApiImpl.class);

    @Nonnull
    private final TickService tickController;

    @Autowired
    public TickApiImpl(@Nonnull final TickService tickController) {
        Preconditions.checkNotNull(tickController, "tickC shouldn't be null!");

        this.tickController = tickController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getTick() {
        final List<Tick> all = tickController.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idTick}")
    @ResponseBody
    public ResponseEntity<?> getTick(@PathVariable("idTick") final int idTick) {
        Tick tick = tickController.find(idTick);
        if (tick == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(tick);
    }

    @GetMapping(value = "/doTick")
    @ResponseBody
    public ResponseEntity<?> tick() {
        LOGGER.info("Tick initialized");
        Tick now = tickController.doTick();
        return ResponseEntity.ok(now);
    }
}
