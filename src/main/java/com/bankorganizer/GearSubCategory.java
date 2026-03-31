package com.bankorganizer;

public enum GearSubCategory
{
	MELEE_WEAPON("Melee Weapons"),
	MELEE_ARMOR("Melee Armor"),
	RANGED_WEAPON("Ranged Weapons"),
	RANGED_ARMOR("Ranged Armor"),
	MAGE_WEAPON("Mage Weapons"),
	MAGE_ARMOR("Mage Armor"),
	GENERAL("General Gear");

	private final String displayName;

	GearSubCategory(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName() { return displayName; }
}
