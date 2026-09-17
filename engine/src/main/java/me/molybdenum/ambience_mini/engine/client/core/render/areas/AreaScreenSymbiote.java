package me.molybdenum.ambience_mini.engine.client.core.render.areas;

import me.molybdenum.ambience_mini.engine.client.core.locations.areas.AreaHelper;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.IGuiTools;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.BaseScreenSymbiote;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Owner;


public class AreaScreenSymbiote<
        TScreen,
        TPose,
        TWidget,
        TEditBox extends TWidget,
        TCheckBox extends TWidget,
        TButton extends TWidget
> extends BaseScreenSymbiote<TScreen, TPose, TWidget, TEditBox, TCheckBox, TButton> {
    // Menu dimensions relative to GuiScale = 1
    public static final int MENU_MIN_WIDTH = 250;
    public static final int MENU_BORDER_THICKNESS = 1;
    public static final int MENU_INNER_MARGIN = 7;
    public static final int MENU_WIDGET_BASE_SEPARATION = 5;

    public static final int CHECKBOX_SIDE_LENGTH = 20;

    private final Area area;
    private final BaseDrawer<TPose> baseDrawer;
    private final BaseNotification<?> notification;
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

    // Delete state
    private final String deleteString;
    private final String confirmDeleteString;
    private boolean confirmingDelete = false;

    // Widget states
    private boolean allowInput = true;

    public AreaScreenSymbiote(
            IGuiTools<TScreen, TPose, TWidget, TEditBox, TCheckBox, TButton> gui,
            Area area,
            BaseDrawer<TPose> baseDrawer,
            BaseNotification<?> notification,
            BaseAreaRenderer<?,?,?> areaRenderer,
            AreaHelper areaHelper
    ) {
        super(gui, "Area screen");

        // Core state
        this.area = area;
        this.baseDrawer = baseDrawer;
        this.notification = notification;
        this.areaRenderer = areaRenderer;
        this.areaHelper = areaHelper;

        int lineHeight = baseDrawer.getLineHeight();

        // Ownership
        boolean hasServerSupport = areaHelper.hasServerSupport();
        boolean isNew = area.isNew();
        ownershipString = notification.translateFromKey(AmLang.STRING_OWNERSHIP_AND_SHARING);

        String privateString = notification.translateFromKey(AmLang.STRING_PRIVATE);
        privateStringWidth = baseDrawer.getTextWidth(privateString);
        cbxPrivate = gui.makeCheckBox((isNew || area.owner.isPrivate()) && hasServerSupport, privateString);

        String sharedString = notification.translateFromKey(AmLang.STRING_SHARED);
        sharedStringWidth = baseDrawer.getTextWidth(sharedString);
        cbxShared = gui.makeCheckBox(area.owner.isShared() && hasServerSupport, sharedString);

        String publicString = notification.translateFromKey(AmLang.STRING_PUBLIC);
        publicStringWidth = baseDrawer.getTextWidth(sharedString);
        cbxPublic = gui.makeCheckBox(area.owner.isPublic() && hasServerSupport, publicString);

        cbxLocal = gui.makeCheckBox(area.owner.isLocal() || !hasServerSupport, notification.translateFromKey(AmLang.STRING_LOCAL));

        // Buttons
        buttonHeight = lineHeight + switch (areaHelper.mcVersion) {
            case V1_18, V1_19 -> 11;
            default -> 10;
        };

        String confirmString = notification.translateFromKey(AmLang.STRING_SAVE);
        btnSaveWidth = baseDrawer.getTextWidth(confirmString) + 25;
        btnSave = gui.makeButton(new Vector2i(btnSaveWidth, buttonHeight), confirmString, this::onConfirmClicked);

        String cancelString = notification.translateFromKey(AmLang.STRING_CANCEL);
        btnCancelWidth = baseDrawer.getTextWidth(cancelString) + 25;
        btnCancel = gui.makeButton(new Vector2i(btnCancelWidth, buttonHeight), cancelString, this::onCancelClicked);

        String editBoundsString = notification.translateFromKey(AmLang.STRING_EDIT_BOUNDS);
        btnEditBoundsWidth = baseDrawer.getTextWidth(editBoundsString) + 25;
        btnEditBounds = gui.makeButton(new Vector2i(btnEditBoundsWidth, buttonHeight), editBoundsString, this::onEditBoundsClicked);

        deleteString = notification.translateFromKey(AmLang.STRING_DELETE);
        confirmDeleteString = notification.translateFromKey(AmLang.STRING_CONFIRM_DELETE);
        btnDeleteWidth = Math.max(baseDrawer.getTextWidth(deleteString), baseDrawer.getTextWidth(confirmDeleteString)) + 25;
        btnDelete = gui.makeButton(new Vector2i(btnDeleteWidth, buttonHeight), deleteString, this::onDeleteClicked);

        if (!area.canBeEditedBy(areaHelper.getPlayerUUID())) {
            allowInput = false;
            notification.printLiteralToChat("Cannot edit another player's area! How did you even get this window to open!?");
        }

        // Menu size (+ Area name)
        areaNameInputHeight = lineHeight + 4;
        menuHeight = MENU_INNER_MARGIN*2 + areaNameInputHeight + baseDrawer.getLineHeight() + CHECKBOX_SIDE_LENGTH + buttonHeight + MENU_WIDGET_BASE_SEPARATION*8;
        menuWidth = Math.max(MENU_INNER_MARGIN*2 + btnSaveWidth + btnCancelWidth + btnEditBoundsWidth + btnDeleteWidth + MENU_WIDGET_BASE_SEPARATION*3, MENU_MIN_WIDTH);

        // Area name
        areaNameString = notification.translateFromKey(AmLang.STRING_AREA_NAME);
        areaNameLabelWidth = baseDrawer.getTextWidth(areaNameString);

        int areaNameInputWidth = menuWidth - 2*MENU_INNER_MARGIN - areaNameLabelWidth - MENU_WIDGET_BASE_SEPARATION;
        txtAreaName = gui.makeTextBox(new Vector2i(areaNameInputWidth, areaNameInputHeight), this.area.name);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Setup, rendering, and ticking
    @Override
    public void init() {
        Vector2i borderPos = new Vector2i((screen.screenWidth() - menuWidth)/2, (screen.screenHeight() - menuHeight)/2);

        // Area name label
        areaNameLabelPos = borderPos.offset(MENU_INNER_MARGIN, MENU_INNER_MARGIN + 2);

        // Area name textbox
        gui.setEditBoxPos(txtAreaName, borderPos.x() + MENU_INNER_MARGIN + areaNameLabelWidth + MENU_WIDGET_BASE_SEPARATION, borderPos.y() + MENU_INNER_MARGIN);
        screen.addWidget(txtAreaName);

        // Ownership label
        ownershipLabelPos = areaNameLabelPos.offset(0, baseDrawer.getLineHeight() + MENU_WIDGET_BASE_SEPARATION*4);

        // Ownership checkboxes
        int checkboxY = ownershipLabelPos.y() + baseDrawer.getLineHeight() + MENU_WIDGET_BASE_SEPARATION;
        int firstCheckboxX = borderPos.x() + MENU_INNER_MARGIN;
        if (areaHelper.hasServerSupport()) {
            int sharedX = firstCheckboxX + privateStringWidth + CHECKBOX_SIDE_LENGTH + 10;
            int publicX = sharedX + sharedStringWidth + CHECKBOX_SIDE_LENGTH + 10;
            int localX = publicX + publicStringWidth + CHECKBOX_SIDE_LENGTH + 10;

            gui.setCheckBoxPos(cbxPrivate, firstCheckboxX, checkboxY);
            screen.addWidget(cbxPrivate);

            gui.setCheckBoxPos(cbxShared, sharedX, checkboxY);
            screen.addWidget(cbxShared);

            gui.setCheckBoxPos(cbxPublic, publicX, checkboxY);
            screen.addWidget(cbxPublic);

            gui.setCheckBoxPos(cbxLocal, localX, checkboxY);
            screen.addWidget(cbxLocal);
        }
        else {
            gui.setCheckBoxPos(cbxLocal, firstCheckboxX, checkboxY);
            screen.addWidget(cbxLocal);
        }

        // Buttons
        int buttonY = checkboxY + CHECKBOX_SIDE_LENGTH + MENU_WIDGET_BASE_SEPARATION*4;

        int confirmX = borderPos.x() + MENU_INNER_MARGIN;
        gui.setButtonPos(btnSave, confirmX, buttonY);
        screen.addWidget(btnSave);

        int cancelX = confirmX + btnSaveWidth + MENU_WIDGET_BASE_SEPARATION;
        gui.setButtonPos(btnCancel, cancelX, buttonY);
        screen.addWidget(btnCancel);

        int editBoundsX = cancelX + btnCancelWidth + MENU_WIDGET_BASE_SEPARATION;
        gui.setButtonPos(btnEditBounds, editBoundsX, buttonY);
        screen.addWidget(btnEditBounds);

        int deleteX = editBoundsX + btnEditBoundsWidth + MENU_WIDGET_BASE_SEPARATION;
        gui.setButtonPos(btnDelete, deleteX, buttonY);
        if (!area.isNew())
            screen.addWidget(btnDelete);
    }

    @Override
    public void tick() {
        gui.tickEditBox(txtAreaName);

        // Make checkboxes behave like radio buttons
        boolean currentPrivateChecked = gui.getSelected(cbxPrivate);
        boolean currentSharedChecked = gui.getSelected(cbxShared);
        boolean currentPublicChecked = gui.getSelected(cbxPublic);
        boolean currentLocalChecked = gui.getSelected(cbxLocal);

        if (!currentPrivateChecked && !currentSharedChecked && !currentPublicChecked && !currentLocalChecked) {
            gui.setSelected(cbxPrivate, latestPrivateChecked);
            gui.setSelected(cbxShared, latestSharedChecked);
            gui.setSelected(cbxPublic, latestPublicChecked);
            gui.setSelected(cbxLocal, latestLocalChecked);
        }
        else if (!latestPrivateChecked && currentPrivateChecked)  {
            gui.setSelected(cbxShared, false);
            gui.setSelected(cbxPublic, false);
            gui.setSelected(cbxLocal, false);
        }
        else if (!latestSharedChecked && currentSharedChecked)  {
            gui.setSelected(cbxPrivate, false);
            gui.setSelected(cbxPublic, false);
            gui.setSelected(cbxLocal, false);
        }
        else if (!latestPublicChecked && currentPublicChecked)  {
            gui.setSelected(cbxPrivate, false);
            gui.setSelected(cbxShared, false);
            gui.setSelected(cbxLocal, false);
        }
        else if (!latestLocalChecked && currentLocalChecked)  {
            gui.setSelected(cbxPrivate, false);
            gui.setSelected(cbxShared, false);
            gui.setSelected(cbxPublic, false);
        }

        latestPrivateChecked = gui.getSelected(cbxPrivate);
        latestSharedChecked = gui.getSelected(cbxShared);
        latestPublicChecked = gui.getSelected(cbxPublic);
        latestLocalChecked = gui.getSelected(cbxLocal);
    }

    @Override
    public void renderBackground(TPose pose, int screenWidth, int screenHeight) {
        baseDrawer.setup(pose);
        renderGeneralAreaScreen(screenWidth, screenHeight);
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

    @Override
    public void onClose() {
        areaRenderer.resetEditor();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Misc
    private void onConfirmClicked() {
        if (!allowInput)
            return;
        resetDelete();

        var name = gui.getEditBoxValue(txtAreaName);
        if (name.isBlank() || name.length() > Constants.MAX_AREA_NAME_LENGTH) {
            notification.printTranslatableToChat(AmLang.MSG_AREA_NAME_REQUIREMENTS, Constants.MAX_AREA_NAME_LENGTH);
            return;
        }

        allowInput = false;
        area.name = name;
        area.owner = new Owner(
                gui.getSelected(cbxPublic) || gui.getSelected(cbxLocal)
                        ? new Owner.Ownerless(gui.getSelected(cbxLocal))
                        : new Owner.Owned(areaHelper.getPlayerUUID(), gui.getSelected(cbxShared))
                );
        area.fromBlock = areaHelper.getSelectedCube().getFromBlock();
        area.toBlock = areaHelper.getSelectedCube().getToBlock();

        areaHelper.submitArea(
                area,
                this::resetEditorAndCloseMenu,
                error -> {
                    notification.printToChat(error);
                    allowInput = true;
                }
        );
    }

    private void onCancelClicked() {
        if (!allowInput)
            return;

        resetEditorAndCloseMenu();
    }

    private void onDeleteClicked() {
        if (!allowInput)
            return;

        if (confirmingDelete) {
            allowInput = false;
            areaHelper.deleteArea(
                    area.id,
                    this::resetEditorAndCloseMenu,
                    error -> {
                        notification.printToChat(error);
                        allowInput = true;
                        resetDelete();
                    }
            );
        }
        else {
            gui.setButtonText(btnDelete, confirmDeleteString);
            confirmingDelete = true;
        }
    }

    private void onEditBoundsClicked() {
        if (!allowInput)
            return;

        resetDelete();
        gui.closeScreen();
    }


    private void resetEditorAndCloseMenu() {
        areaRenderer.resetEditor();
        gui.closeScreen();
    }

    private void resetDelete() {
        confirmingDelete = false;
        gui.setButtonText(btnDelete, deleteString);
    }
}
