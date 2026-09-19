package me.molybdenum.ambience_mini.engine.client.core.render.screens;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public interface IGuiTools<
        TScreen,
        TPose,
        TWidget,
        TEditBox extends TWidget,
        TCheckBox extends TWidget,
        TButton extends TWidget
> {
    // Screen tools
    IScreen<TScreen, TWidget> makeScreen(
            BaseScreenSymbiote<TScreen, TPose, TWidget, TEditBox, TCheckBox, TButton> symbiote,
            String screenName
    );
    void closeScreen();

    // -----------------------------------------------------------------------------------------------------------------
    // Widget tools
    TEditBox makeTextBox(Vector2i size, String content);
    TCheckBox makeCheckBox(boolean selected, String label);
    TButton makeButton(Vector2i size, String content, Runnable onClick);

    void setWidgetPos(TWidget widget, int x, int y);
    void setWidgetEnabled(TWidget widget, boolean enabled);

    void setButtonText(TButton button, String text);

    void tickEditBox(TEditBox editBox);
    String getEditBoxValue(TEditBox editBox);

    void setSelected(TCheckBox checkBox, boolean selected);
    boolean getSelected(TCheckBox checkBox);
}
