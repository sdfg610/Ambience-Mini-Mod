package me.molybdenum.ambience_mini.engine.server.core.networking;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.server.core.flags.FlagOperation;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.areas.AreaOperation;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.CreateAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.DeleteAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.GetAreasMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.PutAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.client.ClientInfoMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.features.RequestFeatureFlagsMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.ResponseMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.DeleteFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.GetFlagsMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.PutFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.GetNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.NeoGetNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestMusicChunkMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestServerPlaylistChunkMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music.RequestServerPlaylistInfoMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.GetStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.PutNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.features.Feature;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseServerNetworkManager<TServerPlayer>
{
    // Client versioning
    private final Object lock = new Object();
    private final ConcurrentHashMap<TServerPlayer, AmVersion> playerToVersion = new ConcurrentHashMap<>();

    // Core functionality
    private BaseServerCore<TServerPlayer, ?, ?, ?> core = null;


    public void init(BaseServerCore<TServerPlayer, ?, ?, ?> core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseServerNetworkManager.init'!");
        this.core = core;

        this.core.areaManager.addUpdateListener(this::notifyAreaUpdate);
        this.core.flagManager.addUpdateListener(this::notifyFlagUpdate);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract void sendToPlayer(AmMessage message, TServerPlayer player);

    protected abstract String getServerPlayerUUID(TServerPlayer player);


    // -----------------------------------------------------------------------------------------------------------------
    // Message handling
    public void handleMessage(StrResult<AmMessage> msgRes, TServerPlayer sender) {
        if (!msgRes.isSuccess()) {
            core.logger.error("Error during retrieval of message: {}", msgRes.error);
            return;
        }

        AmMessage message = msgRes.value;
        AmMessage response;
        try {
            if (message instanceof ClientInfoMessage msg)
                response = handleModVersionMessage(msg, sender);
            else if (message instanceof RequestFeatureFlagsMessage msg)
                response = handleRequestFeatureFlagsMessage(msg);

            else if (message instanceof CreateAreaMessage msg)
                response = handleCreateAreaMessage(msg, sender);
            else if (message instanceof PutAreaMessage msg)
                response = handlePutAreaMessage(msg, sender);
            else if (message instanceof DeleteAreaMessage msg)
                response = handleDeleteAreaMessage(msg, sender);
            else if (message instanceof GetAreasMessage msg)
                response = handleGetAreasMessage(msg, sender);

            else if (message instanceof GetStructuresMessage msg)
                response = handleGetStructuresMessage(msg, sender);

            else if (message instanceof GetNameCacheMessage msg)
                response = handleGetNameCacheMessage(msg, sender);
            else if (message instanceof NeoGetNameCacheMessage msg)
                response = handleNeoGetNameCacheMessage(msg);

            else if (message instanceof GetFlagsMessage msg)
                response = handleGetFlagsMessage(msg, sender);

            else if (message instanceof RequestServerPlaylistInfoMessage msg)
                response = handleRequestServerPlaylistInfoMessage(msg);
            else if (message instanceof RequestServerPlaylistChunkMessage msg)
                response = handleRequestServerPlaylistsChunkMessage(msg);
            else if (message instanceof RequestMusicChunkMessage msg)
                response = handleRequestMusicChunkMessage(msg);

            else {
                core.logger.error("Server could not handle message of type '{}'", message.getClass().getName());
                response = message.failure(AmLang.MSG_UNHANDLED_CLIENT_MESSAGE);
            }
        }
        catch (Exception e) {
            core.logger.error("An exception was thrown during message-handling.", e);
            response = message.failure(AmLang.MSG_MESSAGE_CAUSED_SERVER_ERROR);
        }

        if (response != null && message.hasHandler())
            sendToPlayer(response, sender);
    }


    // Basic info
    private AmMessage handleModVersionMessage(ClientInfoMessage msg, TServerPlayer sender) {
        var modVersion = AmVersion.ofString(msg.modVersion);
        setPlayerModVersion(sender, modVersion);
        core.nameCache.putPlayerName(msg.playerUUID, msg.playerName);
        return msg.success();
    }

    private AmMessage handleRequestFeatureFlagsMessage(RequestFeatureFlagsMessage msg) {
        var config = core.serverConfig;
        return msg.succeedWith(
                Arrays.stream(Feature.values())
                        .map(feature -> feature.init(config))
                        .toList()
        );
    }


    // Areas
    private AmMessage handleCreateAreaMessage(CreateAreaMessage msg, TServerPlayer sender) {
        if (core.areaManager.areasDisabled())
            return msg.failure(AmLang.MSG_AREAS_DISABLED.text());

        String owner = msg.area.owner.getOwnerIdIfOwned();
        if (owner != null && !owner.equals(getServerPlayerUUID(sender)))
            return msg.failure(AmLang.MSG_AREA_CANNOT_EDIT);

        var error = core.areaManager.createArea(msg.area);
        if (error.isPresent()) {
            core.logger.error("Error when creating area: {}", error.get());
            return msg.failure(AmLang.MSG_MESSAGE_CAUSED_SERVER_ERROR);
        }

        return msg.success();
    }

    private AmMessage handlePutAreaMessage(PutAreaMessage msg, TServerPlayer sender) {
        if (core.areaManager.areasDisabled())
            return msg.failure(AmLang.MSG_AREAS_DISABLED.text());

        String senderID = getServerPlayerUUID(sender);
        if (!msg.area.canBeEditedBy(senderID))
            return msg.failure(AmLang.MSG_AREA_CANNOT_EDIT);

        var error = core.areaManager.putArea(msg.area);
        if (error.isPresent()) {
            core.logger.error("Error when updating area: {}", error.get());
            return msg.failure(AmLang.MSG_MESSAGE_CAUSED_SERVER_ERROR);
        }

        return msg.success();
    }

    private AmMessage handleDeleteAreaMessage(DeleteAreaMessage msg, TServerPlayer sender) {
        if (core.areaManager.areasDisabled())
            return msg.failure(AmLang.MSG_AREAS_DISABLED.text());

        var result = core.areaManager.getAreaById(msg.areaId);
        if (result.isFailure())
            return msg.failure(result.error);

        var area = result.value;
        if (!area.canBeEditedBy(getServerPlayerUUID(sender)))
            return msg.failure(AmLang.MSG_AREA_CANNOT_EDIT);

        core.areaManager.deleteArea(msg.areaId);
        return msg.success();
    }

    private AmMessage handleGetAreasMessage(GetAreasMessage msg, TServerPlayer sender) {
        if (core.areaManager.areasDisabled())
            return msg.failure(AmLang.MSG_AREAS_DISABLED.text());

        var res = core.areaManager.getAreasVisibleTo(getServerPlayerUUID(sender));
        if (res.isFailure())
            return msg.failWith(res.error);

        res.value.forEach(
                area -> sendToPlayer(new PutAreaMessage(area, false), sender)
        );

        return msg.success();
    }


    // Structures
    private AmMessage handleGetStructuresMessage(GetStructuresMessage msg, TServerPlayer sender) {
        sendToPlayer(
                msg.getReferences
                        ? core.structureReader.getReferences(sender, msg.chunksToFetch)
                        : core.structureReader.getStructures(sender, msg.chunksToFetch),
                sender
        );
        return msg.success();
    }


    // Name caching
    private AmMessage handleGetNameCacheMessage(GetNameCacheMessage msg, TServerPlayer sender) {
        String uuid = msg.playerUuid;
        String name = core.nameCache.getPlayerName(uuid);
        sendToPlayer(new PutNameCacheMessage(uuid, name), sender);
        return msg.success();
    }

    private AmMessage handleNeoGetNameCacheMessage(NeoGetNameCacheMessage msg) {
        return msg.succeedWith(core.nameCache.getPlayerName(msg.playerUuid).getBytes(StandardCharsets.UTF_8));
    }


    // Flags
    private AmMessage handleGetFlagsMessage(GetFlagsMessage msg, TServerPlayer sender) {
        var res = core.flagManager.getFlags();
        if (res.isFailure())
            return msg.failure(res.error);

        for (var elem : res.value)
            sendToPlayer(new PutFlagMessage(elem.getKey(), elem.getValue().asString().orElse(null), false), sender);
        return msg.success();
    }


    // Remote music
    private ResponseMessage handleRequestServerPlaylistInfoMessage(RequestServerPlaylistInfoMessage msg) {
        var manager = core.musicManager;
        return msg.succeedWith(
                manager.hasServerPlaylists()
                        ? msg.hasPlaylists(manager.getServerPlaylistByteSize(), manager.getServerPlaylistCount())
                        : msg.noPlaylists()
        );
    }

    private ResponseMessage handleRequestServerPlaylistsChunkMessage(RequestServerPlaylistChunkMessage msg) {
        return msg.succeedWith(core.musicManager.getServerPlaylistChunk(msg.offset, msg.byteLength));
    }

    private ResponseMessage handleRequestMusicChunkMessage(RequestMusicChunkMessage msg) {
        var res = BaseMusicProvider.validatePath(msg.musicPath);
        if (res.isFailure())
            return msg.failWith("Got request for music on invalid path '" + msg.musicPath + "'! This should not be possible!");

        var chunkRes = core.musicManager.getMusicData(msg.musicPath, msg.offset, msg.length);
        return chunkRes.match(msg::succeedWith, msg::failWith);
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Notification to players
    public void notifyAreaUpdate(Area area, AreaOperation operation) {
        playerToVersion.forEach((player, version) -> {
            if (version.isGreaterThanOrEqual(AmVersion.V_2_5_0)) {
                switch (operation) {
                    case PUT -> {
                        AmMessage msg = area.canBeSeenBy(getServerPlayerUUID(player))
                                ? new PutAreaMessage(area)
                                : new DeleteAreaMessage(area.id);
                        sendToPlayer(msg, player);
                    }
                    case DELETE -> sendToPlayer(new DeleteAreaMessage(area.id), player);
                }
            }
        });
    }
    public void notifyFlagUpdate(FlagOperation op) {
        playerToVersion.forEach((player, version) -> {
            if (version.isGreaterThanOrEqual(AmVersion.V_2_6_0)) {
                if (op instanceof FlagOperation.Put put)
                    sendToPlayer(new PutFlagMessage(put.id(), put.value(), true), player);
                else if (op instanceof FlagOperation.Delete delete)
                    sendToPlayer(new DeleteFlagMessage(delete.id()), player);
                else
                    throw new RuntimeException("Could not handle unknown flag operation: " + op.getClass().getName());
            }
        });
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Client info management
    public void setPlayerModVersion(TServerPlayer player, AmVersion version) {
        synchronized (lock) {
            playerToVersion.put(player, version);
        }
    }

    public void renewPlayerModVersion(TServerPlayer oldPlayer, TServerPlayer newPlayer) {
        synchronized (lock) {
            var info = playerToVersion.remove(oldPlayer);
            if (info != null)
                playerToVersion.put(newPlayer, info);
        }
    }

    public void removePlayerModVersion(TServerPlayer player) {
        synchronized (lock) {
            playerToVersion.remove(player);
        }
    }


    public AmVersion getPlayerModVersion(TServerPlayer player) {
        synchronized (lock) {
            return playerToVersion.getOrDefault(player, AmVersion.ZERO);
        }
    }
}
