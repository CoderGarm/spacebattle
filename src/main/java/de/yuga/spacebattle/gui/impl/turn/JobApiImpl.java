package de.yuga.spacebattle.gui.impl.turn;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.repositories.turn.JobRepository;
import de.yuga.spacebattle.backend.services.turn.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/rest/sb/job")
public class JobApiImpl {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobApiImpl.class);

    @Nonnull
    private final JobRepository jobController;

    @Nonnull
    private final JobService jobS;

    @Autowired
    public JobApiImpl(@Nonnull final JobRepository jobController,
                      @Nonnull final JobService jobS) {
        Preconditions.checkNotNull(jobController, "jobC shouldn't be null!");
        Preconditions.checkNotNull(jobS, "jobS shouldn't be null!");

        this.jobController = jobController;
        this.jobS = jobS;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getJob() {
        final List<Job> all = jobController.findAllJobs();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idJob}")
    @ResponseBody
    public ResponseEntity<?> getJob(@PathVariable("idJob") final Integer idJob) {
        if (validateID(idJob)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Job job = jobController.findById(idJob).get();
        if (job == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(job);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putJob(@RequestBody Job job) {
        if (job.getId() != -1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        jobController.save(job);
        return ResponseEntity.ok(job);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteJob(@RequestBody Job job) {
        if (validateID(job.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        jobController.delete(job);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/construction/{idUser}/{idBuilding}")
    @ResponseBody
    public ResponseEntity<?> putConstructionJob(@PathVariable("idUser") Integer idPlanet,
                                                @PathVariable("idBuilding") Integer idBuilding) {
        if (validateID(idPlanet) || validateID(idBuilding))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Job job = jobS.createConstructionYardJob(idPlanet, idBuilding);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/research/{idUser}/{idResearch}")
    @ResponseBody
    public ResponseEntity<?> putResearchJob(@PathVariable("idUser") Integer idUser,
                                            @PathVariable("idResearch") Integer idResearch) {
        if (validateID(idUser) || validateID(idResearch))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        Job job = jobS.createResearchJob(idUser, idResearch);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/shipyard/{idUser}/{idShipClass}/{amount}")
    @ResponseBody
    public ResponseEntity<?> putShipyardJob(@PathVariable("idUser") Integer idPlanet,
                                            @PathVariable("idShipClass") Integer idShipClass,
                                            @PathVariable("amount") Integer amount) {
        if (validateID(idPlanet) || validateID(idShipClass) || validateID(amount)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Job job = jobS.createShipyardJob(idPlanet, idShipClass, amount);
        return ResponseEntity.ok(job);
    }

    /**
     * fast check if ID could exist
     *
     * @param id input
     * @return <code>true</code> if invalid
     */
    private static boolean validateID(final Integer id) {
        return id == null || id <= 0;
    }
}
