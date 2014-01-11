package me.staartvin.prisongang.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import me.staartvin.prisongang.PrisonGang;
import me.staartvin.prisongang.gang.Gang;
import me.staartvin.prisongang.playerdata.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	private PrisonGang plugin;

	public ChatListener(PrisonGang instance) {
		plugin = instance;
	}

	public String gang_chat_prefix = "{PRISONGANG_GANG}";
	public String title_chat_prefix = "{PRISONGANG_TITLE}";
	public String chatmode_prefix = "{PRISONGANG_CHATMODE}";

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {

		Player player = event.getPlayer();
		PlayerData data = plugin.getPlayerDataHandler().getPlayerData(
				player.getName(), true);

		Set<Player> recip = event.getRecipients();

		String format = event.getFormat();

		// Keep track of players we have to remove.
		List<Player> removeables = new ArrayList<Player>();

		// Always global when not in a gang
		if (!data.isInGang()) {
			// Remove the prisongang tag
			format = format.replace(gang_chat_prefix, "")
					.replace(title_chat_prefix, "")
					.replace(chatmode_prefix, "");

			// Set new format without the extra spaces
			event.setFormat(removeExtraSpaces(format));

			return;
		}

		Gang gang = plugin.getGangHandler().getGang(data.getGangName());

		// For some reason the gang is null
		if (gang == null) {
			throw new NullPointerException("Gang is null of player "
					+ player.getName());
		}

		String chatMode = plugin.getCommands().chatMode.get(player.getName());

		if (chatMode == null) {
			chatMode = "global";
		}

		String chatModeName = null;

		if (chatMode.equals("global")) {
			chatModeName = "";
		} else if (chatMode.equals("ally-only")) {
			chatModeName = "Ally-Only";
		} else if (chatMode.equalsIgnoreCase("gang-only")) {
			chatModeName = "Gang-Only";
		}

		String rankName = (data.getRankName() != null) ? data.getRankName()
				: "";

		// Change format
		format = format.replace(gang_chat_prefix, gang.getGangName())
				.replace(title_chat_prefix, rankName)
				.replace(chatmode_prefix, chatModeName);
		
		// Remove double spaces
		format = format.replaceAll("\\s+", " ");
		
		// Fix chatcolours
		format = ChatColor.translateAlternateColorCodes('&', format);

		event.setFormat(format.trim());

		// Everyone ought to see the message, act as vanilla minecraft
		if (chatMode.equals("global"))
			return;

		// Used to show what chat mode you're in
		String chatPrefix = "";
		
		if (chatMode.equals("ally-only")) {
			// Ally and gang should see message

			chatPrefix = "[Ally]";
			
			for (Player pRecip : recip) {
				if (!plugin.getPlayerDataHandler().isAlly(player, pRecip)
						&& !plugin.getPlayerDataHandler().isGangPartner(player,
								pRecip)) {
					// Player is not an ally and not a gang partner, thus may not receive the message

					removeables.add(pRecip);
				}
			}
		} else if (chatMode.equals("gang-only")) {
			// Gang should see message
			
			chatPrefix = "[Gang]";

			for (Player pRecip : recip) {
				if (!plugin.getPlayerDataHandler()
						.isGangPartner(player, pRecip)) {
					// Player is not a gang partner, thus may not receive the message

					removeables.add(pRecip);
				}
			}
		}

		// Remove all players that cannot get this message.
		recip.removeAll(removeables);
		
		// Change so chatprefix is added to the format
		event.setFormat(chatPrefix + " " + event.getFormat());
	}

	private String removeExtraSpaces(String oldString) {
		// Remove double spaces
		oldString = oldString.replaceAll("\\s+", " ");

		StringBuilder newFormat = new StringBuilder(oldString);

		// Find extra space after '<'
		int charpoint = oldString.indexOf("%1$s") - 1;

		// Delete char so space is removed
		newFormat.deleteCharAt(charpoint);
		
		return newFormat.toString().trim();
	}
}
