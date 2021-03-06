package me.staartvin.prisongang.commands;

import me.staartvin.prisongang.PrisonGang;
import me.staartvin.prisongang.gang.Gang;
import me.staartvin.prisongang.playerdata.PlayerData;
import me.staartvin.prisongang.translation.Lang;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CallVoteCommand implements CommandExecutor {

	private PrisonGang plugin;

	public CallVoteCommand(PrisonGang instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (args.length > 1) {
			sender.sendMessage(ChatColor.YELLOW
					+ "Usage: /gang callvote");
			return true;
		}

		PlayerData player;
		
		Gang gang;

		if (!(sender instanceof Player)) {
			sender.sendMessage(Lang.ONLY_PLAYER_ACTIVITY.getConfigValue(null));
			return true;
		}

		player = plugin.getPlayerDataHandler().getPlayerData(sender.getName(), false);

		if (!player.isInGang()) {
			sender.sendMessage(Lang.NOT_IN_A_GANG.getConfigValue(null));
			return true;
		}
		
		gang = plugin.getGangHandler().getGang(player.getGangName());
		
		if (gang == null) {
			sender.sendMessage(Lang.GANG_DOES_NOT_EXIST.getConfigValue(null));
			return true;
		}

		// Check if you can vote atm.
		if (gang.isVoteInProgress()) {
			sender.sendMessage(Lang.ELECTION_ALREADY_RUNNING.getConfigValue(null));
			return true;
		}
		
		gang.startElection();
		
		// Notify player
		//sender.sendMessage(ChatColor.GREEN + "You have voted for this election!");
		
		// Broadcast message in gang
		gang.broadcastMessage(Lang.PLAYER_STARTED_ELECTION.getConfigValue(new String[] {sender.getName()}));
		
		return true;
	}
}
