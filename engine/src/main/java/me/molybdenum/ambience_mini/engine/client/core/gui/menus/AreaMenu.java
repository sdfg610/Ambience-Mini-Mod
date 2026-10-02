package me.molybdenum.ambience_mini.engine.client.core.gui.menus;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.AmMenu;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.McScreenBuilder;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.*;
import me.molybdenum.ambience_mini.engine.client.core.locations.areas.AreaHelper;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Owner;

public class AreaMenu<TPoseStack> extends AmMenu<TPoseStack>
{
    private static final int MENU_MIN_WIDTH = 250;
    private static final int BUTTON_HEIGHT = 17;

    private final Area area;
    private final BaseNotification<?> notification;
    private final BaseAreaRenderer<?,?> areaRenderer;
    private final AreaHelper areaHelper;

    private final Panel panel;

    // Widgets
    private final Textbox txtAreaName;
    private final RadioButton rdbShared, rdbLocal, rdbPublic;
    private final Button btnDelete;


    // Delete state
    private final String deleteString;
    private final String confirmDeleteString;
    private boolean confirmingDelete = false;

    // Widget states
    private boolean allowInput = true;


    public AreaMenu(
            BaseDrawer<TPoseStack> drawer,
            McScreenBuilder<TPoseStack> builder,
            Area area,
            BaseNotification<?> notification,
            BaseAreaRenderer<?,?> areaRenderer,
            AreaHelper areaHelper
    ) {
        super(drawer, "Area menu", builder);

        if (!area.canBeEditedBy(areaHelper.getPlayerUUID())) {
            allowInput = false;
            notification.printLiteralToChat("Cannot edit another player's area! How did you even get this window to open!?");
        }

        this.area = area;
        this.notification = notification;
        this.areaRenderer = areaRenderer;
        this.areaHelper = areaHelper;

        panel = addWidget(new Panel(10, 10, 10, 10)); // Final position and size later
        panel.backgroundColor = Color.BLACK_128;

        // Area name
        int paddedLineHeight = drawer.getLineHeight() + 8;

        String areaNameString = notification.translateFromKey(AmLang.STRING_AREA_NAME);
        Label lblAreaName = panel.addWidget(new Label(MENU_INNER_MARGIN, MENU_INNER_MARGIN, drawer.getTextWidth(areaNameString), paddedLineHeight, areaNameString));
        txtAreaName = panel.addWidget(new Textbox(lblAreaName.right() + BASE_SEPARATION, MENU_INNER_MARGIN, 0, paddedLineHeight)); // Width later
        txtAreaName.setText(area.name);

        // Ownership and sharing
        String ownershipString = notification.translateFromKey(AmLang.STRING_OWNERSHIP_AND_SHARING);
        Label lblOwnership = panel.addWidget(new Label(
                MENU_INNER_MARGIN, lblAreaName.bottom() + BASE_SEPARATION*4,
                drawer.getTextWidth(ownershipString), paddedLineHeight,
                ownershipString
        ));

        boolean areasEnabled = areaHelper.areServerAreasEnabled();
        boolean isNew = area.isNew();

        int radioY = lblOwnership.bottom() + BASE_SEPARATION;
        var radioGroup = RadioButton.newGroup();

        RadioButton rdbPrivate = panel.addWidget(new RadioButton(
                MENU_INNER_MARGIN, radioY, notification.translateFromKey(AmLang.STRING_PRIVATE), radioGroup, drawer
        ));
        rdbPrivate.setSelected((isNew || area.owner.isPrivate()) && areasEnabled);

        rdbShared = panel.addWidget(new RadioButton(
                MENU_INNER_MARGIN + rdbPrivate.right() + BASE_SEPARATION, radioY, notification.translateFromKey(AmLang.STRING_SHARED), radioGroup, drawer
        ));
        rdbShared.setSelected(area.owner.isShared() && areasEnabled);

        rdbPublic = panel.addWidget(new RadioButton(
                MENU_INNER_MARGIN + rdbShared.right() + BASE_SEPARATION, radioY, notification.translateFromKey(AmLang.STRING_PUBLIC), radioGroup, drawer
        ));
        rdbPublic.setSelected(area.owner.isPublic() && areasEnabled);

        rdbLocal = panel.addWidget(new RadioButton(
                MENU_INNER_MARGIN + rdbPublic.right() + BASE_SEPARATION, radioY, notification.translateFromKey(AmLang.STRING_LOCAL), radioGroup, drawer
        ));
        rdbLocal.setSelected(area.owner.isLocal() || !areasEnabled);


        // Buttons
        int buttonY = rdbPrivate.bottom() + BASE_SEPARATION*4;

        String confirmString = notification.translateFromKey(AmLang.STRING_SAVE);
        Button btnSave = panel.addWidget(new Button(MENU_INNER_MARGIN, buttonY, drawer.getTextWidth(confirmString) + 20, BUTTON_HEIGHT, confirmString, this::onConfirmClicked));

        String cancelString = notification.translateFromKey(AmLang.STRING_CANCEL);
        Button btnCancel = panel.addWidget(new Button(MENU_INNER_MARGIN + btnSave.right(), buttonY, drawer.getTextWidth(cancelString) + 20, BUTTON_HEIGHT, cancelString, this::onCancelClicked));

        String editBoundsString = notification.translateFromKey(AmLang.STRING_EDIT_BOUNDS);
        Button btnEditBounds = panel.addWidget(new Button(MENU_INNER_MARGIN + btnCancel.right(), buttonY, drawer.getTextWidth(editBoundsString) + 20, BUTTON_HEIGHT, editBoundsString, this::onEditBoundsClicked));

        deleteString = notification.translateFromKey(AmLang.STRING_DELETE);
        confirmDeleteString = notification.translateFromKey(AmLang.STRING_CONFIRM_DELETE);
        btnDelete = panel.addWidget(new Button(MENU_INNER_MARGIN + btnEditBounds.right(), buttonY, drawer.getTextWidth(confirmDeleteString) + 20, BUTTON_HEIGHT, deleteString, this::onDeleteClicked));


        // Final sizing
        panel.autoSize();
        panel.width = Math.max(MENU_MIN_WIDTH, panel.width);
        txtAreaName.width = panel.width - 2*MENU_INNER_MARGIN - lblAreaName.width - BASE_SEPARATION;
    }


    @Override
    public void onInit() {
        // Positioning of the menu
        panel.x = (screen.screenWidth() - panel.width)/2;
        panel.y = (screen.screenHeight() - panel.height)/2;
    }

    @Override
    public void onClose() {
        areaRenderer.resetEditor();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Buttons
    private void onConfirmClicked() {
        if (!allowInput)
            return;
        resetDelete();

        var name = txtAreaName.getText();
        if (name.isBlank() || name.length() > Constants.MAX_AREA_NAME_LENGTH) {
            notification.printTranslatableToChat(AmLang.MSG_AREA_NAME_REQUIREMENTS, Constants.MAX_AREA_NAME_LENGTH);
            return;
        }

        allowInput = false;
        area.name = name;
        area.owner = new Owner(
                rdbPublic.isSelected() || rdbLocal.isSelected()
                        ? new Owner.Ownerless(rdbLocal.isSelected())
                        : new Owner.Owned(areaHelper.getPlayerUUID(), rdbShared.isSelected())
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
            btnDelete.text = confirmDeleteString;
            confirmingDelete = true;
        }
    }

    private void onEditBoundsClicked() {
        if (!allowInput)
            return;

        resetDelete();
        screen.close();
        areaRenderer.enableAreaResize();
    }


    private void resetEditorAndCloseMenu() {
        areaRenderer.resetEditor();
        screen.close();
    }

    private void resetDelete() {
        confirmingDelete = false;
        btnDelete.text = deleteString;
    }
}
