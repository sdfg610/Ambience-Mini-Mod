package me.molybdenum.ambience_mini.engine.client.core.gui.menus;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.AmMenu;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.McScreenBuilder;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general.*;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu.CacheDisplay;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu.MusicBar;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu.MusicButton;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.music.Monitor;
import me.molybdenum.ambience_mini.engine.client.core.music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.client.core.music.player.MusicInstance;
import me.molybdenum.ambience_mini.engine.client.core.music.player.PlayState;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.core.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class MainMenu<TPoseStack> extends AmMenu<TPoseStack>
{
    private static final int MENU_WIDTH = 380;
    private static final int MENU_HEIGHT = 220;

    private static final int INNER_WIDTH = MENU_WIDTH-MENU_INNER_MARGIN*2;

    @SuppressWarnings("rawtypes")
    private final BaseClientCore core;
    private final BaseNotification<?> notification;
    private final Function<Music, MusicInstance> instantiate;

    private final TabPanel tabPanel;


    // Player tab
    private final String strPlay, strPause;
    private final Label lblNotRunning;
    private final Panel playerPanel;
    private final Label lblMusic, lblAuthor;
    private final MusicBar musicBar;
    private final Button btnPlayPause;
    private final ScrollPanel scrPlaylist, scrPresets;

    private Monitor monitor;
    private boolean wasPaused;

    private PlayState currentPlaystate;
    private List<Music> currentPlaylist;


    // Server music tab
    private final Label lblMemUsage;
    private final ScrollPanel scrCacheEntries;

    private long latestCacheUpdate;

    private final ServerMusicCache musicCache;
    private final String maxMemUsageStr;



    public MainMenu(
            BaseDrawer<TPoseStack> drawer,
            McScreenBuilder<TPoseStack> builder,
            @SuppressWarnings("rawtypes") BaseClientCore core
    ) {
        super(drawer, "AM menu", builder);

        this.core = core;
        this.notification = core.notification;
        this.instantiate = core::instantiate;

        tabPanel = addWidget(new TabPanel(MENU_WIDTH, MENU_HEIGHT));
        tabPanel.backgroundColor = Color.BLACK_128;

        var tabPlayer = tabPanel.createTab("Player");
        //var tabAreas = tabPanel.createTab("Areas");   // TODO: Area and flags tab. 2.8.1+
        //var tabFlags = tabPanel.createTab("Flags");
        var tabCache = tabPanel.createTab("Server music");

        // Player tab
        Button btnReload = tabPlayer.addWidget(new Button(core.notification.translateFromKey(AmLang.STRING_RELOAD), this::onReloadClicked));
        btnReload.autoSize(5, 3);
        btnReload.y = MENU_INNER_MARGIN/2;
        btnReload.x = tabPanel.width-btnReload.width - MENU_INNER_MARGIN/2;

        lblNotRunning = tabPlayer.addWidget(new Label(0, 0, 0, 0, notification.translateFromKey(AmLang.MSG_NOT_RUNNING)));
        lblNotRunning.autoSize();
        lblNotRunning.autoCenter(MENU_WIDTH, tabPanel.getUsableHeight());
        lblNotRunning.setVisible(false);

        playerPanel = tabPlayer.addWidget(new Panel(0, 0, MENU_WIDTH, tabPanel.getUsableHeight()));
        playerPanel.backgroundColor = Color.TRANSPARENT;
        playerPanel.borderThickness = 0;

        lblMusic = playerPanel.addWidget(new Label(MENU_INNER_MARGIN,MENU_INNER_MARGIN, INNER_WIDTH-btnReload.width, tools.getLineHeight()+1, "N/A"));
        lblAuthor = playerPanel.addWidget(new Label(MENU_INNER_MARGIN, lblMusic.bottom(), INNER_WIDTH-btnReload.width,tools.getLineHeight()+1, "By: N/A"));
        musicBar = playerPanel.addWidget(new MusicBar(MENU_INNER_MARGIN, lblAuthor.bottom() + BASE_SEPARATION, INNER_WIDTH, tools.getLineHeight()+2));

        strPlay = notification.translateFromKey(AmLang.STRING_PLAY);
        strPause = notification.translateFromKey(AmLang.STRING_PAUSE);
        Panel buttonPanel = playerPanel.addWidget(new Panel(0, musicBar.bottom()+BASE_SEPARATION, 0,0));
        buttonPanel.borderThickness = 0;
        btnPlayPause = buttonPanel.addWidget(new Button(strPause, this::onPlayPauseClicked));
        btnPlayPause.autoSize(10, 4);
        Button btnPickRandom = buttonPanel.addWidget(new Button(notification.translateFromKey(AmLang.STRING_PICK_RANDOM), this::onPickRandomClicked));
        btnPickRandom.autoSize(10, 4);
        btnPickRandom.x = btnPlayPause.right()+BASE_SEPARATION;
        buttonPanel.autoSize();
        buttonPanel.x = (MENU_WIDTH-buttonPanel.width)/2;

        var playlistY = buttonPanel.bottom() + BASE_SEPARATION;
        var playlistWidth = (MENU_WIDTH*2)/3;
        scrPlaylist = playerPanel.addWidget(new ScrollPanel(0, playlistY, playlistWidth, tabPanel.getUsableHeight()-playlistY));

        scrPresets = playerPanel.addWidget(new ScrollPanel(playlistWidth-1, playlistY, MENU_WIDTH - playlistWidth+1, tabPanel.getUsableHeight()-playlistY));
        // TODO: Presets

        // Areas tab

        // Flags tab

        // Server music cache tab
        musicCache = core.musicCache;
        maxMemUsageStr = Utils.bytesToMB(musicCache.getMaxMemoryUsage());

        Button btnClearCache = tabCache.addWidget(new Button(core.notification.translateFromKey(AmLang.STRING_CLEAR_CACHE), this::onClearClicked));
        btnClearCache.autoSize(5, 3);
        btnClearCache.y = MENU_INNER_MARGIN/2;
        btnClearCache.x = tabPanel.width-btnClearCache.width - MENU_INNER_MARGIN/2;

        var scrollPanelY = btnClearCache.bottom() + MENU_INNER_MARGIN/2;
        scrCacheEntries = tabCache.addWidget(new ScrollPanel(
                0, scrollPanelY,
                MENU_WIDTH, tabPanel.getUsableHeight()-scrollPanelY
        ));

        var labelH = tools.getLineHeight() + 2;
        var spacing = (scrollPanelY-labelH)/2+1;
        lblMemUsage = tabCache.addWidget(new Label(
                spacing, spacing,
                INNER_WIDTH, labelH,
                ""
        ));
    }



    @Override
    public void onInit() {
        // Positioning of the menu
        tabPanel.x = (screen.screenWidth() - tabPanel.width)/2;
        tabPanel.y = (screen.screenHeight() - tabPanel.height - tabPanel.getMaxIdleTabButtonHeight())/2;

        tick();
    }

    @Override
    public void tick() {
        tickPlayerTab();
        tickMusicCacheTab();
    }

    private void tickPlayerTab() {
        monitor = core.getMonitor();
        var monitorRunning = monitor != null && monitor.isRunning();
        playerPanel.setVisible(monitorRunning);
        lblNotRunning.setVisible(!monitorRunning);
        if (!monitorRunning) {
            monitor = null;
            return;
        }

        boolean isPaused = monitor.isPaused();
        if (wasPaused != isPaused) {
            btnPlayPause.setText(isPaused ? strPlay : strPause);
            wasPaused = isPaused;
        }

        var playlist = monitor.getSelectedPlaylist();
        if (playlist != currentPlaylist) {
            currentPlaylist = playlist;
            scrPlaylist.clearWidgets();

            if (currentPlaylist != null) {
                var x = MENU_INNER_MARGIN/2;
                var y = MENU_INNER_MARGIN/2;
                var width = scrPlaylist.getUsableWidth() - MENU_INNER_MARGIN;
                for (var music : currentPlaylist)
                    y += scrPlaylist.addWidget(new MusicButton(x, y, width, instantiate.apply(music), this::onMusicButtonClicked)).height + BASE_SEPARATION;
            }

            scrPlaylist.refreshScroll();
        }

        var playState = monitor.getPlayState();
        if (playState != null && currentPlaystate != playState) {
            currentPlaystate = playState;
            lblMusic.setText(playState.getTitleOrPath());
            String author = playState.getAuthorOrNull();
            lblAuthor.setText(notification.translateFromKeyAndArgs(AmLang.STRING_BY_X, author == null ? "N/A" : author));

            musicBar.reset();
            musicBar.setSampleRate(playState.getSamplesPerSecond());

            if (playState.isPlaying())
                musicBar.setLoopPoints(playState.getLoopStart(), playState.getLoopEnd());
        }
        else if (playState == null && currentPlaystate != null) {
            currentPlaystate = null;
            String none = notification.translateFromKey(AmLang.STRING_NONE);
            lblMusic.setText(none);
            lblAuthor.setText(notification.translateFromKeyAndArgs(AmLang.STRING_BY_X, none));

            musicBar.reset();
        }

        if (playState != null) {
            musicBar.setCurrentTime(playState.getSamplePosition());

            if (musicBar.getTotalTimeSeconds() < 0)
                musicBar.setTotalTime(playState.getTotalSamples());
        }
    }

    private void tickMusicCacheTab() {
        lblMemUsage.setText(AmLang.STRING_MEMORY_USE.translate(Utils.bytesToMB(musicCache.getMemoryUsage()), maxMemUsageStr));

        var newCacheTime = musicCache.getLatestCacheUpdate();
        if (newCacheTime > latestCacheUpdate) {
            latestCacheUpdate = newCacheTime;

            scrCacheEntries.clearWidgets();
            var x = MENU_INNER_MARGIN/2;
            var y = MENU_INNER_MARGIN/2;
            var width = scrCacheEntries.getUsableWidth() - MENU_INNER_MARGIN;
            for (var path : musicCache.getCachedMusicPaths())
                y += scrCacheEntries.addWidget(new CacheDisplay(
                        x, y, width, instantiate.apply(new Music(path, true))
                )).height + BASE_SEPARATION;
            scrCacheEntries.refreshScroll();
        }
    }

    @Override
    public void onClose() {

    }


    //------------------------------------------------------------------------------------------------------------------
    // Button actions

    // Player tab
    private void onReloadClicked() {
        core.notification.showToast(AmLang.MSG_RELOAD);
        Monitor.enableAutoRestart();
        core.tryReloadMusicEngine();
    }

    private void onPlayPauseClicked() {
        if (monitor != null) {
            if (monitor.isPaused())
                monitor.resume();
            else
                monitor.pause();
        }
    }

    private void onPickRandomClicked() {
        if (monitor != null) {
            core.notification.showToast(AmLang.MSG_NEXT_MUSIC);
            monitor.forceSelectNewMusic();
            monitor.resume();
        }
    }

    private void onMusicButtonClicked(MusicInstance inst) {
        if (monitor != null) {
            core.notification.showToast(AmLang.MSG_NEXT_MUSIC);
            monitor.forceSelectNewMusic(inst.music);
            monitor.resume();
        }
    }

    // Server music tab
    private void onClearClicked() {
        core.notification.showToast(AmLang.MSG_RELOAD);
        Monitor.enableAutoRestart();
        core.getMonitor().stop();
        core.musicCache.clearOnlyCache();
        core.tryReloadMusicEngine();
    }
}
