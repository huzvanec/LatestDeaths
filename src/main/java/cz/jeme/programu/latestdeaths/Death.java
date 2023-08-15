package cz.jeme.programu.latestdeaths;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public record Death(@NotNull UUID uuid, @NotNull World.Environment dimension, double x, double y, double z,
                    @NotNull DamageCause cause, @Nullable EntityType killer, @Nullable UUID killerPlayerUuid,
                    @Nullable Material blockSource, @Nullable EntityType entitySource, @Nullable UUID playerSourceUuid,
                    @NotNull Date date) {

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
}