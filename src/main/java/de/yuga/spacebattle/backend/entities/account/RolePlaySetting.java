package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.EStarNationConverter;
import de.yuga.spacebattle.backend.converter.EStringSetConverter;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EStarNation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rolePlaySetting")
@AttributeOverride(name = "id", column = @Column(name = "idRolePlaySetting"))
public class RolePlaySetting extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @JoinColumn(name = "idUser")
    @OneToOne(optional = false)
    private Owner user;

    @Nullable
    @Size(max = 50)
    private String title;

    @Nullable
    @Size(max = 8)
    private String titleAbbreviation;

    @Nullable
    @Size(max = 50)
    private String firstname;

    @Nullable
    @Size(max = 50)
    private String surname;

    @Nullable
    @Size(max = 6)
    private String shipPrefix;

    @Nullable
    @Size(max = 50)
    private String empireName;

    @Nonnull
    @NotNull
    @Convert(converter = EStarNationConverter.class)
    private Set<EStarNation> shipNameTemplates = new HashSet<>();

    @Lob
    @Nonnull
    @Convert(converter = EStringSetConverter.class)
    private Set<String> shipNames = new HashSet<>();

    public RolePlaySetting() {
    }

    public RolePlaySetting(@Nonnull final Owner user) {
        this.user = Preconditions.checkNotNull(user, "user must not be empty");
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable final String title) {
        this.title = title;
    }

    @Nullable
    public String getTitleAbbreviation() {
        return titleAbbreviation;
    }

    public void setTitleAbbreviation(@Nullable final String titleAbbreviation) {
        this.titleAbbreviation = titleAbbreviation;
    }

    @Nullable
    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(@Nullable final String firstname) {
        this.firstname = firstname;
    }

    @Nullable
    public String getSurname() {
        return surname;
    }

    public void setSurname(@Nullable final String surname) {
        this.surname = surname;
    }

    @Nullable
    public String getShipPrefix() {
        return shipPrefix;
    }

    public void setShipPrefix(@Nullable final String shipPrefix) {
        this.shipPrefix = shipPrefix;
    }

    @Nonnull
    public Set<EStarNation> getShipNameTemplates() {
        return shipNameTemplates;
    }

    public void setShipNameTemplates(@Nonnull final Set<EStarNation> shipNameTemplates) {
        this.shipNameTemplates = shipNameTemplates;
    }

    @Nonnull
    public Set<String> getShipNames() {
        return shipNames;
    }

    public void setShipNames(@Nonnull final Set<String> shipNames) {
        this.shipNames = shipNames;
    }

    @Nullable
    public String getEmpireName() {
        return empireName;
    }

    public void setEmpireName(@Nullable final String empireName) {
        this.empireName = empireName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolePlaySetting)) return false;

        RolePlaySetting that = (RolePlaySetting) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return 31 * id;
    }
}
