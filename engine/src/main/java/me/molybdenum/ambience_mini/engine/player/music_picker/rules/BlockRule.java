package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;
import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;

import java.util.List;
import java.util.Objects;

public record BlockRule(List<Rule> subRules, List<Rule> interrupts) implements Rule {
    @Override
    public PlaylistChoice getNext(VarEnv env) {
        return interrupts.stream()
                .map(rule -> rule.getNext(env))
                .filter(Objects::nonNull)
                .map(PlaylistChoice::asInterrupt)
                .findFirst().orElseGet(() ->
                        subRules.stream()
                                .map(rule -> rule.getNext(env))
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null)
                );
    }
}
