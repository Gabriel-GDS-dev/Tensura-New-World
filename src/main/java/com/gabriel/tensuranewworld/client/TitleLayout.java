package com.gabriel.tensuranewworld.client;

record TitleLayout(int left, int width, int menuTop, int buttonHeight, int gap) {
    static TitleLayout fit(int width, int height) {
        int panelWidth = Math.min(286, Math.max(120, width - 32));
        int left = width > 580 ? width - panelWidth - 36 : (width - panelWidth) / 2;
        int buttonHeight = height < 240 ? 17 : height < 280 ? 22 : 27;
        int gap = height < 240 ? 4 : 7;
        int menuHeight = 4 * buttonHeight + 3 * gap;
        int top = Math.min(Math.max(82, height / 2 - 5), height - menuHeight - 25);
        return new TitleLayout(left, panelWidth, top, buttonHeight, gap);
    }
}
