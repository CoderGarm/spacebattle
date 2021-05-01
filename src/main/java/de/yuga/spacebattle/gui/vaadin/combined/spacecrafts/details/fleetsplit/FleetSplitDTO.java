package de.yuga.spacebattle.gui.vaadin.combined.spacecrafts.details.fleetsplit;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * DTO to hold a by-splitting-another-fleet created fleet.
 */
public class FleetSplitDTO {

    /**
     * the base fleet which has to be splitted.
     */
    @Nonnull
    private final Fleet baseFleet;

    /**
     * The new fleets name.
     */
    @Nullable
    private String name;

    /**
     * The ship classes which has to divided between the fleets.
     */
    @Nonnull
    private List<ShipClassCountSplitDTO> ships;

    public FleetSplitDTO(@Nonnull final Fleet baseFleet) {
        Preconditions.checkNotNull(baseFleet, "baseFleet shouldn't be null!");

        this.baseFleet = baseFleet;
        this.ships = baseFleet.getShips().entrySet()
                .stream()
                .map(e -> new ShipClassCountSplitDTO(e.getKey(), e.getValue()))
                .sorted(new ShipClassCountSplitDTOComparator())
                .collect(Collectors.toList());
    }

    @Nonnull
    public Fleet getBaseFleet() {
        return baseFleet;
    }

    @Nonnull
    public String getBaseFleetName() {
        return baseFleet.getName();
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
    }

    public void setShips(@Nonnull final List<ShipClassCountSplitDTO> ships) {
        Preconditions.checkNotNull(ships, "ships shouldn't be null!");

        this.ships = ships;
    }

    public void setAmount(@Nonnull final ShipClassCountSplitDTO shipClassCountSplitDTO, final int amount) {
        Preconditions.checkNotNull(shipClassCountSplitDTO, "shipClassCountSplitDTO shouldn't be null!");

        ships.stream().filter(shipClassCountSplitDTO::equals).findFirst().ifPresent(dto -> dto.setSplitCount(amount));
    }

    public int getAmount(@Nonnull final ShipClassCountSplitDTO shipClassCountSplitDTO) {
        Preconditions.checkNotNull(shipClassCountSplitDTO, "shipClassCountSplitDTO shouldn't be null!");

        ShipClassCountSplitDTO dto = ships.stream().filter(shipClassCountSplitDTO::equals).findFirst().orElse(null);
        if (dto == null) {
            return 0;
        }
        return dto.getSplitCount();
    }

    @Nonnull
    public List<ShipClassCountSplitDTO> getShips() {
        return ships;
    }

    /**
     * Returns the result of splitting a fleet.
     *
     * @return fleet array, first value is modified base fleet, second argument is splitted fleet
     */
    @Nonnull
    public Fleet[] getSplitResult() {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkState(baseFleet.getOrbit() != null, "baseFleet's orbit shouldn't be null!");

        final Map<ShipClass, Integer> splitFleetSetup = ships.stream()
                .collect(Collectors.toMap(ShipClassCountSplitDTO::getShipClass, ShipClassCountSplitDTO::getSplitCount));

        splitFleetSetup.forEach((shipClass, integer) -> baseFleet.updateShips(shipClass, (-1 * integer)));

        assert baseFleet.getOrbit() != null; // already checked but to remove idea warning
        final Fleet splitFleet = new Fleet(name, baseFleet.getOwner(), baseFleet.getOrbit());
        splitFleetSetup.forEach(splitFleet::updateShips);

        return new Fleet[]{baseFleet, splitFleet};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FleetSplitDTO)) return false;

        FleetSplitDTO that = (FleetSplitDTO) o;

        if (!baseFleet.equals(that.baseFleet)) return false;
        if (!Objects.equals(name, that.name)) return false;
        return ships.equals(that.ships);
    }

    @Override
    public int hashCode() {
        int result = baseFleet.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + ships.hashCode();
        return result;
    }
}
