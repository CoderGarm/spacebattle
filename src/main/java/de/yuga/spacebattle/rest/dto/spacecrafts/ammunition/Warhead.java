package de.yuga.spacebattle.rest.dto.spacecrafts.ammunition;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWarheadType;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public class Warhead {

    @ApiModelProperty(required = true, value = "The id of this warhead.")
    private int idWarhead;

    @Nonnull
    @ApiModelProperty(required = true, value = "The type name of this warhead.")
    private String typeName;

    @ApiModelProperty(required = true, value = "The projected damage of this warhead.")
    private long damageValue;

    /**
     * Defines the range of this weapon in meter.
     */
    @ApiModelProperty(required = true, value = "The effective range of this warhead.")
    private BigDecimal damageProjectionRange;

    /**
     * The way of damage projection.
     */
    @Nonnull
    @ApiModelProperty(required = true, value = "The warhead type.")
    private EWarheadType warheadType;

    @ApiModelProperty(required = true, value = "The used capacity of this warhead.")
    private int useCapacity;

    public Warhead() {
    }

    public Warhead(@Nonnull final de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Warhead warhead) {
        Preconditions.checkNotNull(warhead, "warhead shouldn't be null!");

        this.idWarhead = warhead.getId();
        this.typeName = warhead.getTypeName();
        this.damageValue = warhead.getDamageValue();
        this.damageProjectionRange = warhead.getDamageProjectionRange();
        this.warheadType = warhead.getWarheadType();
        this.useCapacity = warhead.getUseCapacity();
    }

    public int getIdWarhead() {
        return idWarhead;
    }

    public void setIdWarhead(int idWarhead) {
        this.idWarhead = idWarhead;
    }

    @Nonnull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(@Nonnull String typeName) {
        this.typeName = typeName;
    }

    public long getDamageValue() {
        return damageValue;
    }

    public void setDamageValue(long damageValue) {
        this.damageValue = damageValue;
    }

    public BigDecimal getDamageProjectionRange() {
        return damageProjectionRange;
    }

    public void setDamageProjectionRange(BigDecimal damageProjectionRange) {
        this.damageProjectionRange = damageProjectionRange;
    }

    @Nonnull
    public EWarheadType getWarheadType() {
        return warheadType;
    }

    public void setWarheadType(@Nonnull EWarheadType warheadType) {
        this.warheadType = warheadType;
    }

    public int getUseCapacity() {
        return useCapacity;
    }

    public void setUseCapacity(int useCapacity) {
        this.useCapacity = useCapacity;
    }
}
