package cz.jeme.programu.latestdeath;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;

public class LatestDeath extends JavaPlugin implements Listener {

	private SqliteDB db = null;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		pm.registerEvents(this, this);
		File dataDir = getDataFolder();

		if (!dataDir.exists()) {
			dataDir.mkdir();
		}

		try {
			db = new SqliteDB(dataDir);
		} catch (ClassNotFoundException | SQLException e) {
			getLogger().severe("Couldn't establish connection with database!");
			getLogger().severe(e.getMessage());
		}
	}

	@Override
	public void onDisable() {
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("lastdeaths")) { // Command was sent
			int deathsCount = 0;
			if (sender instanceof ConsoleCommandSender) { // recognize if the sender is console
				sender.sendMessage("This command is only runnable as player");
			} else {
				if (args.length == 1) { // controls if there's only one argument in the command
					try {
						deathsCount = Integer.parseInt(args[0]); // tries to parse the argument into int
					} catch (NumberFormatException exc) { // if it falls here, it means that the argument isn't a number
						return false;
					}
					if (deathsCount > 5) { // if the arg is higher number than 5
						if (sender.isOp()) { // if sender is op, allow him everything
							// FIXME na stevovi mozna nefunguje
							sender.sendMessage("Ya know what y're doin' admin!");
							showDeaths(sender.getName(), sender.getName(), deathsCount); // player is admin, read the
																							// database
							return true;
						} else { // default players have to use number lower than 20
							sender.sendMessage("Please, use number between 1 and 5!");
						}
					} else if (deathsCount >= 1){
						showDeaths(sender.getName(), sender.getName(), deathsCount); // number is <= 5, read the
																						// database
						return true;
					} else {
						sender.sendMessage("Please, use number between 1 and 5!");
					}
				} else { // the command doesn't have 1 argument
					return false;
				}
			}
		}
		return false;
	}

	private void showDeaths(String senderName, String playerName, int deathsCount) {
		try {
			List<Death> deaths = db.readDeaths(playerName, deathsCount);
			for (Death death : deaths) {
				tellraw(senderName, "{\"text\":\"=================================\",\"color\":\"yellow\"}");
				tellraw(senderName, "[\"\",{\"text\":\"Timestamp: \",\"color\":\"#ADFFFF\"},{\"text\":\""
						+ translateDate(death.getDate()) + "\",\"color\":\"green\"}]");
				tellraw(senderName, "[\"\",{\"text\":\"Dimension: \",\"color\":\"#ADFFFF\"},{\"text\":\""
						+ translateDimension(death.getDimension()) + "\",\"color\":\"green\"}]");
				tellraw(senderName, "[\"\",{\"text\":\"Coordinates: \",\"color\":\"#ADFFFF\"},{\"text\":\""
						+ translateCoords(death.getxPos(), death.getyPos(), death.getzPos()) + "\",\"color\":\"green\"}]");
				tellraw(senderName, "[\"\",{\"text\":\"Death cause: \",\"color\":\"#ADFFFF\"},{\"text\":\""
						+ translateCause(death.getDeathCause()) + "\",\"color\":\"green\"}]");
				if (death.getShooterEntity() != null) {
					tellraw(senderName, "[\"\",{\"text\":\"Killed by: \",\"color\":\"#ADFFFF\"},{\"text\":\""
							+ translateShooter(death.getShooterEntity()) + "\",\"color\":\"green\"}]");
					tellraw(senderName, "[\"\",{\"text\":\"Projectile: \",\"color\":\"#ADFFFF\"},{\"text\":\""
							+ translateKiller(death.getKillerEntity()) + "\",\"color\":\"green\"}]");
				} else if (death.getShooterEntity() == null) {
					if (death.getKillerEntity() != null) {
						tellraw(senderName, "[\"\",{\"text\":\"Killed by: \",\"color\":\"#ADFFFF\"},{\"text\":\""
								+ translateKiller(death.getKillerEntity()) + "\",\"color\":\"green\"}]");
					}
				}
			}
			tellraw(senderName, "{\"text\":\"=================================\",\"color\":\"yellow\"}");
		} catch (SQLException | ParseException e) {
			getLogger().severe("Error while reading database!");
			getLogger().severe(e.getMessage());
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + playerName
					+ "{\"text\":\"Error while reading from database!\",\"color\":\"#FF0003\"}");
		}

	}

	private String translateDimension(String dimension) {
		if (dimension.equals(Environment.NORMAL.name())) {
			return "Overworld";
		} else if (dimension.equals(Environment.NETHER.name())) {
			return "The Nether";
		} else if (dimension.equals(Environment.THE_END.name())) {
			return "The End";
		}
		return "Unknown Dimension";
	}

	private String translateCoords(int x, int y, int z) {
		return x + " " + y + " " + z;
	}

	private String translateCause(String cause) {
		String lowerCause = cause.replace('_', ' ').toLowerCase();
		return lowerCause.substring(0, 1).toUpperCase() + lowerCause.substring(1);
	}

	private String translateKiller(String killer) {
//		String lowerKiller = killer.replace('_', ' ').toLowerCase();
//		return lowerKiller.substring(0, 1).toUpperCase() + lowerKiller.substring(1);
		return killer;
	}

	private String translateShooter(String shooter) {
//		String lowerShooter = shooter.replace('_', ' ').toLowerCase();
//		return lowerShooter.substring(0, 1).toUpperCase() + lowerShooter.substring(1);
		return shooter;
	}

	private String translateDate(Date dateAndTime) {
		return dateFormatter.format(dateAndTime);
	}

	private void tellraw(String selector, String text) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + selector + " " + text);
	}

	private String getEntityName(Entity entity) {
		String name = null;
		if (entity != null) {
			if (entity instanceof Player) {
				name = entity.getName();
			} else {
				name = entity.getType().name();
			}

		}
		return name;
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Player player = event.getEntity();
		EntityDamageEvent deathEvent = player.getLastDamageCause();
		// get death cause
		String cause = deathEvent.getCause().name();
		Entity killer = null;
		ProjectileSource projectileSource = null;
		String shooter = null;
		String killerStr = null;

		if (deathEvent instanceof EntityDamageByEntityEvent) {
			killer = ((EntityDamageByEntityEvent) deathEvent).getDamager();
			if (killer instanceof Projectile) {
				projectileSource = ((Projectile) killer).getShooter();
				if (projectileSource != null) {
					if (projectileSource instanceof BlockProjectileSource) {
						shooter = ((BlockProjectileSource) projectileSource).getBlock().getType().name();
					} else {
						shooter = getEntityName(((LivingEntity) projectileSource));
					}
				}
			}
		}

		killerStr = getEntityName(killer);

		String dimension = player.getWorld().getEnvironment().name();
		Location location = player.getLocation();
		int xPos = location.getBlockX();
		int yPos = location.getBlockY();
		int zPos = location.getBlockZ();

		Death death = new Death(player.getName(), dimension, xPos, yPos, zPos, cause, killerStr, shooter, new Date());
		try {
			db.addDeath(death);
		} catch (SQLException e) {
			getLogger().severe("Unable to write to database!");
			getLogger().severe(e.getMessage());
		}

//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Dimension: " + dimension + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"X position: " + xPos + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Y position: " + yPos + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Z position: " + zPos + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw @a \"\"");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Player: " + player.getName() + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Death cause: " + cause + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Killer: " + killerStr + "\",\"color\":\"#FA0002\"}");
//		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
//				"tellraw @a {\"text\":\"Thrower: " + shooter + "\",\"color\":\"#FA0002\"}");

	}
}