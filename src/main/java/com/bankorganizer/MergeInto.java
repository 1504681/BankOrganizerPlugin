package com.bankorganizer;

/** Config dropdown: which category's tab a category should share, if any. */
public enum MergeInto
{
	NONE("Keep separate", null),
	TELEPORTS("Teleports", ItemCategory.TELEPORTS),
	COMBAT("Combat", ItemCategory.GEAR),
	POTIONS("Potions", ItemCategory.POTIONS),
	FOOD("Food", ItemCategory.FOOD),
	SKILLING("Skilling", ItemCategory.SKILLING),
	MATERIALS("Materials", ItemCategory.RAW_MATERIALS),
	HIGH_ALCH("High Alch", ItemCategory.HIGH_ALCH),
	MAIN_TAB("Main Tab", ItemCategory.CURRENCY),
	QUEST("Quest", ItemCategory.QUEST),
	MISC("Misc", ItemCategory.QUEST_MISC);

	private final String displayName;
	private final ItemCategory category;

	MergeInto(String displayName, ItemCategory category)
	{
		this.displayName = displayName;
		this.category = category;
	}

	/** The target category, or null for NONE. */
	public ItemCategory getCategory() { return category; }

	@Override
	public String toString() { return displayName; }
}
