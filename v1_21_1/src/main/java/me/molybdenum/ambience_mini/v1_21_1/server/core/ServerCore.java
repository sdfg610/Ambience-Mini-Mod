package me.molybdenum.ambience_mini.v1_21_1.server.core;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.server.core.locations.ServerAreaManager;
import me.molybdenum.ambience_mini.engine.server.core.misc.ServerNameCache;
import me.molybdenum.ambience_mini.engine.server.core.music.ServerMusicManager;
import me.molybdenum.ambience_mini.v1_21_1.server.core.locations.StructureReader;
import me.molybdenum.ambience_mini.v1_21_1.server.core.networking.ServerNetworkManager;
import me.molybdenum.ambience_mini.v1_21_1.server.core.setup.ServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.Path;

public class ServerCore extends BaseServerCore<
        ServerPlayer,
        ServerConfig,
        ServerNetworkManager,
        StructureReader
> {
    private final MinecraftServer server;


    public ServerCore(
            MinecraftServer server,
            Logger logger,
            ServerConfig serverConfig,
            ServerNameCache nameCache,
            ServerAreaManager areaManager,
            StructureReader structureReader,
            FlagManager flagManager,
            ServerMusicManager musicManager,
            ServerNetworkManager networkManager
    ) {
        super(logger, nameCache, serverConfig, areaManager, structureReader, flagManager, musicManager, networkManager);

        this.server = server;
    }


    @Override
    public boolean isDedicatedServer() {
        return server.isDedicatedServer();
    }

    @Override
    public Path getWorldRootPath() {
        return server.getWorldPath(LevelResource.ROOT).normalize();
    }
}
