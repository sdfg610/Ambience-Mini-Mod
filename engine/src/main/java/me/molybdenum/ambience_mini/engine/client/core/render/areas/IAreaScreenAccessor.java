package me.molybdenum.ambience_mini.engine.client.core.render.areas;

public interface IAreaScreenAccessor<TEditBox, TCheckBox, TButton> {
    void addTextBox(TEditBox editBox);
    void addCheckBox(TCheckBox checkBox);
    void addButton(TButton button);

    int screenWidth();
    int screenHeight();
}
