package me.molybdenum.ambience_mini.client.core.render.area;

import me.molybdenum.ambience_mini.engine.client.core.render.areas.AbstractAreaScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.IAreaScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class AreaScreen extends Screen implements IAreaScreen<EditBox, Checkbox, Button>
{
    private final AreaRenderer areaRenderer;
    private final AbstractAreaScreenSymbiote<EditBox, Checkbox, Button> symbiote;

    // TODO: How to best generalize this thing!????????

    Checkbox cbxPrivate = new Checkbox(30, 60, 20, 20, Component.literal("content"), true);
    Checkbox cbxShared = new Checkbox(30, 60, 20, 20, Component.literal("content"), true);
    Checkbox cbxPublic = new Checkbox(30, 60, 20, 20, Component.literal("content"), true);

    Button btnConfirm = Button.builder(Component.literal("content"), (button) -> {}).pos(30, 90).size(20, 50).build();
    Button btnCancel = Button.builder(Component.literal("content"), (button) -> {}).pos(30, 90).size(20, 50).build();
    Button btnEditBounds = Button.builder(Component.literal("content"), (button) -> {}).pos(30, 90).size(20, 50).build();


    public AreaScreen(AreaRenderer areaRenderer, AbstractAreaScreenSymbiote<EditBox, Checkbox, Button> symbiote) {
        super(Component.literal("Area screen"));
        this.areaRenderer = areaRenderer;
        this.symbiote = symbiote;
    }


    @Override
    protected void init() {
        super.init();
        symbiote.init(this);
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
        areaRenderer.setup(graphics.pose().last(), null);    // |
        areaRenderer.renderGeneralAreaScreen(width, height);        // TODO: Generalise these two lines
        super.render(graphics, mouseX, mouseY, partialTick);
    }


    @Override
    public void addTextBox(EditBox editBox) {
        this.addRenderableWidget(editBox);
    }

    @Override
    public void addCheckBox(Checkbox checkbox) {
        this.addRenderableWidget(checkbox);
    }

    @Override
    public void addButton(Button button) {
        this.addRenderableWidget(button);
    }

    @Override
    public int screenWidth() {
        return this.width;
    }

    @Override
    public int screenHeight() {
        return this.height;
    }
}
