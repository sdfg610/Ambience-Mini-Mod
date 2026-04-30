package me.molybdenum.ambience_mini.engine.client.core.locations;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.DeleteAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.PutAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.CreateAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.McVersion;

import java.util.function.Supplier;

public class AreaHelper {
    private final ClientAreaManager areaManager;
    private final BaseClientNetworkManager networkManager;
    private final ServerSetup serverSetup;
    private final BaseNotification<?> notification;

    public final McVersion mcVersion;
    private final Supplier<String> getPlayerUUID;
    private final Supplier<Cube> getSelectedCube;


    @SuppressWarnings("rawtypes")
    public AreaHelper(BaseClientCore core, Supplier<Cube> getSelectedCube) {
        this.areaManager = core.areaManager;
        this.networkManager = core.networkManager;
        this.serverSetup = core.serverSetup;
        this.notification = core.notification;

        this.mcVersion = core.mcVersion;
        this.getPlayerUUID = core.nameCache::getCurrentPlayerUUID;
        this.getSelectedCube = getSelectedCube;
    }


    public String getPlayerUUID() {
        return getPlayerUUID.get();
    }

    public Cube getSelectedCube() {
        return getSelectedCube.get();
    }


    @SuppressWarnings("ConstantValue")
    public void submitArea(Area area, Runnable onSuccess, Runnable onFailure) {
        boolean isLocalOwner = area.owner.isLocal();

        boolean hasServerSupport = hasServerSupport();
        if (!isLocalOwner && !hasServerSupport){
            notification.printLiteralToChat("Cannot save non-local area without server support! It should not be possible to get here!");
            onFailure.run();
        }

        if (area.isNew()) {
            if (isLocalOwner)
                createLocalArea(area, onSuccess, onFailure);
            else
                createRemoteArea(area, onSuccess, onFailure);
        } else {
            boolean isLocalId = area.isLocalId();

            if (isLocalOwner && isLocalId)
                putLocalArea(area, onSuccess, onFailure);
            else if (isLocalOwner && !isLocalId)
                moveToLocalStorage(area, onSuccess, onFailure);
            else if (!isLocalOwner && isLocalId)
                moveToRemoteStorage(area, onSuccess, onFailure);
            else if (!isLocalOwner && !isLocalId)
                putRemoteArea(area, onSuccess, onFailure);
        }
    }


    private void createLocalArea(Area area, Runnable onSuccess, Runnable onFailure) {
        areaManager.createLocalArea(area).ifPresentOrElse(
                error -> {
                    notification.printLiteralToChat("Could not create local area! It should not be possible to get here!\nArea: " + area.toJson() + "\nError: " + error);
                    onFailure.run();
                },
                onSuccess
        );
    }

    private void putLocalArea(Area area, Runnable onSuccess, Runnable onFailure) {
        areaManager.putArea(area).ifPresentOrElse(
                error -> {
                    notification.printLiteralToChat("Could not update local area! It should not be possible to get here!\nArea: " + area.toJson() + "\nError: " + error);
                    onFailure.run();
                },
                onSuccess
        );
    }


    private void createRemoteArea(Area area, Runnable onSuccess, Runnable onFailure) {
        networkManager.sendToServer(new CreateAreaMessage(area), onSuccess, onFailure);
    }

    private void putRemoteArea(Area area, Runnable onSuccess, Runnable onFailure) {
        networkManager.sendToServer(new PutAreaMessage(area), onSuccess, onFailure);
    }


    private void moveToLocalStorage(Area area, Runnable onSuccess, Runnable onFailure) {
        int oldId = area.id;
        createLocalArea(
                area,
                () -> { // Success
                    networkManager.sendToServer(
                            new DeleteAreaMessage(oldId),
                            onSuccess,
                            () -> { // If we cannot delete the remote area, remove the newly added area again.
                                notification.printLiteralToChat("Move to local storage failed at deleting the remote area...");
                                areaManager.deleteArea(area.id);
                                onFailure.run();
                            }
                    );
                },
                () -> {
                    notification.printLiteralToChat("Move to local storage failed at creating the local area...");
                    area.id = oldId;
                    onFailure.run();
                }
        );
    }

    private void moveToRemoteStorage(Area area, Runnable onSuccess, Runnable onFailure) {
        int oldId = area.id;
        createRemoteArea(
                area,
                () -> {
                    areaManager.deleteArea(oldId);
                    onSuccess.run();
                },
                onFailure
        );
    }


    public void deleteArea(int id, Runnable onSuccess, Runnable onFailure) {
        if (Area.isLocalId(id)) {
            areaManager.deleteArea(id);
            onSuccess.run();
        }
        else if (hasServerSupport())
            networkManager.sendToServer(new DeleteAreaMessage(id), onSuccess, onFailure);
    }


    public boolean hasServerSupport() {
        return serverSetup.serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0);
    }
}
