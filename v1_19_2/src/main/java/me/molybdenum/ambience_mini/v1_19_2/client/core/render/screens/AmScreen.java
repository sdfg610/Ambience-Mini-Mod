package me.molybdenum.ambience_mini.v1_19_2.client.core.render.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.BaseScreenSymbiote;
import me.molybdenum.ambience_mini.engine.client.core.render.screens.IScreen;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;


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
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        symbiote.renderBackground(poseStack.last(), width, height);
        super.render(poseStack, mouseX, mouseY, partialTick);
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
