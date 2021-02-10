package de.yuga.spacebattle.gui.impl.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.repositories.researches.ResearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/research")
public class ResearchApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResearchApiImpl.class);

    @Nonnull
    private final ResearchRepository researchController;

    @Autowired
    public ResearchApiImpl(@Nonnull final ResearchRepository researchController) {
        Preconditions.checkNotNull(researchController, "researchC shouldn't be null!");

        this.researchController = researchController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getResearch() {
        final List<Research> all = researchController.findAllResearchs();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idResearch}")
    @ResponseBody
    public ResponseEntity<?> getResearch(@PathVariable("idResearch") final int idResearch) {
        Research research = researchController.findById(idResearch).get();
        if (research == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(research);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putResearch(@RequestBody Research research) {
        if (research.getId() < -1 || research.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        researchController.save(research);
        return ResponseEntity.ok(research);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteResearch(@RequestBody Research research) {
        if (research.getId() < -1 || research.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        researchController.delete(research);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
