package me.molybdenum.ambience_mini.engine.client.core.networking;

import me.molybdenum.ambience_mini.engine.BaseAmbienceMini;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.handlers.AsyncHandler;
import me.molybdenum.ambience_mini.engine.client.core.networking.handlers.Handler;
import me.molybdenum.ambience_mini.engine.client.core.networking.handlers.SyncHandler;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.DeleteAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.PutAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.Response;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.ResponseMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobCombatInteractionMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.PutFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.SuccessMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.PutNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.PutChunkReferencesMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.PutChunkStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.DeleteFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public abstract class BaseClientNetworkManager
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;

    private static final AtomicInteger UNIQUE_ID_GEN = new AtomicInteger();
    private final ConcurrentHashMap<Integer, Pair<Handler, TimeoutJob>> handlers = new ConcurrentHashMap<>();

    private final JobCenter jobCenter = JobCenter.singleThreaded();


    @SuppressWarnings("rawtypes")
    public void init(BaseClientCore core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseClientNetworkManager.init'!");
        this.core = core;
    }


    public AsyncBuilder configureAsync() {
        return new AsyncBuilder();
    }

    public void sendAsync(AmMessage message) {
        new AsyncBuilder().send(message);
    }

    public Response sendSync(AmMessage message, long timeoutMillis) {
        int id = UNIQUE_ID_GEN.getAndIncrement();
        var handler = new SyncHandler();

        handlers.put(id, new Pair<>(handler, null));
        message.handlerID = id;
        sendToServerInternal(message);

        return handler.await(timeoutMillis);
    }

    protected abstract void sendToServerInternal(AmMessage message);


    public void handleMessage(StrResult<AmMessage> msgRes) {
        if (!msgRes.isSuccess()) {
            core.notification.printTranslatableToChat(AmLang.MSG_UNHANDLED_MESSAGE);
            core.logger.error(msgRes.error);
            return;
        }

        AmMessage message = msgRes.value;
        if (message instanceof FailureMessage msg)
            handleFailureMessage(msg);
        else if (message instanceof SuccessMessage msg)
            handleSuccessMessage(msg);
        else if (message instanceof ResponseMessage msg)
            handleResponseMessage(msg);

        else if (message instanceof MobTargetMessage msg)
            handleMobTargetMessage(msg);
        else if (message instanceof MobCombatInteractionMessage msg)
            handleMobCombatInteractionMessage(msg);

        else if (message instanceof PutAreaMessage msg)
            handlePutAreaMessage(msg);
        else if (message instanceof DeleteAreaMessage msg)
            handleDeleteAreaMessage(msg);

        else if (message instanceof PutChunkReferencesMessage msg)
            handlePutChunkReferenceMessage(msg);
        else if (message instanceof PutChunkStructuresMessage msg)
            handlePutChunkStructuresMessage(msg);

        else if (message instanceof PutNameCacheMessage msg)
            handlePutNameCacheMessage(msg);

        else if (message instanceof PutFlagMessage msg)
            handleUpdateFlagMessage(msg);
        else if (message instanceof DeleteFlagMessage msg)
            handleDeleteFlagMessage(msg);

        else {
            core.notification.printTranslatableToChat(AmLang.MSG_UNHANDLED_MESSAGE);
            core.logger.error("Client network handler could not handle message of type: {}", message.getClass().getName());
        }
    }


    // Success and failure responses
    private void handleFailureMessage(FailureMessage msg) {
        handle(msg.handlerID, new Response(msg.message));
    }

    private void handleSuccessMessage(SuccessMessage msg) {
        handle(msg.handlerID, new Response((byte[])null));
    }

    private void handleResponseMessage(ResponseMessage msg) {
        handle(msg.handlerID, msg.response);
    }

    private void handle(int handlerID, Response response) {
        var handler = handlers.remove(handlerID);
        if (handler != null) {
            if (handler.right() != null)
                handler.right().cancel(true);
            handler.left().handle(response);
        }
    }


    // Combat
    private void handleMobTargetMessage(MobTargetMessage msg) {
        BaseAmbienceMini.executeAsync(
                () -> core.combatState.handleTargeting(msg.entityID, msg.isTargetingPlayer)
        );
    }

    private void handleMobCombatInteractionMessage(MobCombatInteractionMessage msg) {
        BaseAmbienceMini.executeAsync(
                () -> core.combatState.handleInteraction(msg.entityID)
        );
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
    private void handlePutChunkReferenceMessage(PutChunkReferencesMessage msg) {
        core.structureCache.setReferences(
                msg.dimension,
                msg.chunkToReferences
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


    // Flags
    private void handleUpdateFlagMessage(PutFlagMessage msg) {
        core.flagCache.putFlag(msg.id, msg.value);
    }

    private void handleDeleteFlagMessage(DeleteFlagMessage msg) {
        core.flagCache.deleteFlag(msg.id);
    }



    public class AsyncBuilder {
        @Nullable
        private Consumer<byte[]> onSuccess;
        @Nullable
        private Consumer<Text> onFailure;
        @Nullable
        private Runnable onTimeout;

        private long timeoutMillis = 1000;

        private boolean hasHandlers = false;


        public AsyncBuilder onSuccess(Consumer<byte[]> onSuccess) {
            this.onSuccess = onSuccess;
            hasHandlers = true;
            return this;
        }

        public AsyncBuilder onSuccess(Runnable onSuccess) {
            return onSuccess((ignored) -> onSuccess.run());
        }

        public AsyncBuilder onFailure(Consumer<Text> onFailure) {
            this.onFailure = onFailure;
            hasHandlers = true;
            return this;
        }

        public AsyncBuilder onTimeout(Runnable onTimeout) {
            this.onTimeout = onTimeout;
            hasHandlers = true;
            return this;
        }

        public AsyncBuilder setTimeout(long timeoutMillis) {
            if (timeoutMillis <= 0)
                throw new RuntimeException("Cannot have negative timeout. Got '" + timeoutMillis + "'");
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        public void send(AmMessage message) {
            if (hasHandlers) {
                int id = UNIQUE_ID_GEN.getAndIncrement();
                message.handlerID = id;

                handlers.put(id, new Pair<>(
                        new AsyncHandler(onSuccess, onFailure, onTimeout),
                        jobCenter.post(new TimeoutJob(id), timeoutMillis)
                ));
            }

            sendToServerInternal(message);
        }
    }

    private class TimeoutJob extends JobCenter.Job {
        private final int handlerId;


        public TimeoutJob(int handlerId) {
            this.handlerId = handlerId;
        }


        @Override
        protected void body() {
            handle(handlerId, null);
        }
    }
}
