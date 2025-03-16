package gsto.ambience_mini.music.loader.abstract_syntax.shed;

import gsto.ambience_mini.music.loader.abstract_syntax.expr.Expr;

public record When(Expr condition, Shed body) implements Shed { }
