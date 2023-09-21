package de.yuga.spacebattle.backend.entities.account;

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
    private User user;

    @Nullable
    @Size(min = 3, max = 50)
    private String title;

    @Nullable
    @Size(min = 3, max = 8)
    private String titleAbbreviation;

    @Nullable
    @Size(min = 3, max = 50)
    private String firstname;

    @Nullable
    @Size(min = 3, max = 50)
    private String surname;

    @Nullable
    @Size(min = 3, max = 6)
    private String shipPrefix;

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

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getTitleAbbreviation() {
        return titleAbbreviation;
    }

    @Nullable
    public String getFirstname() {
        return firstname;
    }

    @Nullable
    public String getSurname() {
        return surname;
    }

    @Nullable
    public String getShipPrefix() {
        return shipPrefix;
    }

    @Nonnull
    public Set<EStarNation> getShipNameTemplates() {
        return shipNameTemplates;
    }

    @Nonnull
    public Set<String> getShipNames() {
        return shipNames;
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
