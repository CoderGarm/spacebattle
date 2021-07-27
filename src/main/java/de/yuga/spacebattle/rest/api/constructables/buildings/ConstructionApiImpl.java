package de.yuga.spacebattle.rest.api.constructables.buildings;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.repositories.constructables.buildings.ConstructionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

//@RestController
//@RequestMapping(value = "/rest/sb/construction")
public class ConstructionApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConstructionApiImpl.class);

    @Nonnull
    private final ConstructionRepository constructionController;

    //@Autowired
    public ConstructionApiImpl(@Nonnull final ConstructionRepository constructionController) {
        Preconditions.checkNotNull(constructionController, "constructionC shouldn't be null!");

        this.constructionController = constructionController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getConstruction() {
        final List<Construction> all = constructionController.findAllConstructions();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idConstruction}")
    @ResponseBody
    public ResponseEntity<?> getConstruction(@PathVariable("idConstruction") final int idConstruction) {
        Construction construction = constructionController.findById(idConstruction).get();
        if (construction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(construction);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteConstruction(@RequestBody Construction construction) {
        if (construction.getId() < -1 || construction.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        constructionController.delete(construction);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
