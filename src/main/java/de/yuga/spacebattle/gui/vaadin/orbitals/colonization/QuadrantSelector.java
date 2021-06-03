package de.yuga.spacebattle.gui.vaadin.orbitals.colonization;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Displays four checkboxes and offers a data model to fetch the user input.
 */
public class QuadrantSelector extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<QuadrantSelector, QuadrantSelector.QuadrantSelectorValidator>, QuadrantSelector.QuadrantSelectorValidator> {

    /**
     * The transfer object which offers the opportunity to check if a given orbit is covered by the quadrants selection.
     */
    public static class QuadrantSelectorValidator {

        /**
         * The quadrants selection.
         */
        @Nonnull
        private final Set<Quadrant> quadrants;

        public QuadrantSelectorValidator(@Nonnull final Set<Quadrant> quadrants) {
            Preconditions.checkNotNull(quadrants, "quadrants shouldn't be null!");

            this.quadrants = quadrants;
        }

        /**
         * Checks if a given orbit is part of this quadrants definition.
         *
         * @param orbit the orbit to validate
         * @return <code>true</code> if the orbit is part of this definition, <code>false</code> otherwise
         */
        public boolean contains(@Nonnull final Orbit orbit) {
            Preconditions.checkNotNull(orbit, "orbit shouldn't be null!");

            final int xCoordinate = orbit.getXCoordinate();
            final int yCoordinate = orbit.getYCoordinate();

            int signumX = Integer.signum(xCoordinate);
            int signumY = Integer.signum(yCoordinate);
            final Quadrant toCheck = Quadrant.getBySignum(signumX, signumY);
            return quadrants.contains(toCheck);
        }

        /**
         * Simply a getter to use this as dto.
         *
         * @return the set of quadrants
         */
        @Nonnull
        public Set<Quadrant> getQuadrants() {
            return quadrants;
        }
    }

    /**
     * A quadrant enum which will transform the 'computer coordinates' to human readable quadrants.
     */
    public enum Quadrant {
        /**
         * mathematical quadrants based on browser coordinates:
         * Q4, Q1
         * Q3, Q2
         */
        Q1(1, -1),
        Q2(1, 1),
        Q3(-1, 1),
        Q4(-1, -1);

        private final int signumX;
        private final int signumY;

        Quadrant(int signumX, int signumY) {
            this.signumX = signumX;
            this.signumY = signumY;
        }

        public int getSignumX() {
            return signumX;
        }

        public int getSignumY() {
            return signumY;
        }

        /**
         * Returns a quadrant by the signum of a coordinate set.
         *
         * @param signumX the x signum
         * @param signumY the y signum
         * @return the corresponding quadrant
         */
        @Nonnull
        public static Quadrant getBySignum(final int signumX, final int signumY) {
            return Objects.requireNonNull(Arrays.stream(Quadrant.values()).filter(q -> q.getSignumX() == signumX && q.getSignumY() == signumY).findFirst().orElse(null));
        }
    }

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<QuadrantSelector, QuadrantSelectorValidator>> valueChangeListener;

    @Nonnull
    private final Checkbox q1 = new Checkbox("Quadrant 1", event -> fireEvent());

    @Nonnull
    private final Checkbox q2 = new Checkbox("Quadrant 2", event -> fireEvent());

    @Nonnull
    private final Checkbox q3 = new Checkbox("Quadrant 3", event -> fireEvent());

    @Nonnull
    private final Checkbox q4 = new Checkbox("Quadrant 4", event -> fireEvent());

    public QuadrantSelector() {
        final HorizontalLayout h1 = new HorizontalLayout();
        h1.add(q4, q1);
        final HorizontalLayout h2 = new HorizontalLayout();
        h2.add(q3, q2);

        q1.setValue(true);
        q2.setValue(true);
        q3.setValue(true);
        q4.setValue(true);

        add(h1, h2);
    }

    @Override
    public void setValue(QuadrantSelectorValidator value) {
        if (value == null) {
            q1.setValue(false);
            q2.setValue(false);
            q1.setValue(false);
            q1.setValue(false);
            return;
        }
        final Set<Quadrant> quadrants = value.getQuadrants();
        q1.setValue(quadrants.contains(Quadrant.Q1));
        q2.setValue(quadrants.contains(Quadrant.Q2));
        q1.setValue(quadrants.contains(Quadrant.Q3));
        q1.setValue(quadrants.contains(Quadrant.Q4));
    }

    @Nonnull
    @Override
    public QuadrantSelectorValidator getValue() {
        final Set<Quadrant> objects = new HashSet<>();
        if (q1.getValue()) {
            objects.add(Quadrant.Q1);
        }
        if (q2.getValue()) {
            objects.add(Quadrant.Q2);
        }
        if (q3.getValue()) {
            objects.add(Quadrant.Q3);
        }
        if (q4.getValue()) {
            objects.add(Quadrant.Q4);
        }
        return new QuadrantSelectorValidator(objects);
    }

    private void fireEvent() {
        if (valueChangeListener != null) {
            valueChangeListener.valueChanged(new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true));
        }
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<QuadrantSelector, QuadrantSelectorValidator>> listener) {
        valueChangeListener = listener;
        return (Registration) () -> valueChangeListener = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        q1.setReadOnly(readOnly);
        q2.setReadOnly(readOnly);
        q3.setReadOnly(readOnly);
        q4.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {

    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }
}
