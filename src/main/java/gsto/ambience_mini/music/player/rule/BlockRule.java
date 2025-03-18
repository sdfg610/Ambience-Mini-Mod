package gsto.ambience_mini.music.player.rule;

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
