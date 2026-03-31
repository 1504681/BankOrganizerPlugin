package com.bankorganizer;

public enum TeleportSubCategory
{
	RUNES("Runes"),
	JEWELRY("Jewelry"),
	TABLETS("Tablets"),
	OTHER("Other Teleports");

	private final String displayName;

	TeleportSubCategory(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName() { return displayName; }
}
