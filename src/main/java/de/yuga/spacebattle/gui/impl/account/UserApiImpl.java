package de.yuga.spacebattle.gui.impl.account;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;
import java.util.List;

@RestController
@RequestMapping(value = "/sb/user")
public class UserApiImpl {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserApiImpl.class);

    @Nonnull
    private final UserRepository userC;

    @Autowired
    public UserApiImpl(@Nonnull final UserRepository userC) {
        Preconditions.checkNotNull(userC, "userC shouldn't be null!");

        this.userC = userC;
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getUser() {
        final List<User> all = userC.findAllUsers();
        return ResponseEntity.ok(all);
    }

    @GetMapping("{idUser}")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable("idUser") final int idUser) {
        User user = userC.findById(idUser).get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<?> putUser(@RequestBody User user) {
        if (user.getId() < -1 || user.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        userC.save(user);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteUser(@RequestBody User user) {
        if (user.getId() < -1 || user.getId() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        userC.delete(user);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
