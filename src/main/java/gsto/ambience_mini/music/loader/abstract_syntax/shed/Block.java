package gsto.ambience_mini.music.loader.abstract_syntax.shed;

import java.util.List;

public record Block(List<Shed> body) implements Shed { }
