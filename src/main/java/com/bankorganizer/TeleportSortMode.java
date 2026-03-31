package com.bankorganizer;

public enum TeleportSortMode
{
	RUNES_FIRST("Runes First"),
	JEWELRY_FIRST("Jewelry First"),
	TABLETS_FIRST("Tablets First");

	private final String displayName;

	TeleportSortMode(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName() { return displayName; }

	@Override
	public String toString() { return displayName; }
}
