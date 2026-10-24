package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu;

import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general.Label;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfoCache;

import java.util.function.Consumer;

public class MusicButton extends AmWidget
{
    public static final int MARGIN = 5;

    private final MusicInstance inst;
    private final Consumer<MusicInstance> onClick;

    private final Label lblTitle, lblAuthor, lblDuration;


    public MusicButton(int x, int y, int width, MusicInstance inst, Consumer<MusicInstance> onClick) {
        super(x, y, width, getHeight());
        this.inst = inst;
        this.onClick = onClick;

        var h = tools.getLineHeight();
        var w = width-MARGIN*2;
        lblTitle = new Label(MARGIN, MARGIN, w, h, inst.music.path());
        lblAuthor = new Label(MARGIN, MARGIN+h, w, h, AmLang.STRING_LOADING.translate());
        lblDuration = new Label(0, MARGIN+h+3, 0, h, "");

        inst.getMusicInfo(this::setMusicInfo);
    }

    private void setMusicInfo(MusicInfo info) {
        var title = info.getTitleOrNull();
        var author = info.getAuthorOrNull();
        lblTitle.setText(title == null ? inst.music.path() : title);
        lblAuthor.setText(AmLang.STRING_BY_X.translate(author == null ? "N/A" : author));
        lblDuration.setText(AmLang.STRING_TIME_X.translate(info.getDurationString()));

        lblDuration.autoSize();
        lblDuration.x = width - MARGIN - lblDuration.width;
        lblAuthor.width = width - MARGIN*3 - lblDuration.width;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY))
            renderRectangle(drawer, x, y, width, height, Color.WHITE_128);
        renderBorder(drawer, x, y, width, height, 1);

        var offX = mouseX-x;
        var offY = mouseY-y;
        drawer.withOffset(x, y, 0, () -> {
            lblTitle.render(drawer, offX, offY);
            lblAuthor.render(drawer, offX, offY);
            lblDuration.render(drawer, offX, offY);
        });
    }

    @Override
    public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        if (mouseKeyIndex == GuiConstants.MOUSE_LEFT && isMouseOver(mouseX, mouseY)) {
            tools.PlayMouseClick();
            onClick.accept(inst);
            return this;
        }
        return null;
    }

    private static int getHeight() {
        return tools.getLineHeight()*2 + MARGIN*2;
    }
}
