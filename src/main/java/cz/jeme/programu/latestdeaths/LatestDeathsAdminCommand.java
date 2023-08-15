package cz.jeme.programu.latestdeaths;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class LatestDeathsAdminCommand extends Command {
    private final LatestDeaths plugin;

    protected LatestDeathsAdminCommand(LatestDeaths plugin) {
        super("latestdeathsadmin", "Admin command for latest deaths", "false", List.of("ldadmin"));
        setPermission("latestdeaths.admin");
        Bukkit.getCommandMap().register("latestdeaths", this);

        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Messages.prefix("<red>Not enough arguments!</red>"));
            return true;
        }
        if (args[0].equals("reload")) {
            plugin.reload();
            sender.sendMessage(Messages.prefix("<green>Plugin reloaded successfully!</green>"));
            return true;
        }
        sender.sendMessage(Messages.prefix("<red>Unknown command!</red>"));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return LastDeathsCommand.containsFilter(List.of("reload"), args[0]);
        }
        return Collections.emptyList();
    }
}