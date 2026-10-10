package com.gabriel.tensuranewworld.client;

/** Shared bounds for rendered rows and their clickable controls, in GUI pixels. */
final class RaphaelViewport {
    private RaphaelViewport() { }

    static int rows(int top, int bottom, int stride, int rowHeight) {
        return bottom - top < rowHeight ? 0 : 1 + (bottom - top - rowHeight) / stride;
    }

    static int clamp(int offset, int count, int visibleRows) {
        return Math.max(0, Math.min(offset, Math.max(0, count - Math.max(1, visibleRows))));
    }
}
