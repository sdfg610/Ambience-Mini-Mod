package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.ArrayList;

public class TabPanel extends AmWidget
{
    private static final int TAB_SEP = 3;

    public Color backgroundColor = Color.TRANSPARENT;

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private Tab selectedTab = null;
    private int tabsWidth = 0;

    private int maxIdleTabButtonHeight = 0;


    public TabPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public TabPanel(int width, int height) {
        this(0, 0, width, height);
    }


    public Tab createTab(String name) {
        Tab tab = new Tab(name);
        tabs.add(tab);
        if (selectedTab == null)
            selectedTab = tab;

        tabsWidth = tabs.stream().mapToInt(t -> t.btnWidth).sum() + TAB_SEP * (tabs.size()-1);
        maxIdleTabButtonHeight = Math.max(maxIdleTabButtonHeight, tab.btnHeight);

        return tab;
    }

    public int getButtonsHeight() {
        return selectedTab.btnHeight+Tab.PADDING_VERTICAL-1;
    }

    public int getMaxIdleTabButtonHeight() {
        return maxIdleTabButtonHeight;
    }

    public int getUsableHeight() {
        return height - getButtonsHeight();
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        int trueX = x+(width - tabsWidth)/2;
        for (var tab : tabs) {
            tab.renderButton(drawer, trueX, mouseX, mouseY);
            trueX += tab.btnWidth + TAB_SEP;
        }

        int btnHeight = getButtonsHeight();
        int trueY = y+btnHeight;
        renderRectangle(drawer, x, trueY, width, height-btnHeight, backgroundColor);
        renderBorder(drawer, x, trueY, width, height-btnHeight, 1, Color.OFF_WHITE);
        drawer.withOffset(x, trueY, 0, () -> selectedTab.renderBody(drawer, mouseX-x, mouseY-trueY));
    }

    @Override
    public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        int trueX = x+(width - tabsWidth)/2;
        for (var tab : tabs) {
            var horizontal = tab.getTrueHorizontal(tab == selectedTab);
            int trueY = horizontal.left();
            int trueHeight = horizontal.right();
            if (isMouseOver(mouseX, mouseY, trueX, trueY, trueX+tab.btnWidth, trueY+trueHeight-2)) {
                tools.PlayMouseClick();
                selectedTab = tab;
                return this;
            }
            trueX += tab.btnWidth + TAB_SEP;
        }
        return isMouseOver(mouseX, mouseY, x, getButtonsHeight(), x+width, y+height)
                ? selectedTab.mousePressed(mouseX-x, mouseY-y-getButtonsHeight(), mouseKeyIndex)
                : null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (visible && enabled && isMouseOver(mouseX, mouseY, x, getButtonsHeight(), x+width, y+height))
            return selectedTab.mouseScrolled(mouseX-x, mouseY-y-getButtonsHeight(), deltaY);
        return false;
    }


    public class Tab
    {
        private static final int PADDING_HORIZONTAL = 8;
        private static final int PADDING_VERTICAL = 4;

        private final ArrayList<AmWidget> widgets = new ArrayList<>();

        private final String name;
        private final int textWidth, textHeight, btnWidth, btnHeight;


        public Tab(String name) {
            this.name = name;

            this.textWidth = tools.getTextWidth(this.name);
            this.textHeight = tools.getTextHeight(this.name);
            this.btnWidth = textWidth + PADDING_HORIZONTAL*2;
            this.btnHeight = textHeight + PADDING_VERTICAL*2;
        }


        public Pair<Integer, Integer> getTrueHorizontal(boolean isSelected) {
            return new Pair<>(
                    isSelected ? y : y + PADDING_VERTICAL,
                    btnHeight + (isSelected ? PADDING_VERTICAL : 0)
            );
        }


        public void renderButton(BaseDrawer<?> drawer, int trueX, int mouseX, int mouseY) {
            boolean isSelected = selectedTab == this;
            var horizontal = getTrueHorizontal(isSelected);
            int trueY = horizontal.left();
            int trueHeight = horizontal.right();
            Color borderColor = isSelected ? Color.WHITE : Color.OFF_WHITE;

            renderRectangle(drawer, trueX, trueY, btnWidth, trueHeight, backgroundColor);
            if (isSelected || isMouseOver(mouseX, mouseY, trueX, trueY, trueX+btnWidth, trueY+trueHeight-2))
                renderRectangle(drawer, trueX, trueY, btnWidth, trueHeight, Color.WHITE_128);
            renderBorder(drawer, trueX, trueY, btnWidth, trueHeight, 1, borderColor);
            drawer.drawText(textDrawer -> textDrawer.drawText(
                    name,
                    new Vector2i(trueX + (btnWidth - textWidth)/2, trueY + (trueHeight - textHeight)/2),
                    Color.WHITE
            ));
        }

        private void renderBody(BaseDrawer<?> drawer, int mouseX, int mouseY) {
            for (var widget : this.widgets)
                widget.render(drawer, mouseX, mouseY);
        }

        public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
            for (var widget : this.widgets) {
                var handler = widget.mousePressed(mouseX, mouseY, mouseKeyIndex);
                if (handler != null)
                    return handler;
            }
            return null;
        }

        public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
            for (var widget : widgets)
                if (widget.mouseScrolled(mouseX, mouseY, deltaY))
                    return true;
            return false;
        }


        public <T extends AmWidget> T addWidget(T widget) {
            this.widgets.add(widget);
            return widget;
        }

        public void removeWidget(AmWidget widget) {
            this.widgets.remove(widget);
        }
    }
}
