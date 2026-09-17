package me.molybdenum.ambience_mini.engine.client.core.render.screens;

public abstract class BaseScreenSymbiote<
        TScreen,
        TPose,
        TWidget,
        TEditBox extends TWidget,
        TCheckBox extends TWidget,
        TButton extends TWidget
> {
    protected final IGuiTools<TScreen, TPose, TWidget, TEditBox, TCheckBox, TButton> gui;
    protected final IScreen<TScreen, TWidget> screen;


    public BaseScreenSymbiote(
            IGuiTools<TScreen, TPose, TWidget, TEditBox, TCheckBox, TButton> gui,
            String screenName
    ) {
        this.gui = gui;
        this.screen = gui.makeScreen(this, screenName);
    }


    public TScreen getScreen() {
        return screen.getScreen();
    }

    public abstract void init();
    public abstract void tick();
    public abstract void renderBackground(TPose pose, int screenWidth, int screenHeight);
    public abstract void onClose();
}
