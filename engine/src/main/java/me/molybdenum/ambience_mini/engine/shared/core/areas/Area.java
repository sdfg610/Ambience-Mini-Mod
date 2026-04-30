package me.molybdenum.ambience_mini.engine.shared.core.areas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3d;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;

import java.util.Optional;

public class Area implements AmSerializable {
    public static final int NEW_ID = Integer.MIN_VALUE;

    public int id;
    public String name;
    public String dimension;
    public Owner owner;

    public Vector3i fromBlock;
    public Vector3i toBlock;


    public Area(AmReader reader) {
        this.id = reader.readInt();
        this.name = reader.readString();
        this.dimension = reader.readString();
        this.owner = new Owner(reader);
        this.fromBlock = new Vector3i(reader);
        this.toBlock = new Vector3i(reader);
    }

    public Area() {
        this(NEW_ID, null, null, null, null, null);
    }

    public Area(String dimension, Owner owner, Vector3i fromBlock, Vector3i toBlock) {
        this(NEW_ID, "New Area", dimension, owner, fromBlock, toBlock);
    }

    public Area(int id, String name, String dimension, Owner owner, Vector3i fromBlock, Vector3i toBlock) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.owner = owner;

        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }


    public Optional<String> validate() {

        if (name == null)
            return Optional.of("Area name cannot be null!");
        else if (name.isBlank())
            return Optional.of("Area name cannot be blank!");
        else if (name.length() > Common.MAX_AREA_NAME_LENGTH)
            return Optional.of("Area name cannot be longer than '" + Common.MAX_AREA_NAME_LENGTH + "' symbols. Got: '" + name + "'");

        else if (dimension == null)
            return Optional.of("Area dimension cannot be null!");
        else if (dimension.isBlank())
            return Optional.of("Area dimension cannot be blank!");

        else if (owner == null)
            return Optional.of("Area owner cannot be null!");

        else if (id == NEW_ID)
            return Optional.of("Area id cannot be '" + NEW_ID + "', the 'new-id'!");
        else if (isLocalId() && !owner.isLocal())
            return Optional.of("Area id '" + NEW_ID + "' is local, but ownership is non-local!");
        else if (isNonLocalId() && owner.isLocal())
            return Optional.of("Area id '" + NEW_ID + "' is non-local, but ownership is local!");

        return Optional.empty();
    }


    public boolean isNew() {
        return id == NEW_ID;
    }

    public boolean isLocalId() {
        return isLocalId(id);
    }

    public static boolean isLocalId(int id) {
        return id < 0 && id != NEW_ID;
    }

    public boolean isNonLocalId() {
        return isNonLocalId(id);
    }

    public static boolean isNonLocalId(int id) {
        return id >= 0;
    }


    public boolean canBeEditedBy(String playerUUID) {
        String ownerUUID = owner.getOwnerIdIfOwned();
        return ownerUUID == null || playerUUID.equals(ownerUUID);
    }

    public boolean canBeSeenBy(String playerUUID) {
        String ownerUUID = owner.getOwnerIdIfOwned();
        return ownerUUID == null || owner.isShared() || playerUUID.equals(ownerUUID);
    }

    public boolean contains(Vector3d position) {
        return Vector3i.minAndMaxOf(fromBlock, toBlock.offset(1,1,1)).destruct((min, max) ->
            position.x() >= min.x() && position.x() <= max.x()
                && position.y() >= min.y() && position.y() <= max.y()
                && position.z() >= min.z() && position.z() <= max.z()
        );
    }


    public Area copy() {
        return new Area(
                this.id,
                this.name,
                this.dimension,
                this.owner.copy(),
                this.fromBlock,
                this.toBlock
        );
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(this.id);
        writer.writeString(this.name);
        writer.writeString(this.dimension);
        owner.writeTo(writer);
        fromBlock.writeTo(writer);
        toBlock.writeTo(writer);
    }


    public static boolean validateJson(JsonElement elem) {
        if (!Utils.isJsonObjectWith(elem, "id", "name", "dimension", "owner", "from", "to"))
            return false;

        JsonObject obj = elem.getAsJsonObject();
        return Utils.isJsonNumber(obj.get("id"))
                && Utils.isJsonString(obj.get("name"))
                && Utils.isJsonString(obj.get("dimension"))
                && Owner.validateJson(obj.get("owner"))
                && Vector3i.validateJson(obj.get("from"))
                && Vector3i.validateJson(obj.get("to"));
    }

    public static Area fromJson(JsonObject obj) {
        return new Area(
                obj.get("id").getAsInt(),
                obj.get("name").getAsString(),
                obj.get("dimension").getAsString(),
                Owner.fromJson(obj.getAsJsonObject("owner")),
                Vector3i.fromJson(obj.getAsJsonObject("from")),
                Vector3i.fromJson(obj.getAsJsonObject("to"))
        );
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("id", new JsonPrimitive(id));
        obj.add("name", new JsonPrimitive(name));
        obj.add("dimension", new JsonPrimitive(dimension));
        obj.add("owner", owner.toJson());
        obj.add("from", fromBlock.toJson());
        obj.add("to", toBlock.toJson());
        return obj;
    }

    public int volume() {
        return Vector3i.volume(fromBlock, toBlock);
    }
}
