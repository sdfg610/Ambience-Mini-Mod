package me.molybdenum.ambience_mini.v1_21_1.client.core.render.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.BaseScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.IScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


public class AmScreen extends Screen implements IScreen<Screen, AbstractWidget>
{
    private final BaseScreenSymbiote<Screen, PoseStack.Pose, AbstractWidget, EditBox, Checkbox, Button> symbiote;


    public AmScreen(
            BaseScreenSymbiote<Screen, PoseStack.Pose, AbstractWidget, EditBox, Checkbox, Button> symbiote,
            String screenName
    ) {
        super(Component.literal(screenName));
        this.symbiote = symbiote;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Minecraft Screen overrides
    @Override
    protected void init() {
        super.init();
        symbiote.init();
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
        symbiote.renderBackground(graphics.pose().last(), width, height);
        for (Renderable renderable : this.renderables)
            renderable.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() { // This only fires when pressing Escape
        super.onClose();
        symbiote.onClose();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // IScreen overrides
    @Override
    public Screen getScreen() {
        return this;
    }

    @Override
    public void addWidget(AbstractWidget abstractWidget) {
        addRenderableWidget(abstractWidget);
    }

    @Override
    public int screenWidth() {
        return width;
    }

    @Override
    public int screenHeight() {
        return height;
    }
}
