package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general.Label;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.music.info.MusicInfo;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;

import java.io.FileNotFoundException;

public class CacheDisplay extends AmWidget
{
    public static final int MARGIN = 5;

    private final Label lblTitle, lblAuthor, lblSize;

    private final MusicInstance inst;



    public CacheDisplay(int x, int y, int width, MusicInstance inst) {
        super(x, y, width, getHeight());
        this.inst = inst;

        var h = tools.getLineHeight();
        var w = width-MARGIN*2;
        lblTitle = new Label(MARGIN, MARGIN, w, h, inst.music.path());
        lblAuthor = new Label(MARGIN, MARGIN+h, w, h, AmLang.STRING_LOADING.translate());
        lblSize = new Label(0, MARGIN+h+1, 0, h, AmLang.STRING_SIZE_X.translate(tryGetSize(inst)));

        lblSize.autoSize();
        lblSize.x = width - MARGIN - lblSize.width;
        lblAuthor.width = width - MARGIN*3 - lblSize.width;

        inst.getMusicInfo(this::setMusicInfo);
    }

    private static String tryGetSize(MusicInstance inst) {
        try {
            return Utils.bytesToMB(inst.getMusicSize());
        } catch (FileNotFoundException e) {
            return "?? MB";
        }
    }

    private void setMusicInfo(MusicInfo info) {
        var title = info.getTitleOrNull();
        var author = info.getAuthorOrNull();
        lblTitle.setText(title == null ? inst.music.path() : title);
        lblAuthor.setText(AmLang.STRING_BY_X.translate(author == null ? "N/A" : author));
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        renderBorder(drawer, x, y, width, height, 1);

        var offX = mouseX-x;
        var offY = mouseY-y;
        drawer.withOffset(x, y, 0, () -> {
            lblTitle.render(drawer, offX, offY);
            lblAuthor.render(drawer, offX, offY);
            lblSize.render(drawer, offX, offY);
        });
    }




    private static int getHeight() {
        return MARGIN + tools.getLineHeight()*2 + MARGIN;
    }
}
