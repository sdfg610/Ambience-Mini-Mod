package me.molybdenum.ambience_mini.engine.client.core.render.areas;

import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;


public abstract class AbstractAreaScreenSymbiote<TEditBox, TCheckBox, TButton>
{
    // Menu dimensions relative to GuiScale = 1
    public static final int MENU_WIDTH = 250;
    public static final int MENU_HEIGHT = 150;
    public static final int MENU_BORDER_THICKNESS = 1;
    public static final int MENU_INNER_MARGIN = 10;
    public static final int MENU_WIDGET_BASE_SEPARATION = 5;

    public static final Vector2i MENU_SIZE = new Vector2i(MENU_WIDTH, MENU_HEIGHT);

    // Core state
    private final BaseNotification<?> notification;
    private final BaseAreaRenderer<?, ?> areaRenderer;
    private final Area area;

    // Widgets
    protected final TEditBox txtAreaName;
    protected TCheckBox cbxPrivate, cbxShared, cbxPublic;
    protected TButton btnConfirm, btnCancel, btnEditBounds;

    int areaNameLabelWidth;


    public AbstractAreaScreenSymbiote(BaseNotification<?> notification, BaseAreaRenderer<?, ?> areaRenderer, Area area) {
        this.notification = notification;
        this.areaRenderer = areaRenderer;
        this.area = area;

        areaNameLabelWidth = areaRenderer.baseDrawer.getTextWidth(notification.translateFromKey(AmLang.STRING_AREA_NAME));

        int areaNameInputWidth = MENU_WIDTH - 2*MENU_INNER_MARGIN - areaNameLabelWidth - MENU_WIDGET_BASE_SEPARATION;
        int areaNameInputHeight = areaRenderer.baseDrawer.getLineHeight() + 4;
        txtAreaName = makeTextBox(new Vector2i(areaNameInputWidth, areaNameInputHeight), area.name);
    }


    public void init(IAreaScreen<TEditBox, TCheckBox, TButton> areaScreen) {
        Vector2i borderPos = new Vector2i((areaScreen.screenWidth() - MENU_WIDTH)/2, (areaScreen.screenHeight() - MENU_HEIGHT)/2);

        setEditBoxPos(txtAreaName, borderPos.x() + MENU_INNER_MARGIN + areaNameLabelWidth + MENU_WIDGET_BASE_SEPARATION, borderPos.y() + MENU_INNER_MARGIN);
        areaScreen.addTextBox(txtAreaName);

        // TODO: The rest of the widgets
    }

    public void tick() {
        tickEditBox(txtAreaName);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract TEditBox makeTextBox(Vector2i size, String content);
    protected abstract TCheckBox makeCheckBox(Vector2i size, String content);
    protected abstract TButton makeButton(Vector2i size, String content);

    protected abstract void setEditBoxPos(TEditBox editBox, int x, int y);
    protected abstract void setCheckBoxPos(TCheckBox checkBox, int x, int y);
    protected abstract void setButtonPos(TButton button, int x, int y);

    protected abstract void tickEditBox(TEditBox editBox);
}
