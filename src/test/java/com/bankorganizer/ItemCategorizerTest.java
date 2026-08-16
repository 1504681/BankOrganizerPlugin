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

	// --- Materials / High Alch / name rules from user feedback ---

	@Test
	public void testMaterialRules()
	{
		String[] materials = {"Uncut ruby", "Rune arrowtips", "Adamant dart tip", "Diamond bolt tips", "Barb bolttips",
			"Runite ore", "Raw shark", "Swamp paste", "Oak bird house", "Magic logs", "Bow string", "Flax",
			"Green dragonhide", "Hard leather", "Yew roots", "Mahogany plank", "Mithril nails", "Runite bolts (unf)",
			"Cotton yarn", "Lava scale shard", "Crab paste"};
		for (String n : materials)
		{
			assertEquals(n, ItemCategory.RAW_MATERIALS, categorizer.categorize(n, 99999));
		}
	}

	@Test
	public void testAdamantMithrilAreHighAlchExceptAmmoAndTools()
	{
		assertEquals(ItemCategory.HIGH_ALCH, categorizer.categorize("Adamant platebody", 99999));
		assertEquals(ItemCategory.HIGH_ALCH, categorizer.categorize("Mithril scimitar", 99999));
		assertEquals(ItemCategory.HIGH_ALCH, categorizer.categorize("Mithril kiteshield", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Adamant bolts", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Mithril arrow", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Adamant pickaxe", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Mithril axe", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Mithril ore", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Adamantite bar", 99999));
	}

	@Test
	public void testNameRules()
	{
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Lightbearer", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Karil's crossbow 0", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Dharok's platebody 100", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Amulet of the damned", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Gem sack", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Open gem tote", 99999));
		assertEquals(ItemCategory.FOOD, categorizer.categorize("Halibut", 99999));
	}

	@Test
	public void testBonesAreMaterials()
	{
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Bones", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Dragon bones", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Superior dragon bones", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Long bone", 99999));
	}

	private long mat(String name) { return categorizer.getMaterialFullSortKey(name, 99999); }

	@Test
	public void testMaterialsSortByTierWithinGroups()
	{
		// ores
		assertTrue(mat("Copper ore") < mat("Iron ore")); assertTrue(mat("Iron ore") < mat("Runite ore"));
		// gems, uncut first (typeOrder) and by tier
		assertTrue(mat("Uncut opal") < mat("Uncut sapphire")); assertTrue(mat("Uncut sapphire") < mat("Uncut zenyte"));
		// tips by metal, gem tips after metal tips
		assertTrue(mat("Bronze arrowtips") < mat("Rune arrowtips")); assertTrue(mat("Rune arrowtips") < mat("Diamond bolt tips"));
		// planks, logs
		assertTrue(mat("Plank") < mat("Oak plank")); assertTrue(mat("Oak plank") < mat("Mahogany plank"));
		assertTrue(mat("Logs") < mat("Yew logs")); assertTrue(mat("Yew logs") < mat("Redwood logs"));
		// raw food by cooking level
		assertTrue(mat("Raw shrimps") < mat("Raw lobster")); assertTrue(mat("Raw lobster") < mat("Raw shark"));
		// bones by prayer xp
		assertTrue(mat("Bones") < mat("Big bones")); assertTrue(mat("Big bones") < mat("Dragon bones"));
		assertTrue(mat("Dragon bones") < mat("Superior dragon bones"));
		// herbs: grimy before clean, by level
		assertTrue(mat("Grimy guam leaf") < mat("Grimy torstol")); assertTrue(mat("Grimy torstol") < mat("Guam leaf"));
		// hides
		assertTrue(mat("Green dragonhide") < mat("Black dragonhide"));
		// bird houses by wood
		assertTrue(mat("Bird house") < mat("Oak bird house")); assertTrue(mat("Oak bird house") < mat("Redwood bird house"));
	}

	@Test
	public void testMaterialGroupsUseSkillIndices()
	{
		assertEquals(4, categorizer.getMaterialGroupIndex("Runite ore", 99999));   // Mining
		assertEquals(2, categorizer.getMaterialGroupIndex("Magic logs", 99999));   // Woodcutting
		assertEquals(12, categorizer.getMaterialGroupIndex("Rune arrowtips", 99999)); // Fletching
		assertEquals(0, categorizer.getMaterialGroupIndex("Ranarr seed", 99999));  // Farming
		assertEquals(13, categorizer.getMaterialGroupIndex("Grimy ranarr weed", 99999)); // Herblore
		assertEquals(13, categorizer.getMaterialGroupIndex("Eye of newt", 99999)); // Herblore secondaries
		assertEquals(3, categorizer.getMaterialGroupIndex("Raw shark", 99999));    // Fishing
		assertEquals(5, categorizer.getMaterialGroupIndex("Dragon bones", 99999)); // Prayer
		assertEquals(15, categorizer.getMaterialGroupIndex("Yew bird house", 99999)); // Hunter
	}

	@Test
	public void testLatestNameRules()
	{
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Achievement diary cape", 99999));
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Achievement diary cape (t)", 99999));
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Ring of wealth (i)", 99999));
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Ring of wealth", 99999));
		assertNotEquals(ItemCategory.TELEPORTS, categorizer.categorize("Ring of wealth scroll", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Bottomless compost bucket", 99999));
		assertEquals(0, categorizer.getSkillGroupIndex("Bottomless compost bucket", 99999)); // Farming
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Drift net", 21652));
		assertEquals(15, categorizer.getSkillGroupIndex("Drift net", 99999)); // Hunter
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Diving apparatus", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Fishbowl helmet", 99999));
		assertEquals(0, categorizer.getSkillGroupIndex("Diving apparatus", 99999));
		assertEquals(0, categorizer.getSkillGroupIndex("Fishbowl helmet", 99999));
		// adjacent in the farming group
		long a = categorizer.getSkillingFullSortKey("Diving apparatus", 7535);
		long b = categorizer.getSkillingFullSortKey("Fishbowl helmet", 7534);
		assertTrue(a < b);
		assertEquals(a >> 16, (b >> 16) - 1);
	}

	@Test
	public void testProfileDefaultsVersionRoundTrip()
	{
		BankOrganizerProfile p = BankOrganizerProfile.createDefault();
		assertEquals(BankOrganizerPlugin.VERSION, p.getDefaultsVersion());
		BankOrganizerProfile back = BankOrganizerProfile.deserialize(p.serialize());
		assertEquals(BankOrganizerPlugin.VERSION, back.getDefaultsVersion());
		// legacy export without a VER line -> empty
		BankOrganizerProfile legacy = BankOrganizerProfile.deserialize("Old\nCAT:1:GEAR\nSUB:\n");
		assertEquals("", legacy.getDefaultsVersion());
		assertEquals(BankOrganizerProfile.DEFAULTS_NONE, BankOrganizerProfile.createBlank("x").getDefaultsVersion());
	}

	// --- Defaults re-application (upgrade) ---

	@Test
	public void testMergeDefaultsPreservesTrackedUserPicks()
	{
		Map<Integer, ItemCategory> shipped = new HashMap<>();
		shipped.put(1, ItemCategory.GEAR); shipped.put(2, ItemCategory.RAW_MATERIALS); shipped.put(3, ItemCategory.FOOD);
		Map<Integer, Integer> shippedSub = new HashMap<>(); shippedSub.put(2, 4);
		// user's current profile: 1 changed to HIGH_ALCH, 3 cleared, 9 added; sub for 2 changed to 0
		Map<Integer, ItemCategory> cur = new HashMap<>();
		cur.put(1, ItemCategory.HIGH_ALCH); cur.put(2, ItemCategory.RAW_MATERIALS); cur.put(9, ItemCategory.POTIONS);
		Map<Integer, Integer> curSub = new HashMap<>(); curSub.put(2, 0);
		java.util.Set<Integer> user = new java.util.HashSet<>(java.util.Arrays.asList(1, 3, 9, 2));

		ProfileManager.MergeResult m = ProfileManager.mergeDefaults(shipped, shippedSub, cur, curSub, user, false);
		assertEquals(ItemCategory.HIGH_ALCH, m.categories.get(1)); // user change kept
		assertEquals(ItemCategory.RAW_MATERIALS, m.categories.get(2)); // default
		assertNull(m.categories.get(3));                            // user cleared -> stays cleared
		assertEquals(ItemCategory.POTIONS, m.categories.get(9));    // user addition kept
		assertEquals(Integer.valueOf(0), m.subCategories.get(2));   // user subgroup kept
	}

	@Test
	public void testMergeDefaultsLegacyProfileKeepsDifferences()
	{
		Map<Integer, ItemCategory> shipped = new HashMap<>();
		shipped.put(1, ItemCategory.GEAR); shipped.put(2, ItemCategory.RAW_MATERIALS);
		Map<Integer, ItemCategory> cur = new HashMap<>();
		cur.put(1, ItemCategory.GEAR);        // same as shipped -> not a user pick
		cur.put(2, ItemCategory.SKILLING);    // differs -> user pick
		cur.put(5, ItemCategory.TELEPORTS);   // not shipped -> user pick
		ProfileManager.MergeResult m = ProfileManager.mergeDefaults(shipped, new HashMap<>(), cur, new HashMap<>(), new java.util.HashSet<>(), true);
		assertEquals(ItemCategory.GEAR, m.categories.get(1));
		assertEquals(ItemCategory.SKILLING, m.categories.get(2));
		assertEquals(ItemCategory.TELEPORTS, m.categories.get(5));
		assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(2, 5)), m.userItems);
	}

	@Test
	public void testMergeDefaultsAddsNewShippedEntries()
	{
		Map<Integer, ItemCategory> shipped = new HashMap<>();
		shipped.put(1, ItemCategory.GEAR); shipped.put(2, ItemCategory.RAW_MATERIALS);
		Map<Integer, ItemCategory> cur = new HashMap<>(); cur.put(1, ItemCategory.GEAR);
		ProfileManager.MergeResult m = ProfileManager.mergeDefaults(shipped, new HashMap<>(), cur, new HashMap<>(), new java.util.HashSet<>(), false);
		assertEquals(2, m.categories.size());
		assertEquals(ItemCategory.RAW_MATERIALS, m.categories.get(2));
	}

	@Test
	public void testProfileUserItemsRoundTrip()
	{
		BankOrganizerProfile p = BankOrganizerProfile.createDefault();
		p.setUserItems(java.util.Arrays.asList(4151, 995));
		BankOrganizerProfile back = BankOrganizerProfile.deserialize(p.serialize());
		assertEquals(new java.util.LinkedHashSet<>(java.util.Arrays.asList(4151, 995)), back.getUserItems());
		assertEquals(BankOrganizerProfile.DEFAULTS_NONE, BankOrganizerProfile.createBlank("b").getDefaultsVersion());
	}

	@Test
	public void testCluesGoToMainTab()
	{
		assertEquals(ItemCategory.CURRENCY, categorizer.categorize("Clue scroll (hard)", 99999));
		assertEquals(ItemCategory.CURRENCY, categorizer.categorize("Scroll box (elite)", 99999));
		assertEquals(ItemCategory.CURRENCY, categorizer.categorize("Reward casket (master)", 99999));
		assertEquals(ItemCategory.CURRENCY, categorizer.categorize("Clue bottle (medium)", 99999));
		assertEquals(ItemCategory.CURRENCY, categorizer.categorize("Coins", 995));
	}

	@Test
	public void testGemsOrbsCompostAndUnenchantedGemBolts()
	{
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Onyx bolts", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Ruby bolts", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Onyx bolts (e)", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Ruby dragon bolts (e)", 99999));
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Runite bolts", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Compost", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Ultracompost", 99999));
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Bottomless compost bucket", 99999));
		assertEquals(ItemCategory.POTIONS, categorizer.categorize("Compost potion(4)", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Ruby", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Zenyte", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Unpowered orb", 99999));
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Water orb", 99999));
	}
}
