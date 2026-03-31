package com.bankorganizer;

import java.awt.Color;
import net.runelite.client.config.Alpha;
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
		name = "Colors",
		description = "Customize overlay colors for each category",
		position = 2,
		closedByDefault = true
	)
	String colorsSection = "colors";

	@ConfigSection(
		name = "Custom Regex",
		description = "Custom regex patterns per category for additional item matching",
		position = 3
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

	// --- Colors ---

	@Alpha
	@ConfigItem(keyName = "colorTeleports", name = "Teleports", description = "Overlay color for Teleports", position = 0, section = colorsSection)
	default Color colorTeleports() { return new Color(0, 150, 255); }

	@Alpha
	@ConfigItem(keyName = "colorCombat", name = "Combat", description = "Overlay color for Combat", position = 1, section = colorsSection)
	default Color colorCombat() { return new Color(220, 50, 50); }

	@Alpha
	@ConfigItem(keyName = "colorPotions", name = "Potions", description = "Overlay color for Potions", position = 2, section = colorsSection)
	default Color colorPotions() { return new Color(0, 200, 0); }

	@Alpha
	@ConfigItem(keyName = "colorFood", name = "Food", description = "Overlay color for Food", position = 3, section = colorsSection)
	default Color colorFood() { return new Color(160, 32, 240); }

	@Alpha
	@ConfigItem(keyName = "colorSkilling", name = "Skilling", description = "Overlay color for Skilling", position = 4, section = colorsSection)
	default Color colorSkilling() { return new Color(255, 255, 0); }

	@Alpha
	@ConfigItem(keyName = "colorMaterials", name = "Materials", description = "Overlay color for Materials", position = 5, section = colorsSection)
	default Color colorMaterials() { return new Color(255, 0, 200); }

	@Alpha
	@ConfigItem(keyName = "colorHighAlch", name = "High Alch", description = "Overlay color for High Alch", position = 6, section = colorsSection)
	default Color colorHighAlch() { return new Color(255, 255, 255); }

	@Alpha
	@ConfigItem(keyName = "colorCurrency", name = "Currency", description = "Overlay color for Currency", position = 7, section = colorsSection)
	default Color colorCurrency() { return new Color(255, 215, 80); }

	@Alpha
	@ConfigItem(keyName = "colorQuestMisc", name = "Quest/Misc", description = "Overlay color for Quest/Misc", position = 8, section = colorsSection)
	default Color colorQuestMisc() { return new Color(180, 100, 255); }

	// --- Manual overrides (stored as JSON string) ---

	@ConfigItem(keyName = "manualOverrides", name = "", description = "", hidden = true)
	default String manualOverrides() { return ""; }

	@ConfigItem(keyName = "manualOverrides", name = "", description = "")
	void setManualOverrides(String json);

	@ConfigItem(keyName = "subCategoryOverrides", name = "", description = "", hidden = true)
	default String subCategoryOverrides() { return ""; }

	@ConfigItem(keyName = "subCategoryOverrides", name = "", description = "")
	void setSubCategoryOverrides(String json);

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
