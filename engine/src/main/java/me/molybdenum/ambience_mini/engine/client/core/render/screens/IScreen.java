package me.molybdenum.ambience_mini.engine.client.core.render.screens;

public interface IScreen<TScreen, TWidget> {
    TScreen getScreen();

    void addWidget(TWidget widget);

    int screenWidth();
    int screenHeight();
}
