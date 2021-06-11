package de.yuga.spacebattle.gui.vaadin.misc;

import com.flowingcode.vaadin.addons.carousel.Carousel;
import com.flowingcode.vaadin.addons.carousel.Slide;
import com.google.common.base.Preconditions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Workhorse to create a simple statistics section.
 */
public class StatisticsDisplay extends VerticalLayout {

    /**
     * Represents the statistics itself.
     */
    @Nullable
    private Component content;

    @Nonnull
    private final Map<Button, Component> slideSelector = new HashMap<>();

    @Nonnull
    private final Carousel carousel = new Carousel().withSlideDuration(4);

    public StatisticsDisplay() {
        setClassName("statsSelectorContainer");
        carousel.setId("statsSelectorMenu");
        carousel.setHeight("70px");
        carousel.setWidth("130px");

        carousel.addChangeListener(event -> {
            final String position = event.getPosition();
            final Carousel source = event.getSource();
            if (position != null) {
                // todo useless in 2.1.0 because of
                // https://github.com/FlowingCode/CarouselAddon/issues/10#issuecomment-884073469
                source.movePos(Integer.parseInt(position));
            }
        });

        add(carousel);
    }

    /**
     * Sets and relate a selector slide to it's corresponding component.
     * Will add a new slide every time.
     *
     * @param title     the slide's title
     * @param component the slide's component
     */
    public void addSlide(@Nonnull final String title, @Nonnull final Component component) {
        Preconditions.checkNotNull(title, "title shouldn't be null!");
        Preconditions.checkNotNull(component, "component shouldn't be null!");

        final List<Slide> knownSlides = Arrays.asList(carousel.getSlides());
        final Button button = new Button(title, event -> {
            Button source = event.getSource();
            Component component1 = slideSelector.get(source);
            setContent(component1);
        });
        final Slide slide = new Slide(button);
        final List<Slide> newSlides = new ArrayList<>(knownSlides);
        newSlides.add(slide);
        final Slide[] slides = newSlides.toArray(carousel.getSlides());
        carousel.setSlides(slides);
        slideSelector.put(button, component);
        if (slides.length == 1) {
            setContent(component);
        }
    }

    /**
     * Sets the content of the statistics element.
     *
     * @param content the content
     */
    public void setContent(@Nonnull final Component content) {
        Preconditions.checkNotNull(content, "content shouldn't be null!");

        if (this.content != null) {
            remove(this.content);
        }
        this.content = content;
        if (this.content instanceof HasStyle) {
            ((HasStyle) this.content).addClassName("sticky-second");
            //((HasStyle) this.content).addClassName("statsSelectorContainer");
        }
        add(this.content);
    }
}
