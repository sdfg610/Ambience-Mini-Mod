package me.molybdenum.ambience_mini.engine.loader.compiler;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed.*;
import me.molybdenum.ambience_mini.engine.player.Music;
import me.molybdenum.ambience_mini.engine.player.rule.BlockRule;
import me.molybdenum.ambience_mini.engine.player.rule.PlayRule;
import me.molybdenum.ambience_mini.engine.player.rule.Rule;
import me.molybdenum.ambience_mini.engine.player.rule.WhenRule;
import me.molybdenum.ambience_mini.engine.player.rule.condition.*;
import me.molybdenum.ambience_mini.engine.state.providers.BaseGameStateProvider;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public record Compiler(String musicDirectory, BaseGameStateProvider gameStateProvider) {
    public Rule Conf(Conf conf) {
        return Conf(conf, new HashMap<>());
    }

    public Rule Conf(Conf conf, HashMap<String, List<Music>> playlists) {
        if (conf instanceof Playlist playlist) {
            String name = playlist.ident().value();
            List<Music> list = PL(playlist.playlist(), playlists).toList();

            playlists.put(name, list);

            return Conf(playlist.conf(), playlists);
        }
        else if (conf instanceof Schedule schedule)
            return Shed(schedule.schedule(), playlists);

        throw new RuntimeException("Unhandled Conf-type: " + conf.getClass().getCanonicalName());
    }

    private Stream<Music> PL(PL play, HashMap<String, List<Music>> playlists) {
        if (play instanceof IdentP ident)
            return playlists.get(ident.value()).stream();
        else if (play instanceof Concat concat)
            return Stream.concat(
                    PL(concat.left(), playlists),
                    PL(concat.right(), playlists)
            );
        else if (play instanceof Load load)
            return Stream.of(new Music(getMusicPath(load.file().value()), load.gain() != null ? load.gain().value() : 0f));
        else if (play instanceof Nil)
            return Stream.empty();

        throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private Rule Shed(Shed shed, HashMap<String, List<Music>> playlists) {
        if (shed instanceof Play play)
            return new PlayRule(PL(play.playlist(), playlists).toList(), play.isInstant());
        else if (shed instanceof Block block) {
            ArrayList<Rule> interrupts = new ArrayList<>();
            ArrayList<Rule> subRules = new ArrayList<>();

            for (var sh : block.body())
                if (sh instanceof Interrupt inter)
                    interrupts.add(Shed(inter.shed(), playlists));
                else
                    subRules.add(Shed(sh, playlists));

            return new BlockRule(subRules, interrupts);
        }
        else if (shed instanceof When when)
            return new WhenRule(
                    Expr(when.condition()),
                    Shed(when.body(), playlists)
            );

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    public Condition Expr(Expr expr) {
        if (expr instanceof BoolV boolV)
            return new ValueCondition(boolV.value());
        else if (expr instanceof IntV intV)
            return new ValueCondition(intV.value());
        else if (expr instanceof FloatV floatV)
            return new ValueCondition(floatV.value());
        else if (expr instanceof StringV stringV)
            return new ValueCondition(stringV.value());
        else if (expr instanceof Ev ev)
            return new EventCondition(gameStateProvider.getEvent(ev.eventName().value()));
        else if (expr instanceof Get property)
            return new PropertyCondition(gameStateProvider.getProperty(property.propertyName().value()));
        else if (expr instanceof BinaryOp binOp)
            return new BinOpCondition(binOp.op(), Expr(binOp.left()), Expr(binOp.right()));

        throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }


    private Path getMusicPath(String musicName) {
        if (!musicName.endsWith(".mp3"))
            return Path.of(musicDirectory, musicName + ".mp3");
        return Path.of(musicDirectory, musicName);
    }
}
