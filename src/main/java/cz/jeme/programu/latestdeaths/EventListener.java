package cz.jeme.programu.latestdeaths;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class EventListener implements Listener {

    Database database;

    public EventListener(Database database) {
        this.database = database;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        EntityDamageEvent deathEvent = player.getLastDamageCause(); // Get death event
        DamageCause cause; // Get Cause
        if (deathEvent == null) {
            throw new NullPointerException("Death event is null!");
        }
        cause = deathEvent.getCause();

        EntityType killerType = null;
        UUID killerPlayerUuid = null;
        Material blockSource = null;
        EntityType entitySource = null;
        UUID playerSourceUuid = null;

        if (deathEvent instanceof EntityDamageByEntityEvent) { // Player was damaged by an entity
            Entity killer = ((EntityDamageByEntityEvent) deathEvent).getDamager();
            killerType = killer.getType();

            if (killer instanceof Player) { // Player was killed by another player
                killerPlayerUuid = killer.getUniqueId();
            } else if (killer instanceof Projectile) { // Player was killed by a projectile
                ProjectileSource shooter = ((Projectile) killer).getShooter();
                if (shooter instanceof LivingEntity) { // Player was shot by an entity
                    entitySource = ((LivingEntity) shooter).getType();
                    if (shooter instanceof Player) { // Player was shot by another player
                        playerSourceUuid = ((Player) shooter).getUniqueId();
                    }
                } else if (shooter instanceof BlockProjectileSource) { // Player was shot by a block (dispenser)
                    blockSource = ((BlockProjectileSource) shooter).getBlock().getType();
                }
            } else if (killer instanceof TNTPrimed) { // Player was killed by a tnt
                Entity source = ((TNTPrimed) killer).getSource();
                if (source != null) { // The tnt was lit by an entity
                    entitySource = source.getType();
                    if (source instanceof Player) { // The tnt was lit by a player
                        playerSourceUuid = source.getUniqueId();
                    }
                }
            }
        }

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) {
            throw new NullPointerException("World is null!");
        }
        World.Environment dimension = world.getEnvironment();

        Death death = new Death(
                player.getUniqueId(),
                dimension,
                location.getX(),
                location.getY(),
                location.getZ(),
                cause,
                killerType,
                killerPlayerUuid,
                blockSource,
                entitySource,
                playerSourceUuid,
                new Date()
        );
        try {
            database.addDeath(death);
        } catch (SQLException e) {
            LatestDeaths.serverLog(Level.SEVERE, "Unable to write to database!");
            e.printStackTrace();
        }
    }
}
