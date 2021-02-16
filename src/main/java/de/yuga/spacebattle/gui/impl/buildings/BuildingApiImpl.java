package de.yuga.spacebattle.gui.impl.buildings;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;
import de.yuga.spacebattle.backend.repositories.buildings.BuildingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Nonnull;
import java.util.List;

//@RestController
//@RequestMapping(value = "v/sb/building")
public class BuildingApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(BuildingApiImpl.class);

    @Nonnull
    private final BuildingRepository buildingRepository;

    //@Autowired
    public BuildingApiImpl(@Nonnull final BuildingRepository buildingRepository) {
        Preconditions.checkNotNull(buildingRepository, "buildingC shouldn't be null!");

        this.buildingRepository = buildingRepository;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getBuilding() {
        final List<Building> all = buildingRepository.findAllBuildings();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idBuilding}")
    @ResponseBody
    public ResponseEntity<?> getBuilding(@PathVariable("idBuilding") final int idBuilding) {
        Building building = buildingRepository.findById(idBuilding).get();
        if (building == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(building);
    }
}
