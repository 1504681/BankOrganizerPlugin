package com.bankorganizer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ItemCategorizer
{
	private final Map<Integer, ItemCategory> itemIdMap = new HashMap<>();
	private final Map<ItemCategory, Pattern> regexPatternCache = new EnumMap<>(ItemCategory.class);

	// Manual overrides (item ID -> category), persisted in config
	private final Map<Integer, ItemCategory> manualOverrides = new HashMap<>();

	// Sub-category maps for gear and teleports
	private final Map<Integer, GearSubCategory> gearSubIdMap = new HashMap<>();
	private final Map<Integer, TeleportSubCategory> teleportSubIdMap = new HashMap<>();

	// Keyword sets for sub-categories
	private static final Set<String> MELEE_WEAPON_KEYWORDS = new HashSet<>(Arrays.asList(
		"scimitar", "longsword", "sword", "dagger", "mace", "warhammer", "battleaxe",
		"halberd", "spear", "hasta", "whip", "godsword", "rapier", "bludgeon",
		"abyssal dagger", "saradomin sword"
	));
	private static final Set<String> RANGED_WEAPON_KEYWORDS = new HashSet<>(Arrays.asList(
		"shortbow", "longbow", "crossbow", "blowpipe", "ballista",
		"dart", "javelin", "thrownaxe", "knife", "chinchompa"
	));
	private static final Set<String> MAGE_WEAPON_KEYWORDS = new HashSet<>(Arrays.asList(
		"staff", "wand", "trident", "sanguinesti", "nightmare staff",
		"kodai", "master wand", "ancient staff"
	));
	private static final Set<String> MELEE_ARMOR_KEYWORDS = new HashSet<>(Arrays.asList(
		"platebody", "platelegs", "plateskirt", "chainbody", "full helm", "med helm",
		"kiteshield", "sq shield", "defender", "berserker helm",
		"fighter torso", "obsidian", "bandos", "inquisitor", "justiciar"
	));
	private static final Set<String> RANGED_ARMOR_KEYWORDS = new HashSet<>(Arrays.asList(
		"d'hide", "dragonhide", "coif", "vambraces", "chaps",
		"armadyl", "karil", "crystal armour", "masori"
	));
	private static final Set<String> MAGE_ARMOR_KEYWORDS = new HashSet<>(Arrays.asList(
		"mystic", "infinity", "ahrim", "ancestral", "virtus",
		"mage's book", "malediction", "arcane"
	));

	// Rune item IDs for teleport sub-categorization
	private static final Set<Integer> RUNE_IDS = new HashSet<>(Arrays.asList(
		554, 555, 556, 557, 558, 559, 560, 561, 562, 563, 564, 565, 566, 9075, 21880
	));
	// Tablet item IDs
	private static final Set<Integer> TABLET_IDS = new HashSet<>(Arrays.asList(
		8007, 8008, 8009, 8010, 8011, 8012, 8013
	));
	// Jewelry item IDs (glory, dueling, games necklace, wealth, skills, combat bracelet, passage, burning, digsite)
	private static final Set<Integer> JEWELRY_IDS = new HashSet<>(Arrays.asList(
		1704, 1706, 1708, 1710, 1712, 11978, 11976,
		2552, 2554, 2556, 2558, 2560, 2562, 2564, 2566,
		3853, 3855, 3857, 3859, 3861, 3863, 3865, 3867,
		11980, 11982, 11984, 11986, 11988,
		11105, 11107, 11109, 11111, 11113, 11115,
		11118, 11120, 11122, 11124, 11126, 11128,
		21146, 21149, 21151, 21153, 21155,
		21166, 21169, 21171, 21173, 21175,
		11190, 11191, 11192, 11193, 11194
	));

	public ItemCategorizer()
	{
		initializeItemIdMap();
		initializeGearSubCategories();
	}

	private void initializeItemIdMap()
	{
		// === TELEPORTS ===
		// Runes
		itemIdMap.put(554, ItemCategory.TELEPORTS); // Fire rune
		itemIdMap.put(555, ItemCategory.TELEPORTS); // Water rune
		itemIdMap.put(556, ItemCategory.TELEPORTS); // Air rune
		itemIdMap.put(557, ItemCategory.TELEPORTS); // Earth rune
		itemIdMap.put(558, ItemCategory.TELEPORTS); // Mind rune
		itemIdMap.put(559, ItemCategory.TELEPORTS); // Body rune
		itemIdMap.put(560, ItemCategory.TELEPORTS); // Death rune
		itemIdMap.put(561, ItemCategory.TELEPORTS); // Nature rune
		itemIdMap.put(562, ItemCategory.TELEPORTS); // Chaos rune
		itemIdMap.put(563, ItemCategory.TELEPORTS); // Law rune
		itemIdMap.put(564, ItemCategory.TELEPORTS); // Cosmic rune
		itemIdMap.put(565, ItemCategory.TELEPORTS); // Blood rune
		itemIdMap.put(566, ItemCategory.TELEPORTS); // Soul rune
		itemIdMap.put(9075, ItemCategory.TELEPORTS); // Astral rune
		itemIdMap.put(21880, ItemCategory.TELEPORTS); // Wrath rune

		// Teleport tablets
		itemIdMap.put(8007, ItemCategory.TELEPORTS); // Varrock teleport
		itemIdMap.put(8008, ItemCategory.TELEPORTS); // Lumbridge teleport
		itemIdMap.put(8009, ItemCategory.TELEPORTS); // Falador teleport
		itemIdMap.put(8010, ItemCategory.TELEPORTS); // Camelot teleport
		itemIdMap.put(8011, ItemCategory.TELEPORTS); // Ardougne teleport
		itemIdMap.put(8012, ItemCategory.TELEPORTS); // Watchtower teleport
		itemIdMap.put(8013, ItemCategory.TELEPORTS); // House teleport

		// Rune pouch
		itemIdMap.put(12791, ItemCategory.TELEPORTS);

		// Teleport jewelry
		itemIdMap.put(1704, ItemCategory.TELEPORTS);  // Glory(4)
		itemIdMap.put(1706, ItemCategory.TELEPORTS);  // Glory(3)
		itemIdMap.put(1708, ItemCategory.TELEPORTS);  // Glory(2)
		itemIdMap.put(1710, ItemCategory.TELEPORTS);  // Glory(1)
		itemIdMap.put(1712, ItemCategory.TELEPORTS);  // Glory (uncharged)
		itemIdMap.put(11978, ItemCategory.TELEPORTS); // Glory(6)
		itemIdMap.put(11976, ItemCategory.TELEPORTS); // Glory(5)
		itemIdMap.put(2552, ItemCategory.TELEPORTS);  // Ring of dueling(8)
		itemIdMap.put(2554, ItemCategory.TELEPORTS);
		itemIdMap.put(2556, ItemCategory.TELEPORTS);
		itemIdMap.put(2558, ItemCategory.TELEPORTS);
		itemIdMap.put(2560, ItemCategory.TELEPORTS);
		itemIdMap.put(2562, ItemCategory.TELEPORTS);
		itemIdMap.put(2564, ItemCategory.TELEPORTS);
		itemIdMap.put(2566, ItemCategory.TELEPORTS);
		itemIdMap.put(3853, ItemCategory.TELEPORTS);  // Games necklace(8)
		itemIdMap.put(3855, ItemCategory.TELEPORTS);
		itemIdMap.put(3857, ItemCategory.TELEPORTS);
		itemIdMap.put(3859, ItemCategory.TELEPORTS);
		itemIdMap.put(3861, ItemCategory.TELEPORTS);
		itemIdMap.put(3863, ItemCategory.TELEPORTS);
		itemIdMap.put(3865, ItemCategory.TELEPORTS);
		itemIdMap.put(3867, ItemCategory.TELEPORTS);
		itemIdMap.put(11980, ItemCategory.TELEPORTS); // Ring of wealth(5)
		itemIdMap.put(11982, ItemCategory.TELEPORTS);
		itemIdMap.put(11984, ItemCategory.TELEPORTS);
		itemIdMap.put(11986, ItemCategory.TELEPORTS);
		itemIdMap.put(11988, ItemCategory.TELEPORTS);
		itemIdMap.put(11105, ItemCategory.TELEPORTS); // Skills necklace(6)
		itemIdMap.put(11107, ItemCategory.TELEPORTS);
		itemIdMap.put(11109, ItemCategory.TELEPORTS);
		itemIdMap.put(11111, ItemCategory.TELEPORTS);
		itemIdMap.put(11113, ItemCategory.TELEPORTS);
		itemIdMap.put(11115, ItemCategory.TELEPORTS);
		itemIdMap.put(11118, ItemCategory.TELEPORTS); // Combat bracelet(6)
		itemIdMap.put(11120, ItemCategory.TELEPORTS);
		itemIdMap.put(11122, ItemCategory.TELEPORTS);
		itemIdMap.put(11124, ItemCategory.TELEPORTS);
		itemIdMap.put(11126, ItemCategory.TELEPORTS);
		itemIdMap.put(11128, ItemCategory.TELEPORTS);
		itemIdMap.put(21146, ItemCategory.TELEPORTS); // Necklace of passage(5)
		itemIdMap.put(21149, ItemCategory.TELEPORTS);
		itemIdMap.put(21151, ItemCategory.TELEPORTS);
		itemIdMap.put(21153, ItemCategory.TELEPORTS);
		itemIdMap.put(21155, ItemCategory.TELEPORTS);
		itemIdMap.put(21166, ItemCategory.TELEPORTS); // Burning amulet(5)
		itemIdMap.put(21169, ItemCategory.TELEPORTS);
		itemIdMap.put(21171, ItemCategory.TELEPORTS);
		itemIdMap.put(21173, ItemCategory.TELEPORTS);
		itemIdMap.put(21175, ItemCategory.TELEPORTS);
		itemIdMap.put(11190, ItemCategory.TELEPORTS); // Digsite pendant(5)
		itemIdMap.put(11191, ItemCategory.TELEPORTS);
		itemIdMap.put(11192, ItemCategory.TELEPORTS);
		itemIdMap.put(11193, ItemCategory.TELEPORTS);
		itemIdMap.put(11194, ItemCategory.TELEPORTS);

		// === GEAR ===
		itemIdMap.put(7462, ItemCategory.GEAR);  // Barrows gloves
		itemIdMap.put(6570, ItemCategory.GEAR);  // Fire cape
		itemIdMap.put(21295, ItemCategory.GEAR); // Infernal cape
		itemIdMap.put(10499, ItemCategory.GEAR); // Ava's accumulator
		itemIdMap.put(22109, ItemCategory.GEAR); // Ava's assembler
		itemIdMap.put(4089, ItemCategory.GEAR);  // Mystic hat
		itemIdMap.put(4091, ItemCategory.GEAR);  // Mystic robe top
		itemIdMap.put(4093, ItemCategory.GEAR);  // Mystic robe bottom
		itemIdMap.put(4095, ItemCategory.GEAR);  // Mystic hat (dark)
		itemIdMap.put(4097, ItemCategory.GEAR);  // Mystic robe top (dark)
		itemIdMap.put(4099, ItemCategory.GEAR);  // Mystic robe bottom (dark)
		itemIdMap.put(4101, ItemCategory.GEAR);  // Mystic hat (light)
		itemIdMap.put(4103, ItemCategory.GEAR);  // Mystic robe top (light)
		itemIdMap.put(4105, ItemCategory.GEAR);  // Mystic robe bottom (light)
		itemIdMap.put(4107, ItemCategory.GEAR);  // Mystic gloves
		itemIdMap.put(4109, ItemCategory.GEAR);  // Mystic boots

		// === POTIONS ===
		itemIdMap.put(12695, ItemCategory.POTIONS);
		itemIdMap.put(12697, ItemCategory.POTIONS);
		itemIdMap.put(12699, ItemCategory.POTIONS);
		itemIdMap.put(12701, ItemCategory.POTIONS);
		itemIdMap.put(2434, ItemCategory.POTIONS);
		itemIdMap.put(139, ItemCategory.POTIONS);
		itemIdMap.put(141, ItemCategory.POTIONS);
		itemIdMap.put(143, ItemCategory.POTIONS);
		itemIdMap.put(6685, ItemCategory.POTIONS);
		itemIdMap.put(6687, ItemCategory.POTIONS);
		itemIdMap.put(6689, ItemCategory.POTIONS);
		itemIdMap.put(6691, ItemCategory.POTIONS);

		// === FOOD ===
		itemIdMap.put(385, ItemCategory.FOOD);
		itemIdMap.put(379, ItemCategory.FOOD);
		itemIdMap.put(373, ItemCategory.FOOD);
		itemIdMap.put(7946, ItemCategory.FOOD);
		itemIdMap.put(391, ItemCategory.FOOD);
		itemIdMap.put(13441, ItemCategory.FOOD);
		itemIdMap.put(11936, ItemCategory.FOOD);
		itemIdMap.put(3144, ItemCategory.FOOD);

		// === TOOLS ===
		itemIdMap.put(1755, ItemCategory.TOOLS);
		itemIdMap.put(2347, ItemCategory.TOOLS);
		itemIdMap.put(590, ItemCategory.TOOLS);
		itemIdMap.put(946, ItemCategory.TOOLS);
		itemIdMap.put(1735, ItemCategory.TOOLS);
		itemIdMap.put(952, ItemCategory.TOOLS);

		// === RAW MATERIALS ===
		itemIdMap.put(1436, ItemCategory.RAW_MATERIALS);
		itemIdMap.put(7936, ItemCategory.RAW_MATERIALS);
		itemIdMap.put(314, ItemCategory.RAW_MATERIALS);
		itemIdMap.put(526, ItemCategory.RAW_MATERIALS);
		itemIdMap.put(23490, ItemCategory.RAW_MATERIALS); // User-contributed
		itemIdMap.put(26792, ItemCategory.RAW_MATERIALS); // User-contributed
		itemIdMap.put(13391, ItemCategory.RAW_MATERIALS); // User-contributed
		itemIdMap.put(28599, ItemCategory.RAW_MATERIALS); // User-contributed

		// === HIGH ALCH ===
		itemIdMap.put(1393, ItemCategory.HIGH_ALCH); // User-contributed

		// === QUEST/MISC ===
		itemIdMap.put(12785, ItemCategory.QUEST_MISC); // User-contributed

		// === User-contributed GEAR ===
		itemIdMap.put(12610, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(12006, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(29031, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(29033, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(29043, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(29045, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(4153, ItemCategory.GEAR);   // Granite maul
		itemIdMap.put(29594, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(11902, ItemCategory.GEAR);  // User-contributed
		itemIdMap.put(11, ItemCategory.GEAR);     // User-contributed

		// === User-contributed TELEPORTS ===
		itemIdMap.put(22947, ItemCategory.TELEPORTS); // User-contributed
		itemIdMap.put(28327, ItemCategory.TELEPORTS); // User-contributed
		itemIdMap.put(25930, ItemCategory.TELEPORTS); // User-contributed
		itemIdMap.put(21389, ItemCategory.TELEPORTS); // User-contributed
		itemIdMap.put(9781, ItemCategory.TELEPORTS);  // User-contributed
	}

	private void initializeGearSubCategories()
	{
		// Mystic robes -> MAGE_ARMOR
		for (int id : new int[]{4089, 4091, 4093, 4095, 4097, 4099, 4101, 4103, 4105, 4107, 4109})
		{
			gearSubIdMap.put(id, GearSubCategory.MAGE_ARMOR);
		}
		// Ava's -> RANGED_ARMOR
		gearSubIdMap.put(10499, GearSubCategory.RANGED_ARMOR);
		gearSubIdMap.put(22109, GearSubCategory.RANGED_ARMOR);
		// Fire cape, Infernal cape -> MELEE_ARMOR (general melee bis)
		gearSubIdMap.put(6570, GearSubCategory.MELEE_ARMOR);
		gearSubIdMap.put(21295, GearSubCategory.MELEE_ARMOR);
		// Barrows gloves -> GENERAL
		gearSubIdMap.put(7462, GearSubCategory.GENERAL);

		// Teleport sub-categories are determined by ID sets (RUNE_IDS, TABLET_IDS, JEWELRY_IDS)
	}

	/**
	 * Categorize an item. Priority: manual override > ID map > keywords > regex > catch-all.
	 */
	public ItemCategory categorize(String itemName, int itemId)
	{
		// Priority 0: Manual override
		ItemCategory override = manualOverrides.get(itemId);
		if (override != null)
		{
			return override;
		}

		// Priority 1: Hardcoded item ID
		ItemCategory idMatch = itemIdMap.get(itemId);
		if (idMatch != null)
		{
			return idMatch;
		}

		String lowerName = itemName.toLowerCase();

		// Priority 2: Keyword matching
		for (ItemCategory category : ItemCategory.values())
		{
			if (category == ItemCategory.QUEST_MISC)
			{
				continue;
			}
			for (String keyword : category.getKeywords())
			{
				if (lowerName.contains(keyword.toLowerCase()))
				{
					return category;
				}
			}
		}

		// Priority 3: User-defined regex
		for (Map.Entry<ItemCategory, Pattern> entry : regexPatternCache.entrySet())
		{
			if (entry.getValue().matcher(lowerName).find())
			{
				return entry.getKey();
			}
		}

		return ItemCategory.QUEST_MISC;
	}

	/**
	 * Determine the gear sub-category for an item (only meaningful if category is GEAR).
	 */
	public GearSubCategory getGearSubCategory(String itemName, int itemId)
	{
		// Check ID map first
		GearSubCategory idSub = gearSubIdMap.get(itemId);
		if (idSub != null)
		{
			return idSub;
		}

		String lowerName = itemName.toLowerCase();

		// Check weapon keywords first (more specific)
		for (String kw : MELEE_WEAPON_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.MELEE_WEAPON;
		}
		for (String kw : RANGED_WEAPON_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.RANGED_WEAPON;
		}
		for (String kw : MAGE_WEAPON_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.MAGE_WEAPON;
		}

		// Check armor keywords
		for (String kw : MELEE_ARMOR_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.MELEE_ARMOR;
		}
		for (String kw : RANGED_ARMOR_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.RANGED_ARMOR;
		}
		for (String kw : MAGE_ARMOR_KEYWORDS)
		{
			if (lowerName.contains(kw)) return GearSubCategory.MAGE_ARMOR;
		}

		return GearSubCategory.GENERAL;
	}

	/**
	 * Determine the teleport sub-category for an item (only meaningful if category is TELEPORTS).
	 */
	public TeleportSubCategory getTeleportSubCategory(String itemName, int itemId)
	{
		if (RUNE_IDS.contains(itemId))
		{
			return TeleportSubCategory.RUNES;
		}
		if (JEWELRY_IDS.contains(itemId))
		{
			return TeleportSubCategory.JEWELRY;
		}
		if (TABLET_IDS.contains(itemId))
		{
			return TeleportSubCategory.TABLETS;
		}

		// Keyword fallbacks for items not in ID sets
		String lowerName = itemName.toLowerCase();
		if (lowerName.contains("rune"))
		{
			return TeleportSubCategory.RUNES;
		}
		if (lowerName.contains("glory(") || lowerName.contains("dueling(")
			|| lowerName.contains("necklace(") || lowerName.contains("bracelet(")
			|| lowerName.contains("wealth(") || lowerName.contains("passage(")
			|| lowerName.contains("pendant(") || lowerName.contains("amulet("))
		{
			return TeleportSubCategory.JEWELRY;
		}
		if (lowerName.contains("teleport") && !lowerName.contains("("))
		{
			return TeleportSubCategory.TABLETS;
		}

		return TeleportSubCategory.OTHER;
	}

	/**
	 * Get the sort priority for a gear item given a sort mode.
	 * Lower numbers sort first.
	 */
	public int getGearSortOrder(GearSubCategory sub, GearSortMode mode)
	{
		if (mode == GearSortMode.COMBAT_STYLE)
		{
			// Melee weapon+armor, Ranged weapon+armor, Mage weapon+armor, General
			switch (sub)
			{
				case MELEE_WEAPON: return 0;
				case MELEE_ARMOR: return 1;
				case RANGED_WEAPON: return 2;
				case RANGED_ARMOR: return 3;
				case MAGE_WEAPON: return 4;
				case MAGE_ARMOR: return 5;
				case GENERAL: return 6;
				default: return 7;
			}
		}
		else
		{
			// Equipment Type: all weapons by style, then all armor by style
			switch (sub)
			{
				case MELEE_WEAPON: return 0;
				case RANGED_WEAPON: return 1;
				case MAGE_WEAPON: return 2;
				case MELEE_ARMOR: return 3;
				case RANGED_ARMOR: return 4;
				case MAGE_ARMOR: return 5;
				case GENERAL: return 6;
				default: return 7;
			}
		}
	}

	/**
	 * Get the sort priority for a teleport item given a sort mode.
	 */
	public int getTeleportSortOrder(TeleportSubCategory sub, TeleportSortMode mode)
	{
		TeleportSubCategory[] order;
		switch (mode)
		{
			case RUNES_FIRST:
				order = new TeleportSubCategory[]{
					TeleportSubCategory.RUNES, TeleportSubCategory.JEWELRY,
					TeleportSubCategory.TABLETS, TeleportSubCategory.OTHER
				};
				break;
			case JEWELRY_FIRST:
				order = new TeleportSubCategory[]{
					TeleportSubCategory.JEWELRY, TeleportSubCategory.RUNES,
					TeleportSubCategory.TABLETS, TeleportSubCategory.OTHER
				};
				break;
			case TABLETS_FIRST:
				order = new TeleportSubCategory[]{
					TeleportSubCategory.TABLETS, TeleportSubCategory.RUNES,
					TeleportSubCategory.JEWELRY, TeleportSubCategory.OTHER
				};
				break;
			default:
				return 0;
		}

		for (int i = 0; i < order.length; i++)
		{
			if (order[i] == sub) return i;
		}
		return order.length;
	}

	// === Manual overrides ===

	public void setManualOverride(int itemId, ItemCategory category)
	{
		manualOverrides.put(itemId, category);
	}

	public void removeManualOverride(int itemId)
	{
		manualOverrides.remove(itemId);
	}

	public boolean hasManualOverride(int itemId)
	{
		return manualOverrides.containsKey(itemId);
	}

	public Map<Integer, ItemCategory> getManualOverrides()
	{
		return manualOverrides;
	}

	public void loadManualOverrides(Map<Integer, ItemCategory> overrides)
	{
		manualOverrides.clear();
		if (overrides != null)
		{
			manualOverrides.putAll(overrides);
		}
	}

	public void setRegexPatterns(Map<ItemCategory, String> patterns)
	{
		regexPatternCache.clear();
		for (Map.Entry<ItemCategory, String> entry : patterns.entrySet())
		{
			String pattern = entry.getValue();
			if (pattern != null && !pattern.trim().isEmpty())
			{
				try
				{
					regexPatternCache.put(entry.getKey(), Pattern.compile(pattern.trim(), Pattern.CASE_INSENSITIVE));
				}
				catch (PatternSyntaxException ignored)
				{
				}
			}
		}
	}
}
