package com.bankorganizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("bankorganizer")
public interface BankOrganizerConfig extends Config
{
	@ConfigSection(
		name = "Tab Mappings",
		description = "Assign a category to each bank tab",
		position = 0
	)
	String tabMappingSection = "tabMappings";

	@ConfigSection(
		name = "Sorting",
		description = "How items are sorted within tabs",
		position = 1
	)
	String sortingSection = "sorting";

	@ConfigSection(
		name = "Custom Regex",
		description = "Custom regex patterns per category for additional item matching",
		position = 2
	)
	String regexSection = "customRegex";

	// --- Tab mappings ---

	@ConfigItem(keyName = "tab1Category", name = "Tab 1", description = "Category for bank tab 1", position = 0, section = tabMappingSection)
	default ItemCategory tab1Category() { return ItemCategory.TELEPORTS; }

	@ConfigItem(keyName = "tab2Category", name = "Tab 2", description = "Category for bank tab 2", position = 1, section = tabMappingSection)
	default ItemCategory tab2Category() { return ItemCategory.GEAR; }

	@ConfigItem(keyName = "tab3Category", name = "Tab 3", description = "Category for bank tab 3", position = 2, section = tabMappingSection)
	default ItemCategory tab3Category() { return ItemCategory.POTIONS; }

	@ConfigItem(keyName = "tab4Category", name = "Tab 4", description = "Category for bank tab 4", position = 3, section = tabMappingSection)
	default ItemCategory tab4Category() { return ItemCategory.FOOD; }

	@ConfigItem(keyName = "tab5Category", name = "Tab 5", description = "Category for bank tab 5", position = 4, section = tabMappingSection)
	default ItemCategory tab5Category() { return ItemCategory.SKILLING; }

	@ConfigItem(keyName = "tab6Category", name = "Tab 6", description = "Category for bank tab 6", position = 5, section = tabMappingSection)
	default ItemCategory tab6Category() { return ItemCategory.RAW_MATERIALS; }

	@ConfigItem(keyName = "tab7Category", name = "Tab 7", description = "Category for bank tab 7", position = 6, section = tabMappingSection)
	default ItemCategory tab7Category() { return ItemCategory.QUEST_MISC; }

	@ConfigItem(keyName = "tab8Category", name = "Tab 8", description = "Category for bank tab 8", position = 7, section = tabMappingSection)
	default ItemCategory tab8Category() { return ItemCategory.QUEST_MISC; }

	@ConfigItem(keyName = "tab9Category", name = "Tab 9", description = "Category for bank tab 9", position = 8, section = tabMappingSection)
	default ItemCategory tab9Category() { return ItemCategory.QUEST_MISC; }

	// --- Sorting ---

	@ConfigItem(keyName = "gearSortMode", name = "Gear Sort Mode", description = "How gear is sorted within the Gear tab", position = 0, section = sortingSection)
	default GearSortMode gearSortMode() { return GearSortMode.COMBAT_STYLE; }

	@ConfigItem(keyName = "teleportSortMode", name = "Teleport Sort Mode", description = "How teleport items are sorted within the Teleports tab", position = 1, section = sortingSection)
	default TeleportSortMode teleportSortMode() { return TeleportSortMode.RUNES_FIRST; }

	// --- Manual overrides (stored as JSON string) ---

	@ConfigItem(keyName = "manualOverrides", name = "", description = "", hidden = true)
	default String manualOverrides() { return ""; }

	@ConfigItem(keyName = "manualOverrides", name = "", description = "")
	void setManualOverrides(String json);

	// --- Custom regex ---

	@ConfigItem(keyName = "regexTeleports", name = "Teleports Regex", description = "Custom regex for Teleports category", position = 0, section = regexSection)
	default String regexTeleports() { return ""; }

	@ConfigItem(keyName = "regexGear", name = "Gear Regex", description = "Custom regex for Gear category", position = 1, section = regexSection)
	default String regexGear() { return ""; }

	@ConfigItem(keyName = "regexPotions", name = "Potions Regex", description = "Custom regex for Potions category", position = 2, section = regexSection)
	default String regexPotions() { return ""; }

	@ConfigItem(keyName = "regexFood", name = "Food Regex", description = "Custom regex for Food category", position = 3, section = regexSection)
	default String regexFood() { return ""; }

	@ConfigItem(keyName = "regexSkilling", name = "Skilling Regex", description = "Custom regex for Skilling category", position = 4, section = regexSection)
	default String regexSkilling() { return ""; }

	@ConfigItem(keyName = "regexRawMaterials", name = "Raw Materials Regex", description = "Custom regex for Raw Materials category", position = 5, section = regexSection)
	default String regexRawMaterials() { return ""; }

	@ConfigItem(keyName = "regexHighAlch", name = "High Alch Regex", description = "Custom regex for High Alch category", position = 6, section = regexSection)
	default String regexHighAlch() { return ""; }

	@ConfigItem(keyName = "regexCurrency", name = "Currency Regex", description = "Custom regex for Currency category", position = 7, section = regexSection)
	default String regexCurrency() { return ""; }
}
