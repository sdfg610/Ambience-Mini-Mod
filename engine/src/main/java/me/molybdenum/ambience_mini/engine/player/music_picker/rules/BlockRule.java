package me.molybdenum.ambience_mini.engine.player.music_picker.rules;

import me.molybdenum.ambience_mini.engine.player.music_picker.PlaylistChoice;

import java.util.List;
import java.util.Objects;

public record BlockRule(List<Rule> subRules, List<Rule> interrupts) implements Rule {
    @Override
    public PlaylistChoice getNext() {
        return interrupts.stream()
                .map(Rule::getNext)
                .filter(Objects::nonNull)
                .map(PlaylistChoice::asInterrupt)
                .findFirst().orElseGet(() ->
                        subRules.stream()
                                .map(Rule::getNext)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null)
                );
    }
}
