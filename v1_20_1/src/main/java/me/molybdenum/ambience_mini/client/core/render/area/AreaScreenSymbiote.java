package me.molybdenum.ambience_mini.client.core.render.area;

import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.AbstractAreaScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class AreaScreenSymbiote extends AbstractAreaScreenSymbiote<EditBox, Checkbox, Button>
{
    public AreaScreenSymbiote(BaseNotification<?> notification, BaseAreaRenderer<?, ?> areaRenderer, Area area) {
        super(notification, areaRenderer, area);
    }

    @Override
    protected EditBox makeTextBox(Vector2i size, String content) {
        EditBox editBox = new EditBox(Minecraft.getInstance().font, 0, 0, size.x(), size.y(), Component.empty());
        editBox.setMaxLength(Common.MAX_AREA_NAME_LENGTH);
        editBox.setValue(content);
        return editBox;
    }

    @Override
    protected Checkbox makeCheckBox(Vector2i size, String content) {
        return null;
    }

    @Override
    protected Button makeButton(Vector2i size, String content) {
        return null;
    }

    @Override
    protected void setEditBoxPos(EditBox editBox, int x, int y) {
        editBox.setPosition(x, y);
    }

    @Override
    protected void setCheckBoxPos(Checkbox checkbox, int x, int y) {

    }

    @Override
    protected void setButtonPos(Button button, int x, int y) {

    }

    @Override
    protected void tickEditBox(EditBox editBox) {
        editBox.tick();
    }
}
