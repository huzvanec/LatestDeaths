package cz.jeme.programu.latestdeaths;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class Death {
    @NotNull
    private final UUID uuid;
    @NotNull
    private final World.Environment dimension;
    private final double x;
    private final double y;
    private final double z;
    @NotNull
    private final DamageCause cause;
    @Nullable
    private final EntityType killer;
    @Nullable
    private final UUID killerPlayerUuid;
    @Nullable
    private final Material blockSource;
    @Nullable
    private final EntityType entitySource;
    @Nullable
    private final UUID playerSourceUuid;
    @NotNull
    private final Date date;

    @Override
    public String toString() {
        return "Death{" +
                "uuid=" + uuid +
                ", dimension=" + dimension +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", cause=" + cause +
                ", killer=" + killer +
                ", killerPlayerUuid=" + killerPlayerUuid +
                ", blockSource=" + blockSource +
                ", entitySource=" + entitySource +
                ", playerSourceUuid=" + playerSourceUuid +
                ", date=" + date +
                '}';
    }

    public Death(@NotNull UUID uuid,
                 World.@NotNull Environment dimension,
                 double x, double y, double z,
                 @NotNull DamageCause cause,
                 @Nullable EntityType killer,
                 @Nullable UUID killerPlayerUuid,
                 @Nullable Material blockSource,
                 @Nullable EntityType entitySource,
                 @Nullable UUID playerSourceUuid,
                 @NotNull Date date) {
        this.uuid = uuid;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.cause = cause;
        this.killer = killer;
        this.killerPlayerUuid = killerPlayerUuid;
        this.blockSource = blockSource;
        this.entitySource = entitySource;
        this.playerSourceUuid = playerSourceUuid;
        this.date = date;
    }


    public @NotNull UUID getUuid() {
        return uuid;
    }

    public World.@NotNull Environment getDimension() {
        return dimension;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public @Nullable UUID getKillerPlayerUuid() {
        return killerPlayerUuid;
    }

    public @NotNull DamageCause getCause() {
        return cause;
    }

    public @Nullable EntityType getKiller() {
        return killer;
    }

    public @NotNull Date getDate() {
        return date;
    }

    public @Nullable Material getBlockSource() {
        return blockSource;
    }

    public @Nullable EntityType getEntitySource() {
        return entitySource;
    }

    public @Nullable UUID getPlayerSourceUuid() {
        return playerSourceUuid;
    }
}
