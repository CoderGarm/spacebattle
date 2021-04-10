package de.yuga.spacebattle.backend.test;

import com.google.common.base.Preconditions;
import org.easymock.EasyMock;
import org.easymock.cglib.proxy.Callback;
import org.easymock.cglib.proxy.Factory;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Listeners;

import javax.annotation.Nonnull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Proxy.isProxyClass;

/**
 * Helper for tests that do not use any mocks and should verify that the mock of the class are not accidently used.
 * <p>
 * The listener will replay and verify all mock objects that are stored in fields of the test except if the name of the field is {@code testObject}.
 * <p>
 * Annotate the test class with {@link MocksNotUsedTestListener} and the test cases not using any mocks with
 * {@link MocksNotUsed}.
 * <p>
 * Example test using the listener:
 * <pre>
 * <code>
 *
 * {@literal @}{@link Listeners}({{@link MocksNotUsedTestListener}.class})
 * class SomeClassTest {
 *
 *     {@literal @}Test
 *     public void testUsingMocks() {
 *         {@link EasyMock#replay replay}(...);
 *         ...
 *         {@link EasyMock#verify verify}(...);
 *     }
 *
 *     {@literal @}Test
 *     {@literal @}{@link MocksNotUsed MocksNotUsed}
 *     public void testNotUsingMocks() {
 *         ...
 *     }
 *
 * }
 * </code>
 * </pre>
 */
public class MocksNotUsedTestListener extends TestListenerAdapter {

    /**
     * Annotation for test methods that do not use any mocks of the test class.
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MocksNotUsed {

    }

    /**
     * The mock objects from the fields of the test class.
     *
     * @param result The test information from TestNG.
     * @return A list of mock objects.
     */
    @Nonnull
    private List<Object> getMocks(@Nonnull final ITestResult result) {
        final List<Object> mocks = new ArrayList<Object>();
        for (final Field field : result.getTestClass().getRealClass().getDeclaredFields()) {
            if ("testObject".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            final Object fieldValue;
            try {
                fieldValue = field.get(result.getInstance());
            } catch (final IllegalAccessException e) {
                continue;
            }
            if (fieldValue == null) {
                continue;
            }
            if (fieldValue instanceof Factory) {
                boolean isMock = false;
                for (final Callback callback : ((Factory) fieldValue).getCallbacks()) {
                    if (!(callback.getClass().getName().startsWith("org.easymock."))) {
                        continue;
                    }
                    isMock = true;
                    break;
                }
                if (!isMock) {
                    continue;
                }
            } else if (!isProxyClass(fieldValue.getClass())) {
                continue;
            }
            mocks.add(fieldValue);
        }
        return mocks;
    }

    /**
     * Check if the test method has the {@link MocksNotUsed} annotation.
     *
     * @param result The test information from TestNG.
     * @return {@code true} if the method has the annotation, {@code false} otherwise.
     */
    private boolean isNotUsingMocks(@Nonnull final ITestResult result) {
        final Method method = result.getMethod().getConstructorOrMethod().getMethod();
        // noinspection SimplifiableIfStatement
        if (method == null) {
            return false;
        }
        return method.getAnnotation(MocksNotUsed.class) != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Replay all mocks in fields of the test class.
     */
    @Override
    public void onTestStart(@Nonnull final ITestResult result) {
        Preconditions.checkNotNull(result, "result must not be null.");

        if (!isNotUsingMocks(result)) {
            return;
        }
        final List<Object> mocks = getMocks(result);
        for (final Object mock : mocks) {
            EasyMock.replay(mock);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Verify all mocks in fields of the test class.
     */
    @Override
    public void onTestSuccess(@Nonnull final ITestResult result) {
        Preconditions.checkNotNull(result, "result must not be null.");

        if (!isNotUsingMocks(result)) {
            return;
        }
        final List<Object> mocks = getMocks(result);
        for (final Object mock : mocks) {
            try {
                EasyMock.verify(mock);
            } catch (final Throwable t) {
                result.setStatus(ITestResult.FAILURE);
                result.setThrowable(t);
                return;
            }
        }
    }

}
