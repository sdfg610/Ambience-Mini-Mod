package me.molybdenum.ambience_mini.v1_20_1.client.core.render.area;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.v1_20_1.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.engine.client.core.locations.AreaHelper;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AreaScreenSymbiote extends BaseAreaScreenSymbiote<EditBox, Checkbox, Button>
{
    private static final Minecraft mc = Minecraft.getInstance();
    private final Drawer drawer;


    public AreaScreenSymbiote(
            Area area,
            Drawer drawer,
            BaseNotification<?> notification,
            BaseAreaRenderer<?, ?, ?> areaRenderer,
            AreaHelper areaHelper
    ) {
        super(area, drawer, notification, areaRenderer, areaHelper);
        this.drawer = drawer;
    }


    public void setup(PoseStack.Pose pose) {
        drawer.setup(pose);
    }


    @Override
    protected EditBox makeTextBox(Vector2i size, String content) {
        EditBox editBox = new EditBox(mc.font, 0, 0, size.x(), size.y(), Component.empty());
        editBox.setMaxLength(Common.MAX_AREA_NAME_LENGTH);
        editBox.setValue(content);
        return editBox;
    }

    @Override
    protected Checkbox makeCheckBox(boolean selected, String label) {
        return new Checkbox(0, 0, CHECKBOX_SIDE_LENGTH, CHECKBOX_SIDE_LENGTH, Component.literal(label), selected);
    }

    @Override
    protected Button makeButton(Vector2i size, String content, Runnable onClick) {
        return Button.builder(Component.literal(content), (ignored) -> onClick.run())
                .size(size.x(), size.y())
                .build();
    }


    @Override
    protected void setEditBoxPos(EditBox editBox, int x, int y) {
        editBox.setPosition(x, y);
    }

    @Override
    protected void setCheckBoxPos(Checkbox checkbox, int x, int y) {
        checkbox.setPosition(x, y);
    }

    @Override
    protected void setButtonPos(Button button, int x, int y) {
        button.setPosition(x, y);
    }


    @Override
    protected void tickEditBox(EditBox editBox) {
        editBox.tick();
    }

    @Override
    protected String getValue(EditBox editBox) {
        return editBox.getValue();
    }


    @Override
    protected void setSelected(Checkbox checkbox, boolean selected) {
        if (checkbox.selected() != selected)
            checkbox.onPress();
    }

    @Override
    protected boolean getSelected(Checkbox checkbox) {
        return checkbox.selected();
    }


    @Override
    protected void closeScreen() {
        mc.execute(() -> mc.setScreen(null));
    }
}
