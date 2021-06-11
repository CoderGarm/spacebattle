package de.yuga.spacebattle.gui.vaadin.misc.details.misc;

import de.yuga.spacebattle.backend.enums.EResolution;

import javax.annotation.Nonnull;

/**
 * Just a mapping interface for simple usage of {@link ImageContainer}.
 */
public interface ImageMapper {

    /**
     * Returns the alternative text.
     *
     * @return the alternative text
     */
    String getAlternativeText();

    /**
     * Returns the title text.
     *
     * @return the title text
     */
    String getTitleText();

    /**
     * Returns the java-resource-path where the image is located.
     *
     * @param resolution the resolution of the requested image
     * @return the java-path
     */
    String getPath(@Nonnull final EResolution resolution);
}
