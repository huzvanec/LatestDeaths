package cz.jeme.programu.latestdeaths;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public final class DeathUtils {

    public static SimpleDateFormat dateFormatter = null;

    private DeathUtils() {
        // Static utility
    }

    public static void loadDatePattern() {
        String pattern = LatestDeaths.config.getString("date-pattern");
        assert pattern != null;
        dateFormatter = new SimpleDateFormat(pattern);
    }

    public static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#0.00");

    public static List<Component> translateDeath(Death death, boolean otherPlayer) {
        List<String> deathList = new ArrayList<>();
        if (otherPlayer) {
            String playerName = playerNameFromUuid(death.getUuid());
            deathList.add("Player: " + playerName);
        }
        deathList.add("Dimension: " + translateDimension(death));
        deathList.add("Coordinates: " + translateCoordinates(death));
        deathList.add("Cause: " + translateCause(death));
        if (death.getBlockSource() != null || death.getEntitySource() != null) {
            String shooter = null;
            if (death.getPlayerSourceUuid() != null) {
                shooter = playerNameFromUuid(death.getPlayerSourceUuid());
            } else if (death.getBlockSource() != null) {
                shooter = translateString(death.getBlockSource().toString());
            } else if (death.getEntitySource() != null) {
                shooter = translateString(death.getEntitySource().toString());
            }
            deathList.add("Shot by: " + shooter);
            if (death.getKiller() != null) {
                deathList.add("Using: " + translateString(death.getKiller().toString()));
            }
        } else if (death.getKillerPlayerUuid() != null) {
            deathList.add("Killed by: " + playerNameFromUuid(death.getKillerPlayerUuid()));
        } else if (death.getKiller() != null) {
            deathList.add("Killed by: " + translateKiller(death));
        }
        deathList.add("Date: " + dateFormatter.format(death.getDate()));

        colorizeDeathTranslation(deathList);

        return deathList.stream()
                .map(Messages::from)
                .collect(Collectors.toList());
    }

    private static void colorizeDeathTranslation(List<String> deathList) {
        String keyColor = LatestDeaths.config.getString("key-color");
        String valueColor = LatestDeaths.config.getString("value-color");
        assert keyColor != null;
        assert valueColor != null;
        int i = 0;
        for (String item : deathList) {
            String[] split = item.split(": ");
            deathList.set(i,
                    keyColor + split[0] + ": " + Messages.getEscapeTag(keyColor)
                    + valueColor + split[1] + Messages.getEscapeTag(valueColor));
            i++;
        }
    }

    public static String playerNameFromUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static String translateDimension(Death death) {
        return translateDimension(death.getDimension());
    }

    public static String translateDimension(World.Environment dimension) {
        if (dimension == World.Environment.NORMAL) {
            return "Overworld";
        } else if (dimension == World.Environment.NETHER) {
            return "The Nether";
        } else if (dimension == World.Environment.THE_END) {
            return "The End";
        }
        return "Custom";
    }

    public static String translateCoordinates(Death death) {
        return translateCoordinates(death.getX(), death.getY(), death.getZ());
    }

    public static String translateCoordinates(double x, double y, double z) {
        String sepa = " / ";
        return DECIMAL_FORMATTER.format(x) + sepa + DECIMAL_FORMATTER.format(y) + sepa + DECIMAL_FORMATTER.format(z);
    }

    public static String translateCause(Death death) {
        return translateCause(death.getCause());
    }

    public static String translateCause(EntityDamageEvent.DamageCause cause) {
        return translateString(cause.toString());
    }

    public static String translateKiller(Death death) {
        EntityType killer = death.getKiller();
        if (killer == null) {
            return null;
        }
        return translateKiller(killer);
    }

    public static String translateKiller(EntityType killer) {
        return translateString(killer.toString());
    }

    public String translateShooter(String shooter) {
// This doesn't work, because it lowers also playernames, so i had to turn it off
//		String lowerShooter = shooter.replace('_', ' ').toLowerCase();
//		return lowerShooter.substring(0, 1).toUpperCase() + lowerShooter.substring(1);
        return shooter;
    }

//    private String translateDate(Date dateAndTime) {
//        return dateFormatter.format(dateAndTime);
//    }

    public static String translateString(String input) {
        String[] words = input.split("_");
        StringBuilder stringBuilder = new StringBuilder();

        for (String word : words) {
            stringBuilder.append(Character.toUpperCase(word.charAt(0)));
            stringBuilder.append(word.substring(1).toLowerCase());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public static Map<String, UUID> getOfflinePlayersStringUuidMap() {
        Map<String, UUID> offlinePlayers = new HashMap<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            offlinePlayers.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
        }
        return offlinePlayers;
    }

    public static Map<UUID, String> getOfflinePlayersUuidStringMap() {
        Map<UUID, String> offlinePlayers = new HashMap<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            offlinePlayers.put(offlinePlayer.getUniqueId(), offlinePlayer.getName());
        }
        return offlinePlayers;
    }

}
