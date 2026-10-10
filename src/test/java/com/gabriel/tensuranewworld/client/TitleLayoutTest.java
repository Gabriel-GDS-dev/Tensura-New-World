package com.gabriel.tensuranewworld.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TitleLayoutTest {
    @Test void allFourActionsStayInsideThePanelAtMinecraftGuiScales() {
        for (int[] size : new int[][]{{320, 180}, {427, 240}, {640, 360}, {854, 480}, {1280, 720}}) {
            var layout = TitleLayout.fit(size[0], size[1]);
            assertTrue(layout.left() >= 16);
            assertTrue(layout.left() + layout.width() <= size[0] - 16);
            assertTrue(layout.menuTop() >= 65);
            assertTrue(layout.menuTop() + 4 * layout.buttonHeight() + 3 * layout.gap() <= size[1] - 25);
        }
    }
}
