package com.gabriel.tensuranewworld.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class RaphaelViewportTest {
    @Test
    void includesOnlyRowsWhoseTextAndButtonFit() {
        assertEquals(0, RaphaelViewport.rows(121, 140, 31, 20));
        assertEquals(1, RaphaelViewport.rows(121, 141, 31, 20));
        assertEquals(5, RaphaelViewport.rows(121, 265, 31, 20));
        for (int height : new int[] {180, 240, 270, 360, 500}) {
            int top = 131;
            int bottom = height - 26;
            int rows = RaphaelViewport.rows(top, bottom, 31, 20);
            for (int row = 0; row < rows; row++) {
                assertTrue(top + row * 31 + 20 <= bottom);
            }
        }
    }

    @Test
    void scrollingReachesEveryAttributeWithoutEmptyPages() {
        int offset = RaphaelViewport.clamp(999, 5, 2);
        assertEquals(3, offset);
        assertEquals(5, offset + 2);
        assertEquals(0, RaphaelViewport.clamp(-1, 5, 2));
        assertEquals(0, RaphaelViewport.clamp(10, 5, 5));
        assertEquals(0, RaphaelViewport.clamp(10, 0, 3));
    }

    @Test
    void resizedFormListClampsToLastFullPage() {
        assertEquals(8, RaphaelViewport.clamp(18, 20, 12));
        assertEquals(0, RaphaelViewport.clamp(18, 3, 12));
        assertEquals(19, RaphaelViewport.clamp(100, 20, 0));
    }
}
