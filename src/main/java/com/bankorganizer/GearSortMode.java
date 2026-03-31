package com.bankorganizer;

public enum GearSortMode
{
	COMBAT_STYLE("Combat Style"),
	EQUIPMENT_TYPE("Equipment Type");

	private final String displayName;

	GearSortMode(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName() { return displayName; }

	@Override
	public String toString() { return displayName; }
}
