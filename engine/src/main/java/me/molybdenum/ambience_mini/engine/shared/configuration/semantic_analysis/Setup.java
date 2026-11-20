package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;

public sealed interface Setup {
    record Client(BaseGameStateProvider gameStateProvider) implements Setup {}
    record IntegratedServer() implements Setup {}
    record DedicatedServer() implements Setup {}
}
