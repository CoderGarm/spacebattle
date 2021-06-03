package de.yuga.spacebattle.gui.vaadin.orbitals.starmap;

import com.vaadin.flow.component.svg.elements.AbstractPolyElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ViewBoxDefinitionTest {

    @Test
    public void rotatePointOffset() {
        // prepare stuff
        final double x = 3;
        final double y = 2;
        // test method
        final AbstractPolyElement.PolyCoordinatePair result = ViewBoxDefinition.rotatePointOffset(x, 0, y, 0, 45);
        // check expectation
        final BigDecimal resultX = BigDecimal.valueOf(result.getPolyX()).setScale(2, RoundingMode.UP);
        final BigDecimal resultY = BigDecimal.valueOf(result.getPolyY()).setScale(2, RoundingMode.UP);
        Assert.assertEquals(resultX, new BigDecimal("0.71"));
        Assert.assertEquals(resultY, new BigDecimal("3.54"));
    }

    @Test
    public void testSquareRootMath() {
        // prepare stuff
        final double expectation = 1 / Math.sqrt(2);
        // test method
        final double result = Math.cos(Math.toRadians(45));
        // check expectation
        final BigDecimal resultBD = new BigDecimal(result).setScale(8, RoundingMode.FLOOR);
        final BigDecimal expectationBD = new BigDecimal(expectation).setScale(8, RoundingMode.FLOOR);
        Assert.assertEquals(resultBD, expectationBD);
    }
}