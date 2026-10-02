package me.molybdenum.ambience_mini.v1_21_1.client.core.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.AmMenu;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.McScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class NeoAmScreen extends Screen implements McScreen
{
    private final AmMenu<PoseStack> menu;


    public NeoAmScreen(
            AmMenu<PoseStack> menu, String menuName
    ) {
        super(Component.literal(menuName));
        this.menu = menu;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Minecraft.Screen overrides
    @Override
    public void init() {
        menu.onInit();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        menu.setPose(graphics.pose());
        menu.render(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseKeyIndex) {
        return menu.mousePressed(mouseX, mouseY, mouseKeyIndex);
    }

    @Override
    public boolean keyPressed(int keyIndex, int noIdeaWhatThisIs1, int noIdeaWhatThisIs2) {
        if (menu.keyPressed(keyIndex))
            return true;

        if (keyIndex == 256 && this.shouldCloseOnEsc()) {
            onClose();
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(char ch, int noIdeaWhatThisIs) {
        return menu.charTyped(ch);
    }

    @Override
    public void onClose() { // This only fires when pressing Escape
        super.onClose();
        menu.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    //------------------------------------------------------------------------------------------------------------------
    // AmbienceMini.IScreen overrides
    @Override
    public void open() {
        Minecraft.getInstance().setScreen(this);
    }

    @Override
    public void close() {
        Minecraft.getInstance().setScreen(null);
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
