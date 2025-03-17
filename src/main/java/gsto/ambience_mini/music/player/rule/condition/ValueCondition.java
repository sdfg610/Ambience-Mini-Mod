package gsto.ambience_mini.music.player.rule.condition;

public record ValueCondition(Object value) implements Condition {
    @Override
    public Object evaluate() {
        return value;
    }
}
