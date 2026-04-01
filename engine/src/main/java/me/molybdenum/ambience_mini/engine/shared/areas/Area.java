package me.molybdenum.ambience_mini.engine.shared.areas;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class Area implements AmSerializable {
    public int id;
    public String name;
    public String dimension;
    public Owner owner;

    public Vector3i fromBlock;
    public Vector3i toBlock;


    public Area() {
        this(Integer.MIN_VALUE, null, null, null, null, null);
    }

    public Area(String dimension, Owner owner, Vector3i fromBlock, Vector3i toBlock) {
        this(Integer.MIN_VALUE, "New Area", dimension, owner, fromBlock, toBlock);
    }

    public Area(int id, String name, String dimension, Owner owner, Vector3i fromBlock, Vector3i toBlock) {
        this.id = id;
        this.name = name;
        this.dimension = dimension;
        this.owner = owner;

        this.fromBlock = fromBlock;
        this.toBlock = toBlock;
    }


    public boolean isNew() {
        return id == Integer.MIN_VALUE;
    }

    public boolean canBeEditedBy(String playerUUID) {
        String ownerUUID = owner.getOwnerIdIfOwned();
        return ownerUUID == null || playerUUID.equals(ownerUUID);
    }

    public boolean canBeSeenBy(String playerUUID) {
        String ownerUUID = owner.getOwnerIdIfOwned();
        return ownerUUID == null || owner.isShared() || playerUUID.equals(ownerUUID);
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


    @Override
    public void readFrom(AmReader reader) {
        this.id = reader.readInt();
        this.name = reader.readString();
        this.dimension = reader.readString();
        this.owner = new Owner(reader);
        this.fromBlock = new Vector3i(reader);
        this.toBlock = new Vector3i(reader);
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
}
