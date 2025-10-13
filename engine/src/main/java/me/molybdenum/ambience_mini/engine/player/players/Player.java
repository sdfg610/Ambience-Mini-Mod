package me.molybdenum.ambience_mini.engine.player.players;

public interface Player {
    boolean isPlaying();

    void play();
    void pause();
    void stop();

    void setGain(float gain);
}
