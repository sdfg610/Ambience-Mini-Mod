package me.molybdenum.ambience_mini.v1_21_1.client.core.render.drawer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lwjgl.opengl.GL11;

public class Drawer extends BaseDrawer<PoseStack>
{
    private final Minecraft mc = Minecraft.getInstance();

    private PoseStack poseStack;
    private BufferBuilder builder = null;
    private final MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();


    @Override
    public void setup(PoseStack poseStack) {
        this.poseStack = poseStack;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Lines
    @Override
    protected void beginLineBuilder() {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(Constants.AREA_LINE_WIDTH);

        builder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
    }

    @Override
    protected void drawLine(Vector3i first, Vector3i last, Color color) {
        var size = getNormalSize(first, last);
        var normal = poseStack.last();
        var pose = normal.pose();

        builder.addVertex(pose, first.x(), first.y(), first.z())
                .setColor(color.r, color.g, color.b, color.a)
                .setNormal(normal, size.first(), size.second(), size.third());

        builder.addVertex(pose, last.x(), last.y(), last.z())
                .setColor(color.r, color.g, color.b, color.a)
                .setNormal(normal, size.first(), size.second(), size.third());
    }

    @Override
    protected void endLineBuilder() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
        builder = null;
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Quads
    @Override
    protected void beginQuadBuilder() {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    protected void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color) {
        var pose = poseStack.last().pose();

        builder.addVertex(pose, p1.x(), p1.y(), p1.z())
                .setColor(color.r, color.g, color.b, color.a);

        builder.addVertex(pose, p2.x(), p2.y(), p2.z())
                .setColor(color.r, color.g, color.b, color.a);

        builder.addVertex(pose, p3.x(), p3.y(), p3.z())
                .setColor(color.r, color.g, color.b, color.a);

        builder.addVertex(pose, p4.x(), p4.y(), p4.z())
                .setColor(color.r, color.g, color.b, color.a);
    }

    @Override
    protected void endQuadBuilder() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
        builder = null;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Text
    @Override
    protected void beginTextBuilder() { }

    @Override
    protected void drawText(String text, Vector2i position, Color color) {
        mc.font.drawInBatch(text, (float)position.x(), (float)position.y(), color.toABGR32(), false, poseStack.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    protected void endTextBuilder() {
        buffer.endBatch();
    }



    @Override
    public int getTextWidth(String text) {
        return mc.font.width(text);
    }

    @Override
    public int getLineHeight() {
        return mc.font.lineHeight;
    }

    @Override
    public String headSubstringByWidth(String str, int width) {
        return mc.font.plainSubstrByWidth(str, width);
    }

    @Override
    public String tailSubstringByWidth(String str, int width) {
        return mc.font.plainSubstrByWidth(str, width, true);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Cropping
    @Override
    protected void enableScissor(int x, int y, int width, int height) {
        // Render system's y-coordinate is from bottom of the screen. Practically everything else measures y from the top.
        var trueY = mc.getWindow().getHeight()-y-height;
        RenderSystem.enableScissor(x, trueY, width, height);
    }

    @Override
    protected void disableScissor() {
        RenderSystem.disableScissor();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Offset
    @Override
    protected void pushOffset(float x, float y, float z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
    }

    @Override
    protected void popOffset() {
        poseStack.popPose();
    }

    @Override
    protected double getGuiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }
}
