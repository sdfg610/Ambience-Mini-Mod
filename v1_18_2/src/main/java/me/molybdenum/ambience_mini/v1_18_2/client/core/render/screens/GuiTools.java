package me.molybdenum.ambience_mini.v1_18_2.client.core.render.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.AreaScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.BaseScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.IGuiTools;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.IScreen;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class GuiTools implements IGuiTools<Screen, PoseStack.Pose, AbstractWidget, EditBox, Checkbox, Button>
{
    public static final GuiTools INSTANCE = new GuiTools();


    private GuiTools() { }


    // Screen tools
    @Override
    public IScreen<Screen, AbstractWidget> makeScreen(
            BaseScreenSymbiote<Screen, PoseStack.Pose, AbstractWidget, EditBox, Checkbox, Button> symbiote,
            String screenName
    ) {
        return new AmScreen(symbiote, screenName);
    }

    @Override
    public void closeScreen() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(null));
    }


    // Widget tools
    @Override
    public EditBox makeTextBox(Vector2i size, String content) {
        EditBox editBox = new EditBox(Minecraft.getInstance().font, 0, 0, size.x(), size.y(), TextComponent.EMPTY);
        editBox.setMaxLength(Constants.MAX_AREA_NAME_LENGTH);
        editBox.setValue(content);
        return editBox;
    }

    @Override
    public Checkbox makeCheckBox(boolean selected, String label) {
        return new Checkbox(0, 0, AreaScreenSymbiote.CHECKBOX_SIDE_LENGTH, AreaScreenSymbiote.CHECKBOX_SIDE_LENGTH, new TextComponent(label), selected);
    }

    @Override
    public Button makeButton(Vector2i size, String content, Runnable onClick) {
        return new Button(0, 0, size.x(), size.y(), new TextComponent(content), (ignored) -> onClick.run());
    }


    @Override
    public void setEditBoxPos(EditBox editBox, int x, int y) {
        editBox.x = x;
        editBox.y = y;
    }

    @Override
    public void setCheckBoxPos(Checkbox checkbox, int x, int y) {
        checkbox.x = x;
        checkbox.y = y;
    }

    @Override
    public void setButtonPos(Button button, int x, int y) {
        button.x = x;
        button.y = y;
    }

    @Override
    public void setButtonText(Button button, String text) {
        button.setMessage(new TextComponent(text));
    }


    @Override
    public void tickEditBox(EditBox editBox) {
        editBox.tick();
    }

    @Override
    public String getEditBoxValue(EditBox editBox) {
        return editBox.getValue();
    }


    @Override
    public void setSelected(Checkbox checkbox, boolean selected) {
        if (checkbox.selected() != selected)
            checkbox.onPress();
    }

    @Override
    public boolean getSelected(Checkbox checkbox) {
        return checkbox.selected();
    }
}
