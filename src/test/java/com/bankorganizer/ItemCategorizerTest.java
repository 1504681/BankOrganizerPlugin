package com.bankorganizer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import net.runelite.client.game.ItemEquipmentStats;

public class ItemCategorizerTest
{
	private ItemCategorizer categorizer;

	@Before
	public void setUp()
	{
		categorizer = new ItemCategorizer();
	}

	@Test
	public void testHardcodedIdTakesPriority()
	{
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Law rune", 563));
	}

	@Test
	public void testKeywordMatchPotion()
	{
		assertEquals(ItemCategory.POTIONS, categorizer.categorize("Super combat potion(4)", 99999));
	}

	@Test
	public void testKeywordMatchGear()
	{
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Abyssal whip", 99999));
	}

	@Test
	public void testKeywordMatchFood()
	{
		assertEquals(ItemCategory.FOOD, categorizer.categorize("Shark", 99999));
	}

	@Test
	public void testKeywordMatchTools()
	{
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Rune pickaxe", 99999));
	}

	@Test
	public void testKeywordMatchRawMaterials()
	{
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Mithril ore", 99999));
	}

	@Test
	public void testUnknownItemFallsToQuestMisc()
	{
		assertEquals(ItemCategory.QUEST_MISC, categorizer.categorize("Strange widget", 99999));
	}

	@Test
	public void testRegexPattern()
	{
		Map<ItemCategory, String> regexPatterns = new HashMap<>();
		regexPatterns.put(ItemCategory.GEAR, "dragonfire.*ward");
		categorizer.setRegexPatterns(regexPatterns);

		assertEquals(ItemCategory.GEAR, categorizer.categorize("Dragonfire ward", 99999));
	}

	@Test
	public void testPickaxeMatchesTools()
	{
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Bronze pickaxe", 99999));
	}

	@Test
	public void testTeleportTablet()
	{
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Varrock teleport", 99999));
	}

	// --- Skilling outfits win over generic gear keywords ---

	@Test
	public void testSkillingOutfitBeatsGearKeyword()
	{
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Rogue gloves", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Angler boots", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Prospector helmet", 99999));
		// but "angler" alone must not steal food
		assertEquals(ItemCategory.FOOD, categorizer.categorize("Anglerfish", 99999));
	}

	// --- Equipment stats provider ---

	private static ItemEquipmentStats bodyArmour()
	{
		return ItemEquipmentStats.builder().slot(4).dstab(100).dslash(100).dcrush(100).build();
	}

	@Test
	public void testStatsFallbackMakesUnknownEquipableGear()
	{
		Map<Integer, ItemEquipmentStats> stats = new HashMap<>();
		stats.put(1000, bodyArmour());
		categorizer.setStatsProvider(stats::get);

		assertEquals(ItemCategory.GEAR, categorizer.categorize("Bandos chestplate", 1000));
		// zero-stat cosmetics stay Misc
		stats.put(1001, ItemEquipmentStats.builder().slot(0).build());
		assertEquals(ItemCategory.QUEST_MISC, categorizer.categorize("Bunny ears", 1001));
	}

	@Test
	public void testNonEquipableItemsSkipGearKeywords()
	{
		Map<Integer, ItemEquipmentStats> stats = new HashMap<>();
		categorizer.setStatsProvider(stats::get);

		// "bolt" / "ring" keywords are false positives for these — they are not equipable
		assertEquals(ItemCategory.QUEST_MISC, categorizer.categorize("Bolt of linen", 1002));
		assertEquals(ItemCategory.QUEST_MISC, categorizer.categorize("Ring mould", 1003));
		// real gear with a keyword AND stats still works
		stats.put(1004, ItemEquipmentStats.builder().slot(3).aslash(50).build());
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Rune scimitar", 1004));
	}

	@Test
	public void testStatsNotReadyFallsBackToKeywords()
	{
		Map<Integer, ItemEquipmentStats> stats = new HashMap<>();
		categorizer.setStatsProvider(stats::get, () -> false);

		// stats not loaded: behave exactly like the keyword-only categorizer
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Rune platebody", 1005));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Bolt of linen", 1006));
	}

	@Test
	public void testManualOverrideBeatsStats()
	{
		Map<Integer, ItemEquipmentStats> stats = new HashMap<>();
		stats.put(1007, bodyArmour());
		categorizer.setStatsProvider(stats::get);
		categorizer.setManualOverride(1007, ItemCategory.HIGH_ALCH);
		assertEquals(ItemCategory.HIGH_ALCH, categorizer.categorize("Rune platebody", 1007));
	}

	// --- Farming / Herblore materials ---

	@Test
	public void testSeedsHerbsAndSecondariesAreMaterials()
	{
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Potato seed", 99999));      // was Food ("potato")
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Hammerstone seed", 99999)); // was Skilling ("hammer")
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Mushroom spore", 99999));   // was Food
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Ranarr weed", 99999));      // was Misc
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Grimy ranarr weed", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Eye of newt", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Magic sapling", 99999));
		// but seed containers/teleports are untouched
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Seed box", 99999));
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Royal seed pod", 99999));
	}

	// --- Teleport sort (issue #1) ---

	private long teleKey(String name, int id, TeleportSortMode mode)
	{
		return categorizer.getTeleportFullSortKey(name, id, mode);
	}

	@Test
	public void testRunesFirstReallyPutsRunesFirst()
	{
		TeleportSortMode mode = TeleportSortMode.RUNES_FIRST;
		long pouch = teleKey("Rune pouch", 12791, mode);
		long law = teleKey("Law rune", 563, mode);
		long glory = teleKey("Amulet of glory(4)", 1712, mode);
		long tab = teleKey("Varrock teleport", 8007, mode);
		long cape = teleKey("Construct. cape(t)", 9790, mode);
		long ecto = teleKey("Ectophial", 4251, mode);
		long random = teleKey("Some new teleport thing", 55555, mode);

		assertTrue(pouch < law);
		assertTrue(law < glory);
		assertTrue(glory < tab);
		assertTrue(tab < cape);
		assertTrue(cape < ecto);
		// anything the categorizer can't place must never sort above the runes
		assertTrue(law < random);
		assertTrue(tab < random);
	}

	@Test
	public void testJewelryFirstMode()
	{
		TeleportSortMode mode = TeleportSortMode.JEWELRY_FIRST;
		assertTrue(teleKey("Amulet of glory(4)", 1712, mode) < teleKey("Law rune", 563, mode));
		assertTrue(teleKey("Law rune", 563, mode) < teleKey("Varrock teleport", 8007, mode));
		assertTrue(teleKey("Varrock teleport", 8007, mode) < teleKey("Ectophial", 4251, mode));
	}

	@Test
	public void testRuneOrderWithinRunes()
	{
		TeleportSortMode mode = TeleportSortMode.RUNES_FIRST;
		assertTrue(teleKey("Air rune", 556, mode) < teleKey("Fire rune", 554, mode));
		assertTrue(teleKey("Fire rune", 554, mode) < teleKey("Law rune", 563, mode));
	}

	// --- Shipped defaults must actually load (regression guard for the "\\:" bug) ---

	private static String readResource(String name) throws Exception
	{
		try (java.io.InputStream is = ItemCategorizerTest.class.getResourceAsStream("/com/bankorganizer/" + name))
		{
			assertNotNull("missing resource " + name, is);
			return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).trim();
		}
	}

	@Test
	public void testShippedDefaultOverridesParse() throws Exception
	{
		String cat = readResource("default_overrides.txt");
		Map<Integer, ItemCategory> parsed = ItemCategorizer.parseCategoryOverrides(cat);
		int entries = cat.split(",").length;
		assertTrue("expected hundreds of default overrides, got " + parsed.size(), parsed.size() > 300);
		assertEquals("every entry in default_overrides.txt must parse", entries, parsed.size());

		String sub = readResource("default_sub_overrides.txt");
		Map<Integer, Integer> parsedSub = ItemCategorizer.parseSubCategoryOverrides(sub);
		assertEquals("every entry in default_sub_overrides.txt must parse", sub.split(",").length, parsedSub.size());
		for (int idx : parsedSub.values())
		{
			assertTrue("skill index out of range: " + idx, idx >= 0 && idx < ItemCategorizer.SKILL_NAMES.length);
		}
	}

	@Test
	public void testParserToleratesEscapedColons()
	{
		Map<Integer, ItemCategory> m = ItemCategorizer.parseCategoryOverrides("2\\:GEAR,7178\\:FOOD, 42:RAW_MATERIALS,bad,9:NOPE");
		assertEquals(3, m.size());
		assertEquals(ItemCategory.GEAR, m.get(2));
		assertEquals(ItemCategory.FOOD, m.get(7178));
		assertEquals(ItemCategory.RAW_MATERIALS, m.get(42));
	}
}
