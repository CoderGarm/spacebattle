package de.yuga.spacebattle.restapi.impl.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.spacecrafts.Module;
import de.yuga.spacebattle.repositories.spacecraft.ModuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping("/sb/module")
public class ModuleApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(ModuleApiImpl.class);

    @Nonnull
    private final ModuleRepository moduleController;

    @Autowired
    public ModuleApiImpl(@Nonnull final ModuleRepository moduleController) {
        Preconditions.checkNotNull(moduleController, "moduleC shouldn't be null!");

        this.moduleController = moduleController;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getModule() {
        final List<Module> all = moduleController.findAllModules();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idModule}")
    @ResponseBody
    public ResponseEntity<?> getModule(@PathVariable("idModule") final int idModule) {
        Module module = moduleController.findById(idModule).get();
        if (module == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(module);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putModule(@RequestBody Module module) {
        if (module.getId() < -1 || module.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        moduleController.save(module);
        return ResponseEntity.ok(module);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteModule(@RequestBody Module module) {
        if (module.getId() < -1 || module.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        moduleController.delete(module);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
