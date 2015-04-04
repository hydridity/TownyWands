package me.fastfelix771.townywands.inventory;

import java.util.ArrayList;

import me.fastfelix771.townywands.utils.Util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class InvPlayer {

	private static InvPlayer instance;

	public static final InvPlayer getInstance() {
		return instance;
	}

	public static TownyGUI gui;

	public static final void createGUI() {
		final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		items.add(Util.createItem("�2�lCreate a new town", "", 1, Material.BEACON));
		gui = new TownyGUI(null, "�6�lPlayer GUI", 18, TownyGUI.getNextID(), Util.list2array(items));
	}

}