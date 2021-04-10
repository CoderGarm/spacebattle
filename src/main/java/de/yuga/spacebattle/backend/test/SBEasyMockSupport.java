package de.yuga.spacebattle.backend.test;

import org.easymock.EasyMockSupport;
import org.easymock.IMocksControl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * Generic mock test which holds standard mock methods.
 */
public abstract class SBEasyMockSupport extends EasyMockSupport {

    /**
     * List of all temporary controls created.
     */
    private Set<IMocksControl> tempControls = new HashSet<>(5);

    /**
     * The flag, whether a mock is a method mock or not. Method mocks will be cleared after the method.
     */
    private boolean isMethodMock = false;

    /**
     * Clear mocks.
     */
    @AfterClass
    protected final void clearMocksEasyMockSupport() {
        controls.clear();
        tempControls = null;
    }

    /**
     * Activates all upcoming mocks as method mocks.
     */
    @BeforeMethod
    protected final void setUpEasyMockSupport() {
        isMethodMock = true;
    }

    /**
     * Reset flag and clear temporary mocks.
     */
    @AfterMethod
    protected final void cleanUpEasyMockSupport() {
        // remove all temp controls from the controls
        controls.removeAll(tempControls);
        tempControls.clear();

        isMethodMock = false;
        resetAll();
    }

    /**
     * Replay mocks.
     */
    protected final void replayMocks() {
        replayAll();
    }

    /**
     * Verifies mocks.
     */
    protected final void verifyMocks() {
        verifyAll();
    }

    @Override
    @Nonnull
    public IMocksControl createControl() {
        final IMocksControl control = super.createControl();

        if (isMethodMock) {
            tempControls.add(control);
        }

        return control;
    }
}
