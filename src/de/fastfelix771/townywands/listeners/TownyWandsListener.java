package de.fastfelix771.townywands.listeners;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import de.fastfelix771.townywands.api.ModularGUI;
import de.fastfelix771.townywands.api.events.GuiClickEvent;
import de.fastfelix771.townywands.api.events.GuiOpenEvent;
import de.fastfelix771.townywands.inventory.ItemWrapper;
import de.fastfelix771.townywands.lang.Language;
import de.fastfelix771.townywands.main.TownyWands;
import de.fastfelix771.townywands.packets.Version;
import de.fastfelix771.townywands.utils.Reflect;
import de.fastfelix771.townywands.utils.Updater.State;

public class TownyWandsListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {

		Player p = e.getPlayer();

		if(TownyWands.getVirtualSign() != null) {
			TownyWands.getVirtualSign().setup(p);
		}

		if(!TownyWands.isUpdateCheckingEnabled() || TownyWands.getUpdateResult() == null || TownyWands.getUpdateResult().getState() != State.UPDATE_FOUND) return;
		if ((p.isOp() || p.hasPermission("townywands.msg.update"))) {
			p.sendMessage("§4!UPDATE! §6-> TownyWands has found an update!");
			p.sendMessage("§4!UPDATE! §6-> You are currently on version §c" + TownyWands.getInstance().getDescription().getVersion());
			p.sendMessage("§4!UPDATE! §6-> Newest version is §c" + TownyWands.getUpdateResult().getLatestVersion());

			if (Reflect.getServerVersion().isOlderThan(Version.v1_8)) {
				p.sendMessage("§4!UPDATE! §6-> Download latest: §a" + TownyWands.getUpdateResult().getLatestURL());
				return;
			}

			if(Reflect.getServerVersion().isNewerThan(Version.v1_7)) {
				if(Reflect.getClass("net.md_5.bungee.api.chat.TextComponent") == null) return;

				net.md_5.bungee.api.chat.TextComponent text = new net.md_5.bungee.api.chat.TextComponent("§4!UPDATE! §6-> Download latest: §a§l[Click Me]");
				text.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, TownyWands.getUpdateResult().getLatestURL()));
				p.spigot().sendMessage(text);
			}

		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		TownyWands.getVirtualSign().unsetup(e.getPlayer());
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e) {
		if(e.getMessage().trim().isEmpty() || e.getMessage().trim().length() < 2) return;

		String command = e.getMessage().substring(1, e.getMessage().length());
		Player p = e.getPlayer();
		Language language = Language.getLanguage(p);
		ModularGUI gui = ModularGUI.fromCommand(command);

		if(gui == null) return;
		e.setCancelled(true);

		if (!p.hasPermission(gui.getPermission())) {
			p.sendMessage("§cYou are missing the permission '§a" + gui.getPermission() + "§c'.");
			return;
		}

		if(gui.getInventory() == null) {
			p.sendMessage("§cThis GUI has no inventory enabled at the moment!");
			p.sendMessage(String.format("§a%d §cdisabled inventories has been found, though!", gui.getInventories().size()));
			return;
		}
		
		if (gui.contains(lang)) inv = gui.get(lang);
		else {
			if (!gui.contains(Language.ENGLISH)) {
				p.sendMessage("§cTownyWands | §aThere is no GUI registered in your language nor the default one (§6ENGLISH§a)!");
				p.sendMessage("§cPlease report this to an administrator!");
				return;
			}
			inv = gui.get(Language.ENGLISH);
		}

		GuiOpenEvent event = new GuiOpenEvent(p, gui, gui.getInventory(), language);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;
		
		Inventory inv = event.getInventory().toInventory();
		if(inv != null) p.openInventory(inv);
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();

		if ((item == null) || item.getType().equals(Material.AIR)) return;
		ItemWrapper wrapper = ItemWrapper.wrap(item);

		if(!wrapper.hasNBTKey("townywands_id")) return;
		e.setCancelled(true);

		ItemWrapper eventWrapper = wrapper.clone(); // Prevents GUI modifications on accident, and allows for adding commands etc. on-the-fly.
		GuiClickEvent event = new GuiClickEvent(eventWrapper, p, Database.get(wrapper.getValue("key", String.class)), Database.get(wrapper.getValue("key", String.class), Language.getLanguage(p)));
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return;

		onGuiClick(event);
	}
	
	private void onGuiClick(GuiClickEvent e) {

		Player p = e.getPlayer();

		Set<String> commands = e.getItem().getCommands();
		Set<String> console_commands = e.getItem().getConsoleCommands();

		if (commands != null && !commands.isEmpty()) {
			for (String cmd : commands) {
				if (cmd.trim().isEmpty()) continue;

				cmd = cmd.replace("{playername}", p.getName());
				cmd = cmd.replace("{uuid}", p.getUniqueId().toString());
				cmd = cmd.replace("{world}", p.getWorld().getName());
				cmd = cmd.replace("{displayname}", p.getDisplayName());

				Bukkit.dispatchCommand(p, cmd);
			}
		}

		if (console_commands != null && console_commands.isEmpty()) {
			for (String cmd : console_commands) {
				if (cmd.trim().isEmpty()) continue;

				cmd = cmd.replace("{playername}", p.getName());
				cmd = cmd.replace("{uuid}", p.getUniqueId().toString());
				cmd = cmd.replace("{world}", p.getWorld().getName());
				cmd = cmd.replace("{displayname}", p.getDisplayName());

				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}

	}

}