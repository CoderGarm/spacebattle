package de.yuga.spacebattle.backend;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class ComparatorsContractTest {

    /**
     * Verify that a comparator is transitive.
     * todo This code validates compare(a,b) = - compare(b, c). It does not check that if compare(a,b) > 0 && compare(b,c) > 0 then compare(a,c) > 0
     *
     * @param <T>        the type being compared
     * @param comparator the comparator to test
     * @param elements   the elements to test against
     * @throws AssertionError if the comparator is not transitive
     */
    public static <T> void verifyTransitivity(Comparator<T> comparator, Collection<T> elements) {

        /**
         * The implementor must ensure that {@code sgn(compare(x, y)) ==
         * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
         * implies that {@code compare(x, y)} must throw an exception if and only
         * if {@code compare(y, x)} throws an exception.)<p>
         *
         * The implementor must also ensure that the relation is transitive:
         * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
         * {@code compare(x, z)>0}.<p>
         *
         * Finally, the implementor must ensure that {@code compare(x, y)==0}
         * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
         * {@code z}.<p>
         */

        // wenn x > y und y > z dann x > z todo check transitivity at a realistic runtime
        final int size = elements.size();
        final int amountOfRuns = size * size * size;
        System.out.println(size);
        final AtomicInteger counter = new AtomicInteger(size);
        final long start = Calendar.getInstance().getTimeInMillis();
        final AtomicLong timer = new AtomicLong(start);
        elements.forEach(first -> {

            print("first loop: ", counter, timer);

            elements.forEach(second -> {

                int firstGreaterThanSecond = comparator.compare(first, second);
                int result2 = comparator.compare(second, first);
                if (firstGreaterThanSecond != -result2) {
                    // Uncomment the following line to step through the failed case
                    //comparator.compare(first, second);
                    throw new AssertionError("compare(" + first + ", " + second + ") == " + firstGreaterThanSecond +
                            " but swapping the parameters returns " + result2);
                }

                if (firstGreaterThanSecond > 0) {
                    elements.forEach(third -> {

                        int secondGreaterThanThird = comparator.compare(second, third);
                        if (secondGreaterThanThird > 0) {
                            int firstGreaterThanThird = comparator.compare(first, third);
                            if (firstGreaterThanThird <= 0) {
                                // Uncomment the following line to step through the failed case
                                //comparator.compare(first, third);
                                throw new AssertionError("compare(" + first + ", " + second + ") > 0, " +
                                        "compare(" + second + ", " + third + ") > 0, but compare(" + first + ", " + third + ") == " +
                                        firstGreaterThanThird);
                            }
                        }
                    });
                }
            });
        });
        //runOldComparatorTest(comparator, elements);
    }

    private static void print(final String msg, final AtomicInteger counter, final AtomicLong timer) {
        counter.getAndDecrement();
        final long timeInMillis = Calendar.getInstance().getTimeInMillis();
        final long l = (timeInMillis - timer.get()) / 1000;
        System.out.println(msg + counter.get() + " in " + l + " seconds");
        timer.set(timeInMillis);
    }

    private static <T> void runOldComparatorTest(final Comparator<T> comparator, final Collection<T> elements) {
        for (T first : elements) {
            for (T second : elements) {
                int result1 = comparator.compare(first, second);
                int result2 = comparator.compare(second, first);
                if (result1 != -result2) {
                    // Uncomment the following line to step through the failed case
                    //comparator.compare(first, second);
                    throw new AssertionError("compare(" + first + ", " + second + ") == " + result1 +
                            " but swapping the parameters returns " + result2);
                }
            }
        }
        for (T first : elements) {
            for (T second : elements) {
                int firstGreaterThanSecond = comparator.compare(first, second);
                if (firstGreaterThanSecond <= 0)
                    continue;
                for (T third : elements) {
                    int secondGreaterThanThird = comparator.compare(second, third);
                    if (secondGreaterThanThird <= 0)
                        continue;
                    int firstGreaterThanThird = comparator.compare(first, third);
                    if (firstGreaterThanThird <= 0) {
                        // Uncomment the following line to step through the failed case
                        //comparator.compare(first, third);
                        throw new AssertionError("compare(" + first + ", " + second + ") > 0, " +
                                "compare(" + second + ", " + third + ") > 0, but compare(" + first + ", " + third + ") == " +
                                firstGreaterThanThird);
                    }
                }
            }
        }
    }

    /**
     * Prevent construction.
     */
    private ComparatorsContractTest() {
    }
}
