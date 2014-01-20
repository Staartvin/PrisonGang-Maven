package me.staartvin.prisongang;

import me.staartvin.prisongang.chat.ChatListener;
import me.staartvin.prisongang.commands.Commands;
import me.staartvin.prisongang.config.ConfigWrapper;
import me.staartvin.prisongang.config.MainConfig;
import me.staartvin.prisongang.gang.GangHandler;
import me.staartvin.prisongang.listeners.PlayerJoinListener;
import me.staartvin.prisongang.permissions.PermissionsManager;
import me.staartvin.prisongang.playerdata.PlayerDataHandler;
import me.staartvin.prisongang.playerdata.RefreshPlayerDataTask;
import me.staartvin.prisongang.save.SaveGangDataTask;
import me.staartvin.prisongang.save.SaveManager;
import me.staartvin.prisongang.save.SavePlayerDataTask;
import me.staartvin.prisongang.translation.Lang;
import me.staartvin.prisongang.vault.VaultHandler;
import me.staartvin.prisongang.voting.VotingCheckerTask;

import org.bukkit.plugin.java.JavaPlugin;

public class PrisonGang extends JavaPlugin {

	private ConfigWrapper gangDataFile = new ConfigWrapper(this, "/data", "gangs.yml");
	private ConfigWrapper playerDataFile = new ConfigWrapper(this, "/data", "playerdata.yml");
	private ConfigWrapper messagesFile = new ConfigWrapper(this, "", "messages.yml");
	private ConfigWrapper mainConfigFile = new ConfigWrapper(this, "", "config.yml");

	private PlayerDataHandler playerDataHandler = new PlayerDataHandler(this);
	private GangHandler gangHandler = new GangHandler(this);
	private SaveManager saveManager = new SaveManager(this);
	private Commands commands = new Commands(this);
	private PermissionsManager permManager = new PermissionsManager(this);
	private MainConfig mainConfigHandler = new MainConfig(this);
	
	public String gang_chat_prefix = "{PRISONGANG_GANG}";
	public String title_chat_prefix = "{PRISONGANG_TITLE}";
	public String chatmode_prefix = "{PRISONGANG_CHATMODE}";

	private VaultHandler vaultHandler;

	// TODO implement messages.yml
	public void onEnable() {
		gangDataFile
				.createNewFile(
						"Gangs data file has been loaded!",
						"This file stores all information about gangs. "
								+ "You do not have to edit this manually. \nAll the settings can be changed via commands in-game."
								+ "\nEditing any data in here manually will void warranty of a working plugin.");

		playerDataFile
				.createNewFile(
						"Playerdata file has been loaded!",
						"This file stores all information about the players. "
								+ "You do not have to edit this manually. \nThese settings will be changed by the plugin automatically."
								+ "\nEditing any data in here manually will void warranty of a working plugin.");

		messagesFile.createNewFile("Messages have been loaded!",
				"You can change all messages here to your liking");
		
		mainConfigFile.createNewFile("Main config file has been loaded!", "All the settings can be changed here.");
		
		// Setup main config
		mainConfigHandler.setupMainConfig();
		
		// Load messages
		loadMessages();

		// Load player and gang data
		getLogger().info(gangHandler.loadGangsFromConfig() + " gangs loaded!");

		// Register listeners
		registerListeners();

		// Startup save tasks
		startTasks();

		// Register gang command
		getCommand("prisongang").setExecutor(commands);

		// Check for Vault
		if (getServer().getPluginManager().getPlugin("Vault") != null) {
			vaultHandler = new VaultHandler(this);

		}

		getLogger().info(
				"PrisonGang v" + getDescription().getVersion()
						+ " has been enabled!");
	}

	public void onDisable() {

		// Save new gangdata
		saveManager.saveGangData();

		// Save gangdata file
		gangDataFile.reloadConfig();
		gangDataFile.saveConfig();

		// Save new data
		saveManager.savePlayerData();

		// Save playerdata file
		playerDataFile.reloadConfig();
		playerDataFile.saveConfig();

		// Save messages
		messagesFile.reloadConfig();
		messagesFile.saveConfig();
		
		// Save main config
		mainConfigFile.reloadConfig();
		mainConfigFile.saveConfig();

		// Stop all tasks running or scheduled
		getServer().getScheduler().cancelTasks(this);

		// Reset all gangs
		gangHandler.resetAllGangs();

		getLogger().info(
				"PrisonGang v" + getDescription().getVersion()
						+ " has been disabled!");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new PlayerJoinListener(this), this);
		getServer().getPluginManager().registerEvents(new ChatListener(this),
				this);
	}
	
	private void loadMessages() {
		Lang.setFile(messagesFile.getConfig());

		for (final Lang value : Lang.values()) {
			messagesFile.getConfig().addDefault(value.getPath(), value.getDefault());
		}

		messagesFile.getConfig().options().copyDefaults(true);
		messagesFile.saveConfig();
	}

	private void startTasks() {
		// Run the save tasks each minute
		getServer().getScheduler().runTaskTimerAsynchronously(this,
				new SavePlayerDataTask(this), 100L, 1200L);
		getServer().getScheduler().runTaskTimerAsynchronously(this,
				new SaveGangDataTask(this), 100L, 1200L);

		// Run refresh task once for when the server is reloaded
		getServer().getScheduler().runTask(this,
				new RefreshPlayerDataTask(this));

		// Run task every hour to check for elections
		getServer().getScheduler().runTaskTimer(this,
				new VotingCheckerTask(this), 0L, 3600 * 20);
	}

	public ConfigWrapper getGangDataFile() {
		return gangDataFile;
	}

	public ConfigWrapper getPlayerDataFile() {
		return playerDataFile;
	}

	public PlayerDataHandler getPlayerDataHandler() {
		return playerDataHandler;
	}

	public SaveManager getSaveManager() {
		return saveManager;
	}

	public GangHandler getGangHandler() {
		return gangHandler;
	}

	public Commands getCommands() {
		return commands;
	}

	public PermissionsManager getPermissionsManager() {
		return permManager;
	}

	public VaultHandler getVaultHandler() {
		return vaultHandler;
	}

	public ConfigWrapper getMainConfigFile() {
		return mainConfigFile;
	}

	public MainConfig getMainConfig() {
		return mainConfigHandler;
	}
}
