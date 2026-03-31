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

		// === SKILLING ===
		itemIdMap.put(1755, ItemCategory.SKILLING); // Chisel
		itemIdMap.put(2347, ItemCategory.SKILLING); // Hammer
		itemIdMap.put(590, ItemCategory.SKILLING);  // Tinderbox
		itemIdMap.put(946, ItemCategory.SKILLING);  // Knife
		itemIdMap.put(1735, ItemCategory.SKILLING); // Shears
		itemIdMap.put(952, ItemCategory.SKILLING);  // Spade
		itemIdMap.put(25582, ItemCategory.SKILLING); // Fish barrel
		itemIdMap.put(13226, ItemCategory.SKILLING); // Herb sack
		itemIdMap.put(12020, ItemCategory.SKILLING); // Gem bag
		itemIdMap.put(12019, ItemCategory.SKILLING); // Coal bag
		itemIdMap.put(12013, ItemCategory.SKILLING); // Plank sack
		itemIdMap.put(22994, ItemCategory.SKILLING); // Seed box
		itemIdMap.put(28786, ItemCategory.SKILLING); // Log basket
		itemIdMap.put(28788, ItemCategory.SKILLING); // Forestry kit
		itemIdMap.put(11850, ItemCategory.SKILLING); // Graceful hood
		itemIdMap.put(11852, ItemCategory.SKILLING); // Graceful top
		itemIdMap.put(11854, ItemCategory.SKILLING); // Graceful legs
		itemIdMap.put(11856, ItemCategory.SKILLING); // Graceful gloves
		itemIdMap.put(11858, ItemCategory.SKILLING); // Graceful boots
		itemIdMap.put(11860, ItemCategory.SKILLING); // Graceful cape

		// === MATERIALS ===
		itemIdMap.put(1436, ItemCategory.RAW_MATERIALS);  // Rune essence
		itemIdMap.put(7936, ItemCategory.RAW_MATERIALS);  // Pure essence
		itemIdMap.put(314, ItemCategory.RAW_MATERIALS);   // Feather
		itemIdMap.put(526, ItemCategory.RAW_MATERIALS);   // Bones
		itemIdMap.put(28931, ItemCategory.RAW_MATERIALS); // Moonlight grub
		itemIdMap.put(11115, ItemCategory.RAW_MATERIALS); // Mort myre fungus
		itemIdMap.put(13439, ItemCategory.RAW_MATERIALS); // Volcanic ash
		itemIdMap.put(26792, ItemCategory.RAW_MATERIALS); // Numulite
		itemIdMap.put(21930, ItemCategory.RAW_MATERIALS); // Amylase crystal
		itemIdMap.put(28599, ItemCategory.RAW_MATERIALS); // Calcified deposit
		itemIdMap.put(23490, ItemCategory.RAW_MATERIALS); // Daeyalt essence
		itemIdMap.put(5075, ItemCategory.RAW_MATERIALS);  // Bird nest (seeds)
		itemIdMap.put(11232, ItemCategory.RAW_MATERIALS); // Justi faceguard
		itemIdMap.put(31475, ItemCategory.RAW_MATERIALS); // Blue dragonhide
		itemIdMap.put(13391, ItemCategory.RAW_MATERIALS); // Drift net

		// === HIGH ALCH ===
		itemIdMap.put(1393, ItemCategory.HIGH_ALCH);  // Iron warhammer
		itemIdMap.put(1071, ItemCategory.HIGH_ALCH);  // Green d'hide chaps
		itemIdMap.put(1085, ItemCategory.HIGH_ALCH);  // Blue d'hide body
		itemIdMap.put(1371, ItemCategory.HIGH_ALCH);  // Iron battleaxe
		itemIdMap.put(9342, ItemCategory.HIGH_ALCH);  // Onyx bolts (e)
		itemIdMap.put(1428, ItemCategory.HIGH_ALCH);  // Bronze halberd
		itemIdMap.put(1432, ItemCategory.HIGH_ALCH);  // Iron halberd
		itemIdMap.put(22263, ItemCategory.HIGH_ALCH); // Toktz-xil-ak

		// === QUEST/MISC ===
		itemIdMap.put(4129, ItemCategory.QUEST_MISC);  // Hazeel's mark
		itemIdMap.put(11822, ItemCategory.QUEST_MISC); // Armadyl helmet
		itemIdMap.put(12851, ItemCategory.QUEST_MISC); // Dragon defender
		itemIdMap.put(2611, ItemCategory.QUEST_MISC);  // Rune platebody (Saradomin)
		itemIdMap.put(11061, ItemCategory.QUEST_MISC); // Monkey greegree
		itemIdMap.put(26421, ItemCategory.QUEST_MISC); // Thread of Elidinis
		itemIdMap.put(4153, ItemCategory.QUEST_MISC);  // Granite maul
		itemIdMap.put(1099, ItemCategory.QUEST_MISC);  // Red d'hide chaps
		itemIdMap.put(829, ItemCategory.QUEST_MISC);   // Staff of fire
		itemIdMap.put(13120, ItemCategory.QUEST_MISC); // Necklace of passage
		itemIdMap.put(1109, ItemCategory.QUEST_MISC);  // Ghostly robe top
		itemIdMap.put(855, ItemCategory.QUEST_MISC);   // Staff
		itemIdMap.put(859, ItemCategory.QUEST_MISC);   // Magic staff
		itemIdMap.put(4446, ItemCategory.QUEST_MISC);  // Dwarven rock cake
		itemIdMap.put(26227, ItemCategory.QUEST_MISC); // Soulbane item
		itemIdMap.put(1925, ItemCategory.QUEST_MISC);  // Bucket
		itemIdMap.put(26528, ItemCategory.QUEST_MISC); // Quest item
		itemIdMap.put(3753, ItemCategory.QUEST_MISC);  // Flail of Ivandis
		itemIdMap.put(3755, ItemCategory.QUEST_MISC);  // Rod of Ivandis
		itemIdMap.put(7342, ItemCategory.QUEST_MISC);  // Quest item
		itemIdMap.put(7348, ItemCategory.QUEST_MISC);  // Quest item
		itemIdMap.put(9142, ItemCategory.QUEST_MISC);  // Broad bolts
		itemIdMap.put(2487, ItemCategory.QUEST_MISC);  // Quest item
		itemIdMap.put(6328, ItemCategory.QUEST_MISC);  // Initiate hauberk
		itemIdMap.put(11707, ItemCategory.QUEST_MISC); // Granite body
		itemIdMap.put(1245, ItemCategory.QUEST_MISC);  // Berserker ring
		itemIdMap.put(1247, ItemCategory.QUEST_MISC);  // Warrior ring
		itemIdMap.put(12785, ItemCategory.QUEST_MISC); // Dark bow
		itemIdMap.put(4081, ItemCategory.QUEST_MISC);  // Quest item
		itemIdMap.put(22260, ItemCategory.QUEST_MISC); // Quest item
		itemIdMap.put(7158, ItemCategory.QUEST_MISC);  // Quest item

		// === COMBAT (user-contributed) ===
		itemIdMap.put(11, ItemCategory.GEAR);      // Iron dagger
		itemIdMap.put(28947, ItemCategory.GEAR);   // Eclipse atlatl
		itemIdMap.put(12610, ItemCategory.GEAR);   // Black chinchompa
		itemIdMap.put(29283, ItemCategory.GEAR);   // Dual macuahuitl
		itemIdMap.put(868, ItemCategory.GEAR);     // Rune crossbow
		itemIdMap.put(28260, ItemCategory.GEAR);   // Tonalztics of ralos
		itemIdMap.put(29031, ItemCategory.GEAR);   // Eclipse moon helm
		itemIdMap.put(29033, ItemCategory.GEAR);   // Eclipse moon chestplate
		itemIdMap.put(29037, ItemCategory.GEAR);   // Eclipse moon tassets
		itemIdMap.put(7535, ItemCategory.GEAR);    // New crystal shield
		itemIdMap.put(29043, ItemCategory.GEAR);   // Blue moon helm
		itemIdMap.put(29045, ItemCategory.GEAR);   // Blue moon chestplate
		itemIdMap.put(25981, ItemCategory.GEAR);   // Masori body (f)
		itemIdMap.put(11902, ItemCategory.GEAR);   // Godsword blade
		itemIdMap.put(29594, ItemCategory.GEAR);   // Blood moon helm
		itemIdMap.put(24223, ItemCategory.GEAR);   // Ghrazi rapier
		itemIdMap.put(28329, ItemCategory.GEAR);   // Bone shortbow
		itemIdMap.put(30891, ItemCategory.GEAR);   // Soulreaper axe
		itemIdMap.put(12006, ItemCategory.GEAR);   // Abyssal tentacle

		// === TELEPORTS (user-contributed) ===
		itemIdMap.put(28929, ItemCategory.TELEPORTS); // Quetzal whistle
		itemIdMap.put(2572, ItemCategory.TELEPORTS);  // Ring of wealth
		itemIdMap.put(13068, ItemCategory.TELEPORTS); // Xeric's talisman
		itemIdMap.put(13069, ItemCategory.TELEPORTS); // Xeric's talisman (inert)
		itemIdMap.put(9763, ItemCategory.TELEPORTS);  // Ectophial
		itemIdMap.put(13103, ItemCategory.TELEPORTS); // Digsite pendant
		itemIdMap.put(6707, ItemCategory.TELEPORTS);  // Camulet
		itemIdMap.put(9781, ItemCategory.TELEPORTS);  // Enchanted lyre
		itemIdMap.put(13111, ItemCategory.TELEPORTS); // Slayer ring (8)
		itemIdMap.put(13115, ItemCategory.TELEPORTS); // Slayer ring (4)
		itemIdMap.put(9790, ItemCategory.TELEPORTS);  // Skull sceptre
		itemIdMap.put(13124, ItemCategory.TELEPORTS); // Burning amulet
		itemIdMap.put(25930, ItemCategory.TELEPORTS); // Amulet of the eye
		itemIdMap.put(13132, ItemCategory.TELEPORTS); // Ring of returning
		itemIdMap.put(25932, ItemCategory.TELEPORTS); // Amulet of the eye (charged)
		itemIdMap.put(13136, ItemCategory.TELEPORTS); // Skills necklace
		itemIdMap.put(33104, ItemCategory.TELEPORTS); // Sanguine portal nexus
		itemIdMap.put(9811, ItemCategory.TELEPORTS);  // Pharaoh's sceptre
		itemIdMap.put(13140, ItemCategory.TELEPORTS); // Combat bracelet
		itemIdMap.put(4695, ItemCategory.TELEPORTS);  // Games necklace(8)
		itemIdMap.put(4696, ItemCategory.TELEPORTS);  // Games necklace(7)
		itemIdMap.put(4697, ItemCategory.TELEPORTS);  // Games necklace(6)
		itemIdMap.put(4698, ItemCategory.TELEPORTS);  // Games necklace(5)
		itemIdMap.put(4699, ItemCategory.TELEPORTS);  // Games necklace(4)
		itemIdMap.put(13660, ItemCategory.TELEPORTS); // Chronicle
		itemIdMap.put(11872, ItemCategory.TELEPORTS); // Grand seed pod
		itemIdMap.put(11873, ItemCategory.TELEPORTS); // Royal seed pod
		itemIdMap.put(22114, ItemCategory.TELEPORTS); // Drakan's medallion
		itemIdMap.put(19564, ItemCategory.TELEPORTS); // Pharaoh's sceptre (uncharged)
		itemIdMap.put(26990, ItemCategory.TELEPORTS); // Ring of shadows
		itemIdMap.put(22400, ItemCategory.TELEPORTS); // Xeric's talisman (charged)
		itemIdMap.put(24709, ItemCategory.TELEPORTS); // Kharedst's memoirs
		itemIdMap.put(21389, ItemCategory.TELEPORTS); // Teleport anchoring scroll
		itemIdMap.put(32399, ItemCategory.TELEPORTS); // Pendant of Ates
		itemIdMap.put(4251, ItemCategory.TELEPORTS);  // Elf teleport crystal
		itemIdMap.put(22947, ItemCategory.TELEPORTS); // Stony basalt
		itemIdMap.put(28327, ItemCategory.TELEPORTS); // Bone mace (teleport)
		itemIdMap.put(30638, ItemCategory.TELEPORTS); // Lunar seal
		itemIdMap.put(26818, ItemCategory.TELEPORTS); // Mask of Ranul
		itemIdMap.put(29893, ItemCategory.TELEPORTS); // Quetzal whistle (enhanced)
		itemIdMap.put(25818, ItemCategory.TELEPORTS); // Pendant of passage
		itemIdMap.put(11061, ItemCategory.TELEPORTS); // (moved from QUEST_MISC)

		// === SKILLING (user-contributed) ===
		itemIdMap.put(31043, ItemCategory.SKILLING);  // Forestry item
		itemIdMap.put(31052, ItemCategory.SKILLING);  // Forestry item
		itemIdMap.put(13646, ItemCategory.SKILLING);  // Raw anglerfish
		itemIdMap.put(10933, ItemCategory.SKILLING);  // Barronite deposit
		itemIdMap.put(10941, ItemCategory.SKILLING);  // Barronite guard
		itemIdMap.put(5339, ItemCategory.SKILLING);   // Seed dibber
		itemIdMap.put(26848, ItemCategory.SKILLING);  // Giant pouch
		itemIdMap.put(26856, ItemCategory.SKILLING);  // Colossal pouch
		itemIdMap.put(26858, ItemCategory.SKILLING);  // Colossal pouch (degraded)
		itemIdMap.put(25598, ItemCategory.SKILLING);  // Celestial ring

		// === POTIONS (user-contributed) ===
		itemIdMap.put(21163, ItemCategory.POTIONS); // Battlemage potion
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
	 * Determine the gear sub-category using equipment stats.
	 * Falls back to keyword matching if stats unavailable.
	 */
	public GearSubCategory getGearSubCategory(String itemName, int itemId,
		net.runelite.http.api.item.ItemEquipmentStats stats)
	{
		// Check ID map first
		GearSubCategory idSub = gearSubIdMap.get(itemId);
		if (idSub != null)
		{
			return idSub;
		}

		// Use equipment stats if available
		if (stats != null)
		{
			return classifyByStats(stats);
		}

		// Fallback to keywords
		return classifyByKeywords(itemName);
	}

	/**
	 * Overload without stats for backwards compatibility.
	 */
	public GearSubCategory getGearSubCategory(String itemName, int itemId)
	{
		return getGearSubCategory(itemName, itemId, null);
	}

	private GearSubCategory classifyByStats(net.runelite.http.api.item.ItemEquipmentStats stats)
	{
		int meleeAttack = Math.max(stats.getAstab(), Math.max(stats.getAslash(), stats.getAcrush()));
		int rangedAttack = stats.getArange();
		int magicAttack = stats.getAmagic();
		int meleeStr = stats.getStr();
		int rangedStr = stats.getRstr();
		int magicDmg = stats.getMdmg();
		int slot = stats.getSlot();

		// Weapon slots: 3 (weapon), also check two-handed
		boolean isWeapon = (slot == 3) || stats.isTwoHanded();

		// Magic damage % > 0 is always mage
		if (magicDmg > 0)
		{
			return isWeapon ? GearSubCategory.MAGE_WEAPON : GearSubCategory.MAGE_ARMOR;
		}

		// Determine dominant style by attack bonuses
		if (isWeapon)
		{
			if (rangedAttack > meleeAttack && rangedAttack > magicAttack)
			{
				return GearSubCategory.RANGED_WEAPON;
			}
			if (magicAttack > meleeAttack && magicAttack > rangedAttack)
			{
				return GearSubCategory.MAGE_WEAPON;
			}
			if (meleeAttack > 0 || meleeStr > 0)
			{
				return GearSubCategory.MELEE_WEAPON;
			}
			// No clear attack bonus — check strength
			if (rangedStr > 0) return GearSubCategory.RANGED_WEAPON;
			return GearSubCategory.MELEE_WEAPON;
		}
		else
		{
			// Armor: check offensive bonuses to determine style affinity
			if (rangedAttack > 0 && rangedAttack > meleeAttack && rangedAttack > magicAttack)
			{
				return GearSubCategory.RANGED_ARMOR;
			}
			if (magicAttack > 0 && magicAttack > meleeAttack && magicAttack > rangedAttack)
			{
				return GearSubCategory.MAGE_ARMOR;
			}
			if (rangedStr > 0 && rangedStr > meleeStr)
			{
				return GearSubCategory.RANGED_ARMOR;
			}
			if (meleeStr > 0)
			{
				return GearSubCategory.MELEE_ARMOR;
			}

			// Check defensive bonuses as tiebreaker
			int meleeDef = Math.max(stats.getDstab(), Math.max(stats.getDslash(), stats.getDcrush()));
			int rangedDef = stats.getDrange();
			int magicDef = stats.getDmagic();

			// If magic def is negative (typical of melee armor like platebody)
			if (magicDef < 0 && meleeDef > 0)
			{
				return GearSubCategory.MELEE_ARMOR;
			}
			// High magic def with low melee def = mage armor
			if (magicDef > meleeDef && rangedDef < meleeDef)
			{
				return GearSubCategory.MAGE_ARMOR;
			}
			// Ranged armor typically has negative melee def
			if (meleeDef < 0 && rangedDef > 0)
			{
				return GearSubCategory.RANGED_ARMOR;
			}

			return GearSubCategory.GENERAL;
		}
	}

	private GearSubCategory classifyByKeywords(String itemName)
	{
		String lowerName = itemName.toLowerCase();

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
	// Equipment slot sort order: weapon, head, amulet, cape, body, legs, shield, gloves, boots, ring, ammo
	private static final int[] SLOT_ORDER = new int[14];
	static
	{
		SLOT_ORDER[3] = 0;   // Weapon
		SLOT_ORDER[0] = 1;   // Head
		SLOT_ORDER[2] = 2;   // Amulet/Neck
		SLOT_ORDER[1] = 3;   // Cape
		SLOT_ORDER[4] = 4;   // Body
		SLOT_ORDER[7] = 5;   // Legs
		SLOT_ORDER[5] = 6;   // Shield
		SLOT_ORDER[9] = 7;   // Gloves
		SLOT_ORDER[10] = 8;  // Boots
		SLOT_ORDER[12] = 9;  // Ring
		SLOT_ORDER[13] = 10; // Ammo
	}

	/**
	 * Get a full sort key for a gear item: sub-category, then slot, then stat strength.
	 * Returns a long where higher bits = sub-category, middle bits = slot, lower bits = inverted stat.
	 * Lower values sort first.
	 */
	public long getGearFullSortKey(String itemName, int itemId,
		net.runelite.http.api.item.ItemEquipmentStats stats, GearSortMode mode)
	{
		GearSubCategory sub = getGearSubCategory(itemName, itemId, stats);
		int subOrder = getGearSortOrder(sub, mode);

		int slotOrder = 0;
		int statValue = 0;

		if (stats != null)
		{
			int slot = stats.getSlot();
			if (slot >= 0 && slot < SLOT_ORDER.length)
			{
				slotOrder = SLOT_ORDER[slot];
			}

			// Get the relevant stat for ordering within slot (highest = first)
			switch (sub)
			{
				case MELEE_WEAPON:
				case MELEE_ARMOR:
					// Melee: strength first, then best accuracy as tiebreaker
					statValue = stats.getStr() * 1000
						+ Math.max(stats.getAstab(), Math.max(stats.getAslash(), stats.getAcrush()));
					break;
				case RANGED_WEAPON:
				case RANGED_ARMOR:
					// Ranged: ranged strength first, then ranged accuracy
					statValue = stats.getRstr() * 1000 + stats.getArange();
					break;
				case MAGE_WEAPON:
				case MAGE_ARMOR:
					// Mage: magic damage first, then magic accuracy
					statValue = stats.getMdmg() * 1000 + stats.getAmagic();
					break;
				default:
					statValue = 0;
			}
		}

		// Invert stat so higher stats sort first (lower key value = sorted first)
		long invertedStat = 999999 - statValue;
		if (invertedStat < 0) invertedStat = 0;

		// Pack into long with enough bits: subOrder (8 bits) | slotOrder (8 bits) | invertedStat (20 bits)
		return ((long) subOrder << 28) | ((long) slotOrder << 20) | (invertedStat & 0xFFFFF);
	}

	public int getGearSortOrder(GearSubCategory sub, GearSortMode mode)
	{
		if (mode == GearSortMode.COMBAT_STYLE)
		{
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
