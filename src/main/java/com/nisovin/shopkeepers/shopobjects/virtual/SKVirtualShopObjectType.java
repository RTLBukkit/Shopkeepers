package com.nisovin.shopkeepers.shopobjects.virtual;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopobjects.virtual.VirtualShopObjectType;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.AbstractShopObjectType;

// TODO Not yet used.
public class SKVirtualShopObjectType extends AbstractShopObjectType<SKVirtualShopObject> implements VirtualShopObjectType<SKVirtualShopObject> {

	private final VirtualShops virtualShops;

	public SKVirtualShopObjectType(VirtualShops virtualShops) {
		super("virtual", "shopkeeper.virtual");
		this.virtualShops = virtualShops;
	}

	// TODO Maybe require virtual shopkeepers to use unique names and identify them by that?
	// -> Requires changes to the shopkeeper creation via command.
	public String createObjectId(Shopkeeper shopkeeper) {
		if (shopkeeper == null) return null;
		return this.getIdentifier() + ":" + shopkeeper.getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return "virtual"; // TODO Message setting
	}

	@Override
	public SKVirtualShopObject createObject(AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		return new SKVirtualShopObject(virtualShops, shopkeeper, creationData);
	}

	@Override
	public boolean isEnabled() {
		return false; // TODO Add setting
	}

	@Override
	public boolean isValidSpawnLocation(Location spawnLocation, BlockFace targetedBlockFace) {
		return true; // Does not use any spawn location
	}
}
