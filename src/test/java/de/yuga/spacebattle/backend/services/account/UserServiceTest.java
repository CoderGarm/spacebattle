package de.yuga.spacebattle.backend.services.account;

import de.yuga.spacebattle.BaseTestCase;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
public class UserServiceTest extends BaseTestCase {

    @Mock
    private UserRepository userRepositoryMock;

    private UserService testObject;

    @BeforeEach
    public void beforeClass() {
        testObject = new UserService(userRepositoryMock);
    }

    @Test
    public void testFindAll() {
        // prepare stuff
        final ArrayList<User> userList = new ArrayList<>();
        userList.add(createUser(1));
        userList.add(createUser(2));
        // prepare mocks
        when(userRepositoryMock.findAllUsers()).thenReturn(userList);
        // test method
        final List<User> result = testObject.findAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, userList);

    }

    private User createUser(int i) {
        final User user = new User();
        ReflectionTestUtils.setField(user, "id", i);
        return user;
    }

    @Test
    public void testFindWithResult() {
        // prepare stuff
        final int idUser = 1;
        final User user = createUser(idUser);
        final Optional<User> optionalUser = Optional.of(user);
        // prepare mocks
        when(userRepositoryMock.findById(idUser)).thenReturn(optionalUser);
        // test method
        final User result = testObject.find(idUser);
        // check expectation
        assertNotNull(result);
        assertSame(result, user);
    }

    @Test
    public void testFindWithoutResult() {
        // prepare stuff
        final int idUser = 1;
        final Optional<User> optionalUser = Optional.empty();
        // prepare mocks
        when(userRepositoryMock.findById(idUser)).thenReturn(optionalUser);
        // test method
        final User result = testObject.find(idUser);
        // check expectation
        assertNull(result);
    }

    @Test
    public void testSave() {
        // prepare stuff
        User user = createUser(1);
        // prepare mocks
        when(userRepositoryMock.save(user)).thenReturn(user);
        // test method
        final User result = testObject.save(user);
        // check expectation
        assertNotNull(result);
        assertEquals(result, user);
    }

    @Test
    public void testFindByUsernameAndEmail() {
        // prepare stuff
        final String username = "user";
        final String email = "email";
        final User user = createUser(1);
        user.setUsername(username);
        user.setEmail(email);
        // prepare mocks
        when(userRepositoryMock.findByUsernameAndEmail(username, email)).thenReturn(user);
        // test method
        final User result = testObject.findByUsernameAndEmail(username, email);
        // check expectation
        assertNotNull(result);
        assertEquals(result, user);
    }

    @Test
    public void testFindByUsernameAndEmailWithoutResult() {
        // prepare stuff
        final String username = "user";
        final String email = "email";
        // prepare mocks
        when(userRepositoryMock.findByUsernameAndEmail(username, email)).thenReturn(null);
        // test method
        final User result = testObject.findByUsernameAndEmail(username, email);
        // check expectation
        assertNull(result);
    }

    @Test
    public void testCreateUser() {
        // prepare stuff
        final String username = "user";
        final String password = "password";
        final String email = "email";
        final User user = new User(username, password, email, EWebUserRole.USER, false);
        // prepare mocks
        when(userRepositoryMock.save(user)).thenReturn(user);
        // test method
        final User result = testObject.createUser(username, password, email, EWebUserRole.USER, EGameUserRole.ALLIANCE_ADMIN);
        // check expectation
        assertNotNull(result);
        assertEquals(result, user);
    }
}
