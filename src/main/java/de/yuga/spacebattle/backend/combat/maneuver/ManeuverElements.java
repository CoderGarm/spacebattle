package de.yuga.spacebattle.backend.combat.maneuver;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.geometry.CubicBezier;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator.MC_HU;

public class ManeuverElements implements Cloneable {

    @Nonnull
    private final Set<ManeuverElement> maneuverElements = new HashSet<>();

    public void set(@Nonnull final CubicBezier cubicBezier) {
        Preconditions.checkNotNull(cubicBezier, "cubicBezier must not be empty");

        maneuverElements.add(new ManeuverElement(cubicBezier, 100, 1));
        validate();
    }

    public void withTransferManeuver(@Nonnull final CubicBezier cubicBezier) {
        Preconditions.checkNotNull(cubicBezier, "cubicBezier must not be empty");

        getManeuverElements().forEach(ManeuverElement::increaseSequenceNo);
        maneuverElements.add(new ManeuverElement(cubicBezier, -1, 1));

        final double totalLength = getTotalLength();

        getManeuverElements().forEach(me -> {
            final BigDecimal length = BigDecimal.valueOf(me.getCurve().getLength());
            final BigDecimal total = BigDecimal.valueOf(totalLength);
            final int percent = length.divide(total, MC_HU).multiply(BigDecimal.valueOf(100)).intValue();
            me.setPartOfManeuver(percent);
        });

        validate();
    }

    public double getTotalLength() {
        return getManeuverElements().stream().mapToDouble(me -> me.getCurve().getLength()).sum();
    }

    public void validate() {
        // per element one percent flooring deviation is allowed
        if (Math.abs(maneuverElements.stream().mapToInt(ManeuverElement::getPartOfManeuver).sum() - 100) > maneuverElements.size()) {
            throw new NotifyWebUserException("Please fill me up!");
        }
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
    public ManeuverElement getManeuverForPart(final double percent) {
        Preconditions.checkArgument(0 <= percent && percent <= 100, "percent must be between 0 and 100 included.");

        int summedUp = 0;
        ManeuverElement result = null;
        for (int i = 0; i < getManeuverElements().size(); i++) {
            final ManeuverElement m = getManeuverElements().get(i);

            summedUp += m.getPartOfManeuver();
            if (percent <= (summedUp + maneuverElements.size())) {
                result = m;
            }
        }

        Preconditions.checkNotNull(result, "result must not be empty");
        return result;
    }
}
