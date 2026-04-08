package me.molybdenum.ambience_mini.v1_21_1.client.core.render.area;

import me.molybdenum.ambience_mini.engine.client.core.render.areas.IAreaScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class AreaScreen extends Screen
{
    private final AreaScreenAccessor accessor = new AreaScreenAccessor();

    private final AreaScreenSymbiote symbiote;


    public AreaScreen(AreaScreenSymbiote symbiote) {
        super(Component.literal("Area screen"));
        this.symbiote = symbiote;
    }


    @Override
    protected void init() {
        super.init();
        symbiote.init(accessor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        symbiote.tick();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        symbiote.setup(graphics.pose().last());
        symbiote.renderGeneralAreaScreen(width, height);

        for (Renderable renderable : this.renderables)
            renderable.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() { // This only fires when pressing Escape
        super.onClose();
        symbiote.onClose();
    }

    private class AreaScreenAccessor implements IAreaScreenAccessor<EditBox, Checkbox, Button> {
        @Override
        public void addTextBox(EditBox editBox) {
            AreaScreen.this.addRenderableWidget(editBox);
        }

        @Override
        public void addCheckBox(Checkbox checkbox) {
            AreaScreen.this.addRenderableWidget(checkbox);
        }

        @Override
        public void addButton(Button button) {
            AreaScreen.this.addRenderableWidget(button);
        }

        @Override
        public int screenWidth() {
            return AreaScreen.this.width;
        }

        @Override
        public int screenHeight() {
            return AreaScreen.this.height;
        }
    }
}
