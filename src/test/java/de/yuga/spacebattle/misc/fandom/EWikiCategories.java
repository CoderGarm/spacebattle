package de.yuga.spacebattle.misc.fandom;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public enum EWikiCategories {

    PLANETS("Planets", "Planeten", "planets/"),
    SYSTEMS("Systems", "Sonnensysteme", "systems/"),
    ASTEROID_BELTS("Asteroid_Belts", null, "belts/"),
    SHIP_CLASSES("Spacecraft", "Raumschiffsklassen", "ship-classes/"),

    SYSTEMS_MANTICORE("Manticoran_Systems", "Manticoranische_Systeme", "systems/manticore/"),
    SYSTEMS_HAVEN("Havenite_Systems", "Havenitische_Systeme", "systems/haven/"),
    SYSTEMS_ANDERMAN("Andermani_Systems", "Andermanische_Systeme", "systems/anderman/"),
    SYSTEMS_SILESIA("Silesian_Star_Systems", "Silesianische_Systeme", "systems/silesia/"),
    SYSTEMS_SOLARIAN("Solarian_League_Systems", "Solarische_Systeme", "systems/solarian/"),
    SYSTEMS_GRAYSON("Grayson_Systems", "Graysonitische_Systeme", "systems/grayson/"),
    SYSTEMS_MESA(null, "Alignmentsysteme", "systems/mesa/"),

    WARSHIPS_MANTICORE("Naval_Ships_of_Manticore", "Manticoranische_Raumschiffe", "warships/manticore/"),
    WARSHIPS_HAVEN("Naval_Ships_of_Haven", "Havenitische_Raumschiffe", "warships/haven/"),
    WARSHIPS_ANDERMAN("Naval_Ships_of_the_Anderman_Empire", "Andermanische_Raumschiffe", "warships/anderman/"),
    WARSHIPS_SILESIA("Naval_Ships_of_Silesia", "Silesianische_Raumschiffe", "warships/silesia/"),
    WARSHIPS_SOLARIAN("Naval_Ships_of_the_Solarian_League", "Solarische_Raumschiffe", "warships/solarian/"),
    WARSHIPS_GRAYSON("Naval_Ships_of_the_Solarian_League", "Graysonitische_Raumschiffe", "warships/grayson/"),

    SHIP_CLASSES_MANTICORE("Manticoran_Ship_Classes", "Manticoranische_Schiffsklassen", "ship-classes/manticore/"),
    SHIP_CLASSES_HAVEN("Havenite_Ship_Classes", "Havenitische_Schiffsklassen", "ship-classes/haven/"),
    SHIP_CLASSES_ANDERMAN("Andermani_Ship_Classes", "Andermanische_Schiffsklassen", "ship-classes/anderman/"),
    SHIP_CLASSES_SILESIA("Silesian_Ship_Classes", "Silesianische_Schiffsklassen", "ship-classes/silesia/"),
    SHIP_CLASSES_SOLARIAN("Solarian_Ship_Classes", "Solarische_Schiffsklassen", "ship-classes/solarian/"),
    SHIP_CLASSES_GRAYSON("Grayson_Ship_Classes", "Graysonitische_Schiffsklassen", "ship-classes/grayson/"),
    ;

    @Nullable
    private final String categoryEN;

    @Nullable
    private final String categoryDE;

    @Nonnull
    private final String folder;

    EWikiCategories(@Nullable final String categoryEN, @Nullable final String categoryDE, @Nonnull final String folder) {
        this.categoryEN = categoryEN;
        this.categoryDE = categoryDE;
        this.folder = Preconditions.checkNotNull(folder, "folder must not be empty");
    }

    @Nullable
    public String getCategory(@Nonnull final EWikiConfig config) {
        Preconditions.checkNotNull(config, "config must not be empty");

        return config == EWikiConfig.EN ? categoryEN : categoryDE;
    }

    @Nonnull
    public String getFolder(@Nonnull final EWikiConfig config) {
        Preconditions.checkNotNull(config, "config must not be empty");

        return folder + config.getLanguage() + "/";
    }

    @Nonnull
    public static List<EWikiCategories> get() {
        return List.of(EWikiCategories.values());
    }
}
