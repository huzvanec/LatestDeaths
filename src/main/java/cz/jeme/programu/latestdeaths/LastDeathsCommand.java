package cz.jeme.programu.latestdeaths;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class LastDeathsCommand extends Command {
    private final Database database;

    protected LastDeathsCommand(Database database) {
        super("lastdeaths", "Shows player's last deaths", "false", List.of("ld"));
        register();
        setPermission("latestdeaths.lastdeaths");

        this.database = database;
    }

    private void register() {
        Bukkit.getCommandMap().register("latestdeaths", this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        int deathsCount = 1;
        if (args.length > 0) {
            if (args.length > 2 || (args.length > 1 && !sender.hasPermission("latestdeaths.seeothers"))) {
                sender.sendMessage(Messages.prefix("<red>Too many arguments!</red>"));
                return true;
            }
            try {
                deathsCount = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Messages.prefix("<red>That is not a valid number!</red>"));
                return true;
            }
        }

        int limit = LatestDeaths.config.getInt("queue-limit");
        if (deathsCount > limit && !sender.hasPermission("latestdeaths.overlimit")) {
            sender.sendMessage(Messages.prefix("<red>Too many deaths to display! The limit is " + limit + "!</red>"));
            return true;
        }
        if (args.length == 2) {
            Map<String, UUID> offlinePlayers = DeathUtils.getOfflinePlayersStringUuidMap();
            if (!offlinePlayers.containsKey(args[1])) {
                sender.sendMessage(Messages.prefix("<red>Player not found!</red>"));
                return true;
            }
            List<Death> deaths = readDeaths(sender, offlinePlayers.get(args[1]), deathsCount);
            assert deaths != null;
            Collections.reverse(deaths);
            showDeaths(sender, deaths, true);
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Messages.prefix("<red>Not enough arguments!</red>"));
                return true;
            }
            List<Death> deaths = readDeaths(sender, player.getUniqueId(), deathsCount);
            assert deaths != null;
            Collections.reverse(deaths);
            showDeaths(sender, deaths, false);
        }
        return true;
    }

    private List<Death> readDeaths(CommandSender sender, UUID uuid, int count) {
        try {
            return database.readDeaths(uuid, count);
        } catch (SQLException | ParseException e) {
            sender.sendMessage(Messages.prefix("<red>An error occured while reading from the database!</red>"));
            LatestDeaths.serverLog(Level.SEVERE, "Unable to read from database!");
            e.printStackTrace();
        }
        return null;
    }

    private static void showDeaths(CommandSender sender, List<Death> deaths, boolean otherPlayer) {
        if (deaths == null) {
            return;
        }
        if (deaths.size() == 0) {
            if (otherPlayer) {
                sender.sendMessage(Messages.prefix("<red>This player has no deaths!</red>"));
            } else {
                sender.sendMessage(Messages.prefix("<red>You have no deaths!</red>"));
            }
            return;
        }
        String separator = LatestDeaths.config.getString("death-separator");
        sender.sendMessage(Messages.from(separator));
        for (Death death : deaths) {
            DeathUtils.translateDeath(death, otherPlayer).forEach(sender::sendMessage);
            sender.sendMessage(Messages.from(separator));
        }
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 2 && sender.hasPermission("latestdeaths.seeothers")) {
            return containsFilter(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> containsFilter(List<String> list, String mark) {
        return list.stream()
                .filter(item -> item.contains(mark))
                .toList();
    }
}
