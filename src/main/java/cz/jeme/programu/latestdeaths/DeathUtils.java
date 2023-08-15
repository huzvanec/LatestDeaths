package cz.jeme.programu.latestdeaths;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

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
        String pattern = LatestDeaths.config.getString("print.date-pattern");
        if (pattern == null) throw new NullPointerException("Print date pattern is null!");
        dateFormatter = new SimpleDateFormat(pattern);
    }

    public static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#0.00");

    public static List<Component> translateDeath(Death death, boolean otherPlayer) {
        List<String> deathList = new ArrayList<>();
        if (otherPlayer) {
            String playerName = playerNameFromUuid(death.uuid());
            deathList.add("Player: " + playerName);
        }
        deathList.add("Dimension: " + translateDimension(death.dimension()));
        deathList.add("Coordinates: " + translateCoordinates(death.x(), death.y(), death.z()));
        deathList.add("Cause: " + translateCause(death.cause()));
        if (death.blockSource() != null || death.entitySource() != null) {
            String shooter;
            if (death.playerSourceUuid() != null) {
                shooter = playerNameFromUuid(death.playerSourceUuid());
            } else if (death.blockSource() != null) {
                shooter = upperString(death.blockSource().toString());
            } else {
                shooter = upperString(death.entitySource().toString());
            }
            deathList.add("Shot by: " + shooter);
            if (death.killer() != null) {
                deathList.add("Using: " + upperString(death.killer().toString()));
            }
        } else if (death.killerPlayerUuid() != null) {
            deathList.add("Killed by: " + playerNameFromUuid(death.killerPlayerUuid()));
        } else if (death.killer() != null) {
            deathList.add("Killed by: " + translateKiller(death.killer()));
        }
        deathList.add("Date: " + dateFormatter.format(death.date()));

        colorizeDeathTranslation(deathList);

        return deathList.stream()
                .map(Messages::from)
                .collect(Collectors.toList());
    }

    private static void colorizeDeathTranslation(List<String> deathList) {
        String keyColor = LatestDeaths.config.getString("print.key-color");
        String valueColor = LatestDeaths.config.getString("print.value-color");
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
        return (uuid == null) ? null : Bukkit.getOfflinePlayer(uuid).getName();
    }

    public static String translateDimension(World.Environment dimension) {
        return switch (dimension) {
            case NORMAL -> "Overworld";
            case NETHER -> "The Nether";
            case THE_END -> "The End";
            default -> "Unknown";
        };
    }

    public static String translateCoordinates(double x, double y, double z) {
        String sepa = LatestDeaths.config.getString("print.coordinates-separator");
        return DECIMAL_FORMATTER.format(x) + sepa + DECIMAL_FORMATTER.format(y) + sepa + DECIMAL_FORMATTER.format(z);
    }

    public static String translateCause(EntityDamageEvent.DamageCause cause) {
        return upperString(cause.toString());
    }

    public static String translateKiller(EntityType killer) {
        return (killer == null) ? null : upperString(killer.toString());
    }

    public static String upperString(@NotNull String input) {
        // I can't use org.apache.commons.lang3.text.WordUtils, because it is deprecated and
        // org.apache.commons.text is not bundled with paper
        String[] words = input.split("_");
        StringBuilder stringBuilder = new StringBuilder();

        for (String word : words) {
            stringBuilder.append(Character.toUpperCase(word.charAt(0)));
            stringBuilder.append(word.substring(1).toLowerCase());
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().trim();
    }


    public static Map<String, UUID> getOfflinePlayersNameUuid() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> player.getName() != null)
                .collect(Collectors.toMap(
                        OfflinePlayer::getName,
                        OfflinePlayer::getUniqueId
                ));
    }

    public static Map<UUID, String> getOfflinePlayersUuidName() {
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(player -> player.getName() != null)
                .collect(Collectors.toMap(
                        OfflinePlayer::getUniqueId,
                        OfflinePlayer::getName
                ));
    }
}