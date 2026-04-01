package me.molybdenum.ambience_mini.engine.client.core.render.areas;

import me.molybdenum.ambience_mini.engine.client.core.areas.AreaHelper;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.areas.Owner;


// Java does not have multiple inheritance, but I needed the Area screen to extend Minecraft's Screen class
// in addition to a general class with all the general logic. The symbiote contains all the general logic
// whereas the non-symbiote contains what is needed for the screen to work in Minecraft.
public abstract class BaseAreaScreenSymbiote<TEditBox, TCheckBox, TButton>
{
    // Menu dimensions relative to GuiScale = 1
    public static final int MENU_MIN_WIDTH = 250;
    public static final int MENU_BORDER_THICKNESS = 1;
    public static final int MENU_INNER_MARGIN = 7;
    public static final int MENU_WIDGET_BASE_SEPARATION = 5;

    public static final int CHECKBOX_SIDE_LENGTH = 20;

    private final Area area;
    private final BaseDrawer baseDrawer;
    private final BaseAreaRenderer<?,?,?> areaRenderer;
    private final AreaHelper areaHelper;

    // Widgets
    protected final TEditBox txtAreaName;
    protected TCheckBox cbxPrivate, cbxShared, cbxPublic, cbxLocal;
    protected TButton btnSave, btnCancel, btnEditBounds, btnDelete;

    // Layout cache
    int areaNameInputHeight;

    private Vector2i areaNameLabelPos;
    private final String areaNameString;
    private final int areaNameLabelWidth;

    private final String ownershipString;
    private Vector2i ownershipLabelPos;

    private final int privateStringWidth;
    private final int sharedStringWidth;
    private final int publicStringWidth;

    int buttonHeight;
    int btnSaveWidth;
    int btnCancelWidth;
    int btnEditBoundsWidth;
    int btnDeleteWidth;

    int menuHeight;
    int menuWidth;

    // Checkbox state
    private boolean latestPrivateChecked = true;
    private boolean latestSharedChecked = false;
    private boolean latestPublicChecked = false;
    private boolean latestLocalChecked = false;

    // Widget states
    boolean allowInput = true;



    public BaseAreaScreenSymbiote(
            Area area,
            BaseDrawer baseDrawer,
            BaseNotification<?> notification,
            BaseAreaRenderer<?,?,?> areaRenderer,
            AreaHelper areaHelper
    ) {
        // Core state
        this.area = area;
        this.baseDrawer = baseDrawer;
        this.areaRenderer = areaRenderer;
        this.areaHelper = areaHelper;

        int lineHeight = baseDrawer.getLineHeight();

        // Area name
        areaNameString = notification.translateFromKey(AmLang.STRING_AREA_NAME);
        areaNameLabelWidth = baseDrawer.getTextWidth(areaNameString);

        int areaNameInputWidth = MENU_MIN_WIDTH - 2*MENU_INNER_MARGIN - areaNameLabelWidth - MENU_WIDGET_BASE_SEPARATION;
        areaNameInputHeight = lineHeight + 4;
        txtAreaName = makeTextBox(new Vector2i(areaNameInputWidth, areaNameInputHeight), this.area.name);

        // Ownership
        ownershipString = notification.translateFromKey(AmLang.STRING_OWNERSHIP_AND_SHARING);

        String privateString = notification.translateFromKey(AmLang.STRING_PRIVATE);
        privateStringWidth = baseDrawer.getTextWidth(privateString);
        cbxPrivate = makeCheckBox(area.owner.isPrivate(), privateString);

        String sharedString = notification.translateFromKey(AmLang.STRING_SHARED);
        sharedStringWidth = baseDrawer.getTextWidth(sharedString);
        cbxShared = makeCheckBox(area.owner.isShared(), sharedString);

        String publicString = notification.translateFromKey(AmLang.STRING_PUBLIC);
        publicStringWidth = baseDrawer.getTextWidth(sharedString);
        cbxPublic = makeCheckBox(area.owner.isPublic(), publicString);

        cbxLocal = makeCheckBox(area.owner.isLocal(), notification.translateFromKey(AmLang.STRING_LOCAL));

        // Buttons
        buttonHeight = lineHeight + 10;

        String confirmString = notification.translateFromKey(AmLang.STRING_SAVE);
        btnSaveWidth = baseDrawer.getTextWidth(confirmString) + 25;
        btnSave = makeButton(new Vector2i(btnSaveWidth, buttonHeight), confirmString, this::onConfirmClicked);

        String cancelString = notification.translateFromKey(AmLang.STRING_CANCEL);
        btnCancelWidth = baseDrawer.getTextWidth(cancelString) + 25;
        btnCancel = makeButton(new Vector2i(btnCancelWidth, buttonHeight), cancelString, this::resetEditorAndCloseMenu);

        String editBoundsString = notification.translateFromKey(AmLang.STRING_EDIT_BOUNDS);
        btnEditBoundsWidth = baseDrawer.getTextWidth(editBoundsString) + 25;
        btnEditBounds = makeButton(new Vector2i(btnEditBoundsWidth, buttonHeight), editBoundsString, this::onEditBoundsClicked);

        String deleteString = notification.translateFromKey(AmLang.STRING_DELETE);
        btnDeleteWidth = baseDrawer.getTextWidth(editBoundsString) + 25;
        btnDelete = makeButton(new Vector2i(btnDeleteWidth, buttonHeight), deleteString, this::onDeleteClicked);

        if (!area.canBeEditedBy(areaHelper.getPlayerUUID())) {
            allowInput = false;
            notification.printLiteralToChat("Cannot edit another player's area! How did you even get this window to open!?");
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Setup, rendering, and ticking
    public void init(IAreaScreenAccessor<TEditBox, TCheckBox, TButton> areaScreen) {
        menuHeight = MENU_INNER_MARGIN*2 + areaNameInputHeight + baseDrawer.getLineHeight() + CHECKBOX_SIDE_LENGTH + buttonHeight + MENU_WIDGET_BASE_SEPARATION*8;
        menuWidth = Math.max(MENU_INNER_MARGIN*2 + btnSaveWidth + btnCancelWidth + btnEditBoundsWidth + btnDeleteWidth + MENU_WIDGET_BASE_SEPARATION*3, MENU_MIN_WIDTH);
        Vector2i borderPos = new Vector2i((areaScreen.screenWidth() - menuWidth)/2, (areaScreen.screenHeight() - menuHeight)/2);

        // Area name label
        areaNameLabelPos = borderPos.offset(MENU_INNER_MARGIN, MENU_INNER_MARGIN + 2);

        // Area name textbox
        setEditBoxPos(txtAreaName, borderPos.x() + MENU_INNER_MARGIN + areaNameLabelWidth + MENU_WIDGET_BASE_SEPARATION, borderPos.y() + MENU_INNER_MARGIN);
        areaScreen.addTextBox(txtAreaName);

        // Ownership label
        ownershipLabelPos = areaNameLabelPos.offset(0, baseDrawer.getLineHeight() + MENU_WIDGET_BASE_SEPARATION*4);

        // Ownership checkboxes
        int checkboxY = ownershipLabelPos.y() + baseDrawer.getLineHeight() + MENU_WIDGET_BASE_SEPARATION;

        int privateX = borderPos.x() + MENU_INNER_MARGIN;
        setCheckBoxPos(cbxPrivate, privateX, checkboxY);
        areaScreen.addCheckBox(cbxPrivate);

        int sharedX = privateX + privateStringWidth + CHECKBOX_SIDE_LENGTH + 10;
        setCheckBoxPos(cbxShared, sharedX, checkboxY);
        areaScreen.addCheckBox(cbxShared);

        int publicX = sharedX + sharedStringWidth + CHECKBOX_SIDE_LENGTH + 10;
        setCheckBoxPos(cbxPublic, publicX, checkboxY);
        areaScreen.addCheckBox(cbxPublic);

        int localX = publicX + publicStringWidth + CHECKBOX_SIDE_LENGTH + 10;
        setCheckBoxPos(cbxLocal, localX, checkboxY);
        areaScreen.addCheckBox(cbxLocal);

        // Buttons
        int buttonY = checkboxY + CHECKBOX_SIDE_LENGTH + MENU_WIDGET_BASE_SEPARATION*4;

        int confirmX = borderPos.x() + MENU_INNER_MARGIN;
        setButtonPos(btnSave, confirmX, buttonY);
        areaScreen.addButton(btnSave);

        int cancelX = confirmX + btnSaveWidth + MENU_WIDGET_BASE_SEPARATION;
        setButtonPos(btnCancel, cancelX, buttonY);
        areaScreen.addButton(btnCancel);

        int editBoundsX = cancelX + btnCancelWidth + MENU_WIDGET_BASE_SEPARATION;
        setButtonPos(btnEditBounds, editBoundsX, buttonY);
        areaScreen.addButton(btnEditBounds);

        int deleteX = editBoundsX + btnEditBoundsWidth + MENU_WIDGET_BASE_SEPARATION;
        setButtonPos(btnDelete, deleteX, buttonY);
        if (!area.isNew())
            areaScreen.addButton(btnDelete);
    }

    public void tick() {
        tickEditBox(txtAreaName);

        // Make checkboxes behave like radio buttons
        boolean currentPrivateChecked = getSelected(cbxPrivate);
        boolean currentSharedChecked = getSelected(cbxShared);
        boolean currentPublicChecked = getSelected(cbxPublic);
        boolean currentLocalChecked = getSelected(cbxLocal);

        if (!currentPrivateChecked && !currentSharedChecked && !currentPublicChecked && !currentLocalChecked) {
            setSelected(cbxPrivate, latestPrivateChecked);
            setSelected(cbxShared, latestSharedChecked);
            setSelected(cbxPublic, latestPublicChecked);
            setSelected(cbxLocal, latestLocalChecked);
        }
        else if (!latestPrivateChecked && currentPrivateChecked)  {
            setSelected(cbxShared, false);
            setSelected(cbxPublic, false);
            setSelected(cbxLocal, false);
        }
        else if (!latestSharedChecked && currentSharedChecked)  {
            setSelected(cbxPrivate, false);
            setSelected(cbxPublic, false);
            setSelected(cbxLocal, false);
        }
        else if (!latestPublicChecked && currentPublicChecked)  {
            setSelected(cbxPrivate, false);
            setSelected(cbxShared, false);
            setSelected(cbxLocal, false);
        }
        else if (!latestLocalChecked && currentLocalChecked)  {
            setSelected(cbxPrivate, false);
            setSelected(cbxShared, false);
            setSelected(cbxPublic, false);
        }

        latestPrivateChecked = getSelected(cbxPrivate);
        latestSharedChecked = getSelected(cbxShared);
        latestPublicChecked = getSelected(cbxPublic);
        latestLocalChecked = getSelected(cbxLocal);
    }

    public void renderGeneralAreaScreen(int screenWidth, int screenHeight) {
        Vector2i borderPos = new Vector2i((screenWidth - menuWidth)/2, (screenHeight - menuHeight)/2);
        renderBorderedBox(borderPos);
        baseDrawer.drawText(textDrawer -> {
            textDrawer.drawText(areaNameString, areaNameLabelPos, Color.WHITE, Color.ALPHA_OPAQUE);
            textDrawer.drawText(ownershipString, ownershipLabelPos, Color.WHITE, Color.ALPHA_OPAQUE);
        });
    }

    private void renderBorderedBox(Vector2i p1Inner) {
        Vector2i p2Inner = p1Inner.offset(menuWidth, 0);
        Vector2i p3Inner = p1Inner.offset(menuWidth, menuHeight);
        Vector2i p4Inner = p1Inner.offset(0, menuHeight);

        Vector2i p1Outer = p1Inner.offset(-MENU_BORDER_THICKNESS, -MENU_BORDER_THICKNESS);
        Vector2i p2Outer = p2Inner.offset(MENU_BORDER_THICKNESS, -MENU_BORDER_THICKNESS);
        Vector2i p3Outer = p3Inner.offset(MENU_BORDER_THICKNESS, MENU_BORDER_THICKNESS);
        Vector2i p4Outer = p4Inner.offset(-MENU_BORDER_THICKNESS, MENU_BORDER_THICKNESS);

        baseDrawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dQuad(p1Inner, p4Inner, p4Outer, p1Outer, Color.WHITE, Color.ALPHA_OPAQUE); // left border
            quadDrawer.draw2dQuad(p1Inner, p2Inner, p2Outer, p1Outer, Color.WHITE, Color.ALPHA_OPAQUE); // top border
            quadDrawer.draw2dQuad(p3Inner, p2Inner, p2Outer, p3Outer, Color.WHITE, Color.ALPHA_OPAQUE); // right order
            quadDrawer.draw2dQuad(p3Inner, p4Inner, p4Outer, p3Outer, Color.WHITE, Color.ALPHA_OPAQUE); // bottom order

            quadDrawer.draw2dRectangle(p1Inner, p3Inner, Color.BLACK, Color.ALPHA_OPAQUE/2);
        });
    }

    public void onClose() {
        areaRenderer.resetEditor();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Misc
    private void onConfirmClicked() {
        if (!allowInput)
            return;

        allowInput = false;
        area.name = getValue(txtAreaName);
        area.owner = new Owner(
                getSelected(cbxPublic) || getSelected(cbxLocal)
                        ? new Owner.Ownerless(getSelected(cbxLocal))
                        : new Owner.Owned(areaHelper.getPlayerUUID(), getSelected(cbxShared))
                );
        area.fromBlock = areaHelper.getSelectedCube().getFromBlock();
        area.toBlock = areaHelper.getSelectedCube().getToBlock();

        areaHelper.submitArea(
                area,
                this::resetEditorAndCloseMenu,
                () -> allowInput = true
        );
    }

    private void onDeleteClicked() {
        if (!allowInput)
            return;

        allowInput = false;
        areaHelper.deleteArea(
                area.id,
                this::resetEditorAndCloseMenu,
                () -> allowInput = true
        );
    }

    private void onEditBoundsClicked() {
        if (allowInput)
            closeScreen();
    }

    private void resetEditorAndCloseMenu() {
        areaRenderer.resetEditor();
        closeScreen();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract TEditBox makeTextBox(Vector2i size, String content);
    protected abstract TCheckBox makeCheckBox(boolean selected, String label);
    protected abstract TButton makeButton(Vector2i size, String content, Runnable onClick);

    protected abstract void setEditBoxPos(TEditBox editBox, int x, int y);
    protected abstract void setCheckBoxPos(TCheckBox checkBox, int x, int y);
    protected abstract void setButtonPos(TButton button, int x, int y);

    protected abstract void tickEditBox(TEditBox editBox);
    protected abstract String getValue(TEditBox editBox);

    protected abstract void setSelected(TCheckBox checkBox, boolean selected);
    protected abstract boolean getSelected(TCheckBox checkBox);

    protected abstract void closeScreen();
}
