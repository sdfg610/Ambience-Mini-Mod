package me.molybdenum.ambience_mini.engine.client.core.networking;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.*;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseClientNetworkManager
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;

    private static final AtomicInteger UNIQUE_ID_GEN = new AtomicInteger();
    private final ConcurrentHashMap<Integer, Pair<Runnable, Runnable>> handlers = new ConcurrentHashMap<>();


    @SuppressWarnings("rawtypes")
    public void init(BaseClientCore core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseClientNetworkManager.init'!");
        this.core = core;
    }


    public void sendToServer(AmMessage message) {
        message.handlerID = Integer.MIN_VALUE;
        sendToServerInternal(message);
    }

    public void sendToServer(AmMessage message, Runnable onSuccess, Runnable onFailure) {
        // TODO: Remove old, unused handlers?
        message.handlerID = UNIQUE_ID_GEN.getAndIncrement();
        handlers.put(message.handlerID, new Pair<>(onSuccess, onFailure));
        sendToServerInternal(message);
    }

    protected abstract void sendToServerInternal(AmMessage message);


    public void handleMessage(Result<AmMessage> msgRes) {
        if (!msgRes.isSuccess()) {
            core.notification.printTranslatableToChat(AmLang.MSG_UNHANDLED_MESSAGE);
            core.logger.error(msgRes.error);
            return;
        }

        AmMessage message = msgRes.value;
        if (message instanceof FailureMessage msg)
            handleFailureMessage(msg);
        if (message instanceof SuccessMessage msg)
            handleSuccessMessage(msg);

        else if (message instanceof MobTargetMessage msg)
            handleMobTargetMessage(msg);
        else if (message instanceof PutAreaMessage msg)
            handlePutAreaMessage(msg);
        else if (message instanceof DeleteAreaMessage msg)
            handleDeleteAreaMessage(msg);

        else if (message instanceof PutChunkReferenceMessage msg)
            handlePutChunkReferenceMessage(msg);
        else if (message instanceof PutChunkStructuresMessage msg)
            handlePutChunkStructuresMessage(msg);

        else if (message instanceof PutNameCacheMessage msg)
            handlePutNameCacheMessage(msg);

        else {
            core.notification.printTranslatableToChat(AmLang.MSG_UNHANDLED_MESSAGE);
            core.logger.error("Client network handler could not handle message of type: {}", message.getClass().getName());
        }
    }


    // Success and failure responses
    private void handleFailureMessage(FailureMessage msg) {
        core.notification.printTranslatableToChat(msg.translationKey, msg.arguments);
        var handler = handlers.remove(msg.handlerID);
        if (handler != null)
            handler.right().run();
    }

    private void handleSuccessMessage(SuccessMessage msg) {
        var handler = handlers.remove(msg.handlerID);
        if (handler != null)
            handler.left().run();
    }


    // Combat
    private void handleMobTargetMessage(MobTargetMessage msg) {
        if (msg.isTargetingPlayer)
            core.combatState.tryAddCombatantById(msg.entityID, false);
        else
            core.combatState.removeCombatant(msg.entityID);
    }


    // Areas
    private void handlePutAreaMessage(PutAreaMessage msg) {
        if (msg.overwriteIfExists || core.areaManager.getAreaById(msg.area.id) == null)
            core.areaManager.putArea(msg.area);
    }

    private void handleDeleteAreaMessage(DeleteAreaMessage msg) {
        core.areaManager.deleteArea(msg.areaId);
    }


    // Structures
    private void handlePutChunkReferenceMessage(PutChunkReferenceMessage msg) {
        core.structureCache.setReferences(
                msg.dimension,
                msg.chunkPos,
                msg.startChunks
        );
    }

    private void handlePutChunkStructuresMessage(PutChunkStructuresMessage msg) {
        for (var entry : msg.chunkToStructures.entrySet())
            core.structureCache.setStructures(
                    msg.dimension,
                    entry.getKey(),
                    entry.getValue()
            );
    }


    // Name caching
    private void handlePutNameCacheMessage(PutNameCacheMessage msg) {
        core.nameCache.putPlayerName(msg.playerUuid, msg.playerName);
    }
}
