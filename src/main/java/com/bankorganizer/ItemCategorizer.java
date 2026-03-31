package com.bankorganizer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ItemCategorizer
{
	private final Map<Integer, ItemCategory> itemIdMap = new HashMap<>();
	private final Map<ItemCategory, Pattern> regexPatternCache = new EnumMap<>(ItemCategory.class);

	public ItemCategorizer()
	{
		initializeItemIdMap();
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

		// Teleport jewelry — Amulet of glory (1-6 charges + uncharged)
		itemIdMap.put(1704, ItemCategory.TELEPORTS);  // Glory(4)
		itemIdMap.put(1706, ItemCategory.TELEPORTS);  // Glory(3)
		itemIdMap.put(1708, ItemCategory.TELEPORTS);  // Glory(2)
		itemIdMap.put(1710, ItemCategory.TELEPORTS);  // Glory(1)
		itemIdMap.put(1712, ItemCategory.TELEPORTS);  // Glory (uncharged)
		itemIdMap.put(11978, ItemCategory.TELEPORTS); // Glory(6)
		itemIdMap.put(11976, ItemCategory.TELEPORTS); // Glory(5)
		// Ring of dueling (1-8 charges)
		itemIdMap.put(2552, ItemCategory.TELEPORTS);  // Ring of dueling(8)
		itemIdMap.put(2554, ItemCategory.TELEPORTS);  // Ring of dueling(7)
		itemIdMap.put(2556, ItemCategory.TELEPORTS);  // Ring of dueling(6)
		itemIdMap.put(2558, ItemCategory.TELEPORTS);  // Ring of dueling(5)
		itemIdMap.put(2560, ItemCategory.TELEPORTS);  // Ring of dueling(4)
		itemIdMap.put(2562, ItemCategory.TELEPORTS);  // Ring of dueling(3)
		itemIdMap.put(2564, ItemCategory.TELEPORTS);  // Ring of dueling(2)
		itemIdMap.put(2566, ItemCategory.TELEPORTS);  // Ring of dueling(1)
		// Games necklace (1-8 charges)
		itemIdMap.put(3853, ItemCategory.TELEPORTS);  // Games necklace(8)
		itemIdMap.put(3855, ItemCategory.TELEPORTS);  // Games necklace(7)
		itemIdMap.put(3857, ItemCategory.TELEPORTS);  // Games necklace(6)
		itemIdMap.put(3859, ItemCategory.TELEPORTS);  // Games necklace(5)
		itemIdMap.put(3861, ItemCategory.TELEPORTS);  // Games necklace(4)
		itemIdMap.put(3863, ItemCategory.TELEPORTS);  // Games necklace(3)
		itemIdMap.put(3865, ItemCategory.TELEPORTS);  // Games necklace(2)
		itemIdMap.put(3867, ItemCategory.TELEPORTS);  // Games necklace(1)
		// Ring of wealth (1-5 charges)
		itemIdMap.put(11980, ItemCategory.TELEPORTS); // Ring of wealth(5)
		itemIdMap.put(11982, ItemCategory.TELEPORTS); // Ring of wealth(4)
		itemIdMap.put(11984, ItemCategory.TELEPORTS); // Ring of wealth(3)
		itemIdMap.put(11986, ItemCategory.TELEPORTS); // Ring of wealth(2)
		itemIdMap.put(11988, ItemCategory.TELEPORTS); // Ring of wealth(1)
		// Skills necklace (1-6 charges)
		itemIdMap.put(11105, ItemCategory.TELEPORTS); // Skills necklace(6)
		itemIdMap.put(11107, ItemCategory.TELEPORTS); // Skills necklace(5)
		itemIdMap.put(11109, ItemCategory.TELEPORTS); // Skills necklace(4)
		itemIdMap.put(11111, ItemCategory.TELEPORTS); // Skills necklace(3)
		itemIdMap.put(11113, ItemCategory.TELEPORTS); // Skills necklace(2)
		itemIdMap.put(11115, ItemCategory.TELEPORTS); // Skills necklace(1)
		// Combat bracelet (1-6 charges)
		itemIdMap.put(11118, ItemCategory.TELEPORTS); // Combat bracelet(6)
		itemIdMap.put(11120, ItemCategory.TELEPORTS); // Combat bracelet(5)
		itemIdMap.put(11122, ItemCategory.TELEPORTS); // Combat bracelet(4)
		itemIdMap.put(11124, ItemCategory.TELEPORTS); // Combat bracelet(3)
		itemIdMap.put(11126, ItemCategory.TELEPORTS); // Combat bracelet(2)
		itemIdMap.put(11128, ItemCategory.TELEPORTS); // Combat bracelet(1)
		// Necklace of passage (1-5 charges)
		itemIdMap.put(21146, ItemCategory.TELEPORTS); // Necklace of passage(5)
		itemIdMap.put(21149, ItemCategory.TELEPORTS); // Necklace of passage(4)
		itemIdMap.put(21151, ItemCategory.TELEPORTS); // Necklace of passage(3)
		itemIdMap.put(21153, ItemCategory.TELEPORTS); // Necklace of passage(2)
		itemIdMap.put(21155, ItemCategory.TELEPORTS); // Necklace of passage(1)
		// Burning amulet (1-5 charges)
		itemIdMap.put(21166, ItemCategory.TELEPORTS); // Burning amulet(5)
		itemIdMap.put(21169, ItemCategory.TELEPORTS); // Burning amulet(4)
		itemIdMap.put(21171, ItemCategory.TELEPORTS); // Burning amulet(3)
		itemIdMap.put(21173, ItemCategory.TELEPORTS); // Burning amulet(2)
		itemIdMap.put(21175, ItemCategory.TELEPORTS); // Burning amulet(1)
		// Digsite pendant (1-5 charges)
		itemIdMap.put(11190, ItemCategory.TELEPORTS); // Digsite pendant(5)
		itemIdMap.put(11191, ItemCategory.TELEPORTS); // Digsite pendant(4)
		itemIdMap.put(11192, ItemCategory.TELEPORTS); // Digsite pendant(3)
		itemIdMap.put(11193, ItemCategory.TELEPORTS); // Digsite pendant(2)
		itemIdMap.put(11194, ItemCategory.TELEPORTS); // Digsite pendant(1)

		// === GEAR ===
		itemIdMap.put(7462, ItemCategory.GEAR);  // Barrows gloves
		itemIdMap.put(6570, ItemCategory.GEAR);  // Fire cape
		itemIdMap.put(21295, ItemCategory.GEAR); // Infernal cape
		itemIdMap.put(10499, ItemCategory.GEAR); // Ava's accumulator
		itemIdMap.put(22109, ItemCategory.GEAR); // Ava's assembler
		// Mystic robes
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
		// Super combat (4-1 dose)
		itemIdMap.put(12695, ItemCategory.POTIONS);
		itemIdMap.put(12697, ItemCategory.POTIONS);
		itemIdMap.put(12699, ItemCategory.POTIONS);
		itemIdMap.put(12701, ItemCategory.POTIONS);
		// Prayer potions (4-1 dose)
		itemIdMap.put(2434, ItemCategory.POTIONS);
		itemIdMap.put(139, ItemCategory.POTIONS);
		itemIdMap.put(141, ItemCategory.POTIONS);
		itemIdMap.put(143, ItemCategory.POTIONS);
		// Saradomin brews (4-1 dose)
		itemIdMap.put(6685, ItemCategory.POTIONS);
		itemIdMap.put(6687, ItemCategory.POTIONS);
		itemIdMap.put(6689, ItemCategory.POTIONS);
		itemIdMap.put(6691, ItemCategory.POTIONS);

		// === FOOD ===
		itemIdMap.put(385, ItemCategory.FOOD);   // Shark
		itemIdMap.put(379, ItemCategory.FOOD);   // Lobster
		itemIdMap.put(373, ItemCategory.FOOD);   // Swordfish
		itemIdMap.put(7946, ItemCategory.FOOD);  // Monkfish
		itemIdMap.put(391, ItemCategory.FOOD);   // Manta ray
		itemIdMap.put(13441, ItemCategory.FOOD); // Anglerfish
		itemIdMap.put(11936, ItemCategory.FOOD); // Dark crab
		itemIdMap.put(3144, ItemCategory.FOOD);  // Karambwan

		// === TOOLS ===
		itemIdMap.put(1755, ItemCategory.TOOLS); // Chisel
		itemIdMap.put(2347, ItemCategory.TOOLS); // Hammer
		itemIdMap.put(590, ItemCategory.TOOLS);  // Tinderbox
		itemIdMap.put(946, ItemCategory.TOOLS);  // Knife
		itemIdMap.put(1735, ItemCategory.TOOLS); // Shears
		itemIdMap.put(952, ItemCategory.TOOLS);  // Spade

		// === RAW MATERIALS ===
		itemIdMap.put(1436, ItemCategory.RAW_MATERIALS); // Rune essence
		itemIdMap.put(7936, ItemCategory.RAW_MATERIALS); // Pure essence
		itemIdMap.put(314, ItemCategory.RAW_MATERIALS);  // Feather
		itemIdMap.put(526, ItemCategory.RAW_MATERIALS);  // Bones
	}

	public ItemCategory categorize(String itemName, int itemId)
	{
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

		// Priority 4: Catch-all
		return ItemCategory.QUEST_MISC;
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
