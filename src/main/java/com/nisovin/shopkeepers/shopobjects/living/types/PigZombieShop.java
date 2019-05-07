package com.nisovin.shopkeepers.shopobjects.living.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.Settings;
import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObject;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;
import com.nisovin.shopkeepers.ui.defaults.EditorHandler;
import com.nisovin.shopkeepers.util.ItemUtils;
import com.nisovin.shopkeepers.util.Log;

// TODO use BabyableShop as base once there is a common interface for this inside bukkit
public class PigZombieShop extends SKLivingShopObject<PigZombie> {

	private static final boolean DEFAULT_BABY = false;

	private boolean baby = DEFAULT_BABY;

	public PigZombieShop(	LivingShops livingShops, SKLivingShopObjectType<PigZombieShop> livingObjectType,
							AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		this.loadBaby(configSection);
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		this.saveBaby(configSection);
	}

	@Override
	protected void onSpawn(PigZombie entity) {
		super.onSpawn(entity);
		this.applyBaby(entity);
	}

	@Override
	public List<EditorHandler.Button> getEditorButtons() {
		List<EditorHandler.Button> editorButtons = new ArrayList<>();
		editorButtons.addAll(super.getEditorButtons());
		editorButtons.add(this.getBabyEditorButton());
		return editorButtons;
	}

	// BABY STATE

	private void loadBaby(ConfigurationSection configSection) {
		if (!configSection.isBoolean("baby")) {
			Log.warning("Missing or invalid 'baby' state for shopkeeper " + shopkeeper.getId()
					+ "'. Using '" + DEFAULT_BABY + "' now.");
			baby = DEFAULT_BABY;
			shopkeeper.markDirty();
		} else {
			baby = configSection.getBoolean("baby");
		}
	}

	private void saveBaby(ConfigurationSection configSection) {
		configSection.set("baby", baby);
	}

	public void setBaby(boolean baby) {
		this.baby = baby;
		shopkeeper.markDirty();
		this.applyBaby(this.getEntity()); // null if not active
	}

	private void applyBaby(PigZombie entity) {
		if (entity == null) return;
		entity.setBaby(baby);
	}

	public void cycleBaby() {
		this.setBaby(!baby);
	}

	private ItemStack getBabyEditorItem() {
		// TODO use mob-specific spawn egg (if available; some mobs (illusioner) don't have a spawn egg)?
		// on the other hand: using a single item consistently for the editor icon has benefits as well
		ItemStack iconItem = new ItemStack(Material.EGG);
		// TODO use more specific text
		ItemUtils.setItemStackNameAndLore(iconItem, Settings.msgButtonType, Settings.msgButtonTypeLore);
		return iconItem;
	}

	private EditorHandler.Button getBabyEditorButton() {
		return new EditorHandler.ActionButton(shopkeeper) {
			@Override
			public ItemStack getIcon(EditorHandler.Session session) {
				return getBabyEditorItem();
			}

			@Override
			protected boolean runAction(InventoryClickEvent clickEvent, Player player) {
				cycleBaby();
				return true;
			}
		};
	}
}
