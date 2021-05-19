package de.yuga.spacebattle.backend.services.account;

import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.repositories.account.UserRepository;
import de.yuga.spacebattle.backend.services.researches.ResearchService;
import de.yuga.spacebattle.backend.test.MocksNotUsedTestListener;
import de.yuga.spacebattle.backend.test.MocksNotUsedTestListener.MocksNotUsed;
import de.yuga.spacebattle.backend.test.SBEasyMockSupport;
import org.easymock.Mock;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.easymock.EasyMock.expect;
import static org.testng.Assert.*;

@Listeners({MocksNotUsedTestListener.class})
public class UserServiceTest extends SBEasyMockSupport {

    @Mock
    private UserRepository userRepositoryMock;

    @Mock
    private ResearchService researchServiceMock;

    private UserService testObject;

    @BeforeClass
    public void beforeClass() {
        injectMocks(this);
        testObject = new UserService(userRepositoryMock, researchServiceMock);
    }

    @AfterClass
    public void afterClass() {
        userRepositoryMock = null;
        researchServiceMock = null;
        testObject = null;
    }

    @AfterMethod
    public void afterMethod() {
        testObject = new UserService(userRepositoryMock, researchServiceMock);
    }

    @Test
    @MocksNotUsed
    public void testIsLoggedInWithoutLogin() {
        // test method
        final User result = testObject.getLoggedInUser();
        // check expectation
        assertNull(result);
    }

    @Test
    @MocksNotUsed
    public void testIsLoggedInWithLogin() {
        // prepare stuff
        final User login = new User();
        testObject.setLogin(login);
        // test method
        final User result = testObject.getLoggedInUser();
        // check expectation
        assertNotNull(result);
        assertSame(result, login);
    }

    @Test
    public void testRefresh() {
        // prepare stuff
        final int idUser = 1;
        final User user = new User();
        user.setId(idUser);
        final Optional<User> optionalUser = Optional.of(user);
        testObject.setLogin(user);
        // prepare mocks
        expect(userRepositoryMock.findById(idUser)).andReturn(optionalUser);
        // replay mocks
        replayAll();
        // test method
        final User result = testObject.refresh();
        // verify mocks
        verifyAll();
        // check expectation
        assertNotNull(result);
        final User loggedIn = testObject.getLoggedInUser();
        assertSame(result, user);
        assertSame(loggedIn, result);
    }

    @Test
    @MocksNotUsed
    public void testSetLogin() {
        // prepare stuff
        final User user = new User();
        testObject.setLogin(user);
        // test method
        final User result = testObject.getLoggedInUser();
        // check expectation
        assertNotNull(result);
        assertSame(result, user);
    }

    @Test
    @MocksNotUsed
    public void testLogin() {
        // prepare stuff
        final User user = createUser(1);
        // test method
        testObject.setLogin(user);
        // check expectation
        final User result = testObject.getLoggedInUser();
        assertNotNull(result);
        assertSame(result, user);
    }

    @Test
    public void testFindAll() {
        // prepare stuff
        final ArrayList<User> userList = new ArrayList<>();
        userList.add(createUser(1));
        userList.add(createUser(2));
        // prepare mocks
        expect(userRepositoryMock.findAllUsers()).andReturn(userList);
        // replay mocks
        replayAll();
        // test method
        final List<User> result = testObject.findAll();
        // verify mocks
        verifyAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, userList);

    }

    private User createUser(int i) {
        final User user = new User();
        user.setId(i);
        return user;
    }

    @Test
    public void testFindWithResult() {
        // prepare stuff
        final int idUser = 1;
        final User user = createUser(idUser);
        final Optional<User> optionalUser = Optional.of(user);
        // prepare mocks
        expect(userRepositoryMock.findById(idUser)).andReturn(optionalUser);
        // test method
        replayAll();
        final User result = testObject.find(idUser);
        // verify mocks
        verifyAll();
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
        expect(userRepositoryMock.findById(idUser)).andReturn(optionalUser);
        // test method
        replayAll();
        final User result = testObject.find(idUser);
        // verify mocks
        verifyAll();
        // check expectation
        assertNull(result);
    }

    @Test
    public void testSave() {
        // prepare stuff
        User user = createUser(1);
        // prepare mocks
        expect(userRepositoryMock.save(user)).andReturn(user);
        // test method
        replayAll();
        final User result = testObject.save(user);
        // verify mocks
        verifyAll();
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
        expect(userRepositoryMock.findByUsernameAndEmail(username, email)).andReturn(user);
        // test method
        replayAll();
        final User result = testObject.findByUsernameAndEmail(username, email);
        // verify mocks
        verifyAll();
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
        expect(userRepositoryMock.findByUsernameAndEmail(username, email)).andReturn(null);
        // test method
        replayAll();
        final User result = testObject.findByUsernameAndEmail(username, email);
        // verify mocks
        verifyAll();
        // check expectation
        assertNull(result);
    }

    @Test
    public void testCreateUser() {
        // prepare stuff
        final String username = "user";
        final String password = "password";
        final String email = "email";
        final User user = new User(username, password, email);
        // prepare mocks
        expect(userRepositoryMock.save(user)).andReturn(user);
        // test method
        replayAll();
        final User result = testObject.createUser(username, password, email);
        // verify mocks
        verifyAll();
        // check expectation
        assertNotNull(result);
        assertEquals(result, user);
    }
}