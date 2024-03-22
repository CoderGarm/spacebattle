package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class ManeuverElements implements Cloneable {

    @Nonnull
    private final Set<ManeuverElement> maneuverElements = new HashSet<>();

    public void add(@Nonnull final CubicBezier cubicBezier) {
        Preconditions.checkNotNull(cubicBezier, "cubicBezier must not be empty");

        maneuverElements.add(new ManeuverElement(cubicBezier, maneuverElements.size() + 1));
    }

    public double getTotalLength() {
        return getManeuverElements().stream().mapToDouble(me -> me.getCurve().getLength()).sum();
    }

    @Nonnull
    public List<ManeuverElement> getManeuverElements() {
        return maneuverElements.stream()
                .sorted(Comparator.comparingInt(ManeuverElement::getSequenceNo))
                .collect(Collectors.toList());
    }

    @Override
    public ManeuverElements clone() {
        try {
            final ManeuverElements clone = (ManeuverElements) super.clone();
            clone.maneuverElements.clear();
            clone.maneuverElements.addAll(maneuverElements.stream().map(ManeuverElement::clone).collect(Collectors.toSet()));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Nonnull
    public ManeuverElement getInitial() {
        return new ArrayList<>(maneuverElements).get(0);
    }
}
