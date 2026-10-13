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
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Ring mould", 1003)); // crafting tool, never gear
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
		long divine = teleKey("Divine rune pouch", 27281, mode);
		long law = teleKey("Law rune", 563, mode);
		long glory = teleKey("Amulet of glory(4)", 1712, mode);
		long tab = teleKey("Varrock teleport", 8007, mode);
		long cape = teleKey("Construct. cape(t)", 9790, mode);
		long ecto = teleKey("Ectophial", 4251, mode);
		long random = teleKey("Some new teleport thing", 55555, mode);

		// rune pouches lead the runes group, then runes, then jewelry, tablets, teleport items
		assertTrue(pouch < law); assertTrue(divine < law);
		assertTrue(law < glory);
		assertTrue(glory < tab);
		assertTrue(tab < cape);
		assertTrue(cape < ecto);
		assertTrue(law < random);
		// rune pouch note is not a pouch
		assertTrue(teleKey("Rune pouch note", 55556, mode) > law);
	}

	@Test
	public void testJewelryFirstMode()
	{
		TeleportSortMode mode = TeleportSortMode.JEWELRY_FIRST;
		assertTrue(teleKey("Amulet of glory(4)", 1712, mode) < teleKey("Rune pouch", 12791, mode));
		assertTrue(teleKey("Rune pouch", 12791, mode) < teleKey("Law rune", 563, mode));
		assertTrue(teleKey("Law rune", 563, mode) < teleKey("Varrock teleport", 8007, mode));
		assertTrue(teleKey("Varrock teleport", 8007, mode) < teleKey("Ectophial", 4251, mode));
	}

	@Test
	public void testTeleportItemsFirstAndCustomOrder()
	{
		TeleportSortMode mode = TeleportSortMode.TELEPORT_ITEMS_FIRST;
		assertTrue(teleKey("Ectophial", 4251, mode) < teleKey("Law rune", 563, mode));
		assertTrue(teleKey("Construct. cape(t)", 9790, mode) < teleKey("Ectophial", 4251, mode));
		assertTrue(teleKey("Law rune", 563, mode) < teleKey("Amulet of glory(4)", 1712, mode));

		categorizer.setCustomTeleportOrder(TeleportSubCategory.TABLETS, TeleportSubCategory.OTHER);
		TeleportSortMode c = TeleportSortMode.CUSTOM;
		assertTrue(teleKey("Varrock teleport", 8007, c) < teleKey("Ectophial", 4251, c));
		assertTrue(teleKey("Ectophial", 4251, c) < teleKey("Law rune", 563, c));   // missing groups appended: runes, jewelry
		assertTrue(teleKey("Law rune", 563, c) < teleKey("Amulet of glory(4)", 1712, c));
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
		// gems: every uncut gem before every cut gem, each block by tier, amethyst and crushed gem last
		assertTrue(mat("Uncut opal") < mat("Uncut sapphire")); assertTrue(mat("Uncut sapphire") < mat("Uncut zenyte"));
		assertTrue(mat("Uncut zenyte") < mat("Opal")); assertTrue(mat("Opal") < mat("Zenyte"));
		assertTrue(mat("Zenyte") < mat("Amethyst")); assertTrue(mat("Amethyst") < mat("Crushed gem"));
		assertTrue(mat("Zenyte") < mat("Bronze nails")); assertTrue(mat("Runite bar") < mat("Uncut opal"));
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

	// --- Issue #5: the planner's "has a subcategory" check must agree with the key it sorts by ---

	@Test
	public void testGroupIndexMatchesSortKeyWithOverrides()
	{
		// a Materials item (gem) in a Skilling tab: key says group 99 (not Skilling), index must say the same
		categorizer.setSubCategoryOverride(1623, 4); // Uncut sapphire -> Mining
		assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Uncut sapphire", 1623));
		assertEquals(99, categorizer.getSkillGroupIndex("Uncut sapphire", 1623));
		assertEquals(categorizer.getSkillingFullSortKey("Uncut sapphire", 1623) >> 28, categorizer.getSkillGroupIndex("Uncut sapphire", 1623));
		// in the Materials tab the override and the rule both say Mining
		assertEquals(4, categorizer.getMaterialGroupIndex("Uncut sapphire", 1623));
		assertEquals(4, categorizer.getMaterialGroupIndex("Uncut sapphire", 99999));
		// an override on a Skilling item is honoured by both
		categorizer.setSubCategoryOverride(99998, 3); // some tool -> Fishing
		assertEquals(ItemCategory.SKILLING, categorizer.categorize("Rune pickaxe", 99998));
		assertEquals(3, categorizer.getSkillGroupIndex("Rune pickaxe", 99998));
		assertEquals(3, categorizer.getSkillingFullSortKey("Rune pickaxe", 99998) >> 28);
		// tabs mapped to an unordered category are reported, not silently "complete"
		assertTrue(BankOrganizerPlugin.hasSortRules(ItemCategory.GEAR));
		assertTrue(BankOrganizerPlugin.hasSortRules(ItemCategory.POTIONS));
		assertFalse(BankOrganizerPlugin.hasSortRules(ItemCategory.QUEST_MISC));
		assertFalse(BankOrganizerPlugin.hasSortRules(ItemCategory.CURRENCY));
		assertFalse(BankOrganizerPlugin.hasSortRules(ItemCategory.HIGH_ALCH));
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

	// --- Issue #4: staves have a bigger crush than magic bonus, so stats alone call them melee ---

	private static ItemEquipmentStats battlestaffStats()
	{
		return ItemEquipmentStats.builder().slot(3).astab(7).aslash(-1).acrush(28).amagic(12).str(35).build();
	}

	@Test
	public void testStavesAreMageWeaponsDespiteCrushBonus()
	{
		ItemEquipmentStats ancient = ItemEquipmentStats.builder().slot(3).astab(15).aslash(-1).acrush(40).amagic(15).str(50).build();
		ItemEquipmentStats ibans = ItemEquipmentStats.builder().slot(3).astab(10).acrush(50).amagic(10).str(50).build();
		ItemEquipmentStats ahrims = ItemEquipmentStats.builder().slot(3).astab(12).aslash(-1).acrush(65).amagic(15).str(68).mdmg(5).build();
		String[] battlestaves = { "Lava battlestaff", "Mud battlestaff", "Mist battlestaff", "Mystic lava staff",
			"Mystic air staff", "Staff of air", "Slayer's staff", "Dramen staff", "Lunar staff" };
		for (String n : battlestaves)
		{
			assertEquals(n, GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory(n, 99999, battlestaffStats()));
		}
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Ancient staff", 4675, ancient));
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Iban's staff", 1409, ibans));
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Ahrim's staff", 4710, ahrims));
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Toktz-mej-tal", 6526,
			ItemEquipmentStats.builder().slot(3).acrush(35).amagic(15).str(35).build()));
		// mage weapons the stats already got right still work, with or without stats
		ItemEquipmentStats mage = ItemEquipmentStats.builder().slot(3).amagic(25).mdmg(15).build();
		String[] mageWeapons = { "Kodai wand", "Master wand", "Trident of the seas", "Toxic staff of the dead",
			"Nightmare staff", "Sanguinesti staff", "Tumeken's shadow", "Thammaron's sceptre", "Dawnbringer" };
		for (String n : mageWeapons)
		{
			assertEquals(n, GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory(n, 99999, mage));
		}
		for (String n : new String[]{ "Kodai wand", "Trident of the swamp", "Ancient staff", "Lava battlestaff" })
		{
			assertEquals(n, GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory(n, 99999, null));
		}
		// melee weapons with a small magic bonus stay melee
		assertEquals(GearSubCategory.MELEE_WEAPON, categorizer.getGearSubCategory("Void knight mace", 8841,
			ItemEquipmentStats.builder().slot(3).acrush(42).amagic(6).str(38).build()));
		assertEquals(GearSubCategory.MELEE_WEAPON, categorizer.getGearSubCategory("Arclight", 19675,
			ItemEquipmentStats.builder().slot(3).astab(67).aslash(55).str(62).build()));
		assertEquals(GearSubCategory.MELEE_WEAPON, categorizer.getGearSubCategory("Abyssal whip", 4151,
			ItemEquipmentStats.builder().slot(3).aslash(82).str(82).build()));
		// and in the Combat tab the staves sort into the mage group, not the melee one
		GearSortMode mode = GearSortMode.COMBAT_STYLE;
		long lava = categorizer.getGearFullSortKey("Lava battlestaff", 3053, battlestaffStats(), mode);
		long whip = categorizer.getGearFullSortKey("Abyssal whip", 4151,
			ItemEquipmentStats.builder().slot(3).aslash(82).str(82).build(), mode);
		long sang = categorizer.getGearFullSortKey("Sanguinesti staff", 22323, mage, mode);
		assertTrue(whip < lava);
		assertEquals(sang >> 44, lava >> 44);
	}

	@Test
	public void testTwinflameStaffIsMageAndMegaRaresTopTheirStyle()
	{
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Twinflame staff", 99999,
			ItemEquipmentStats.builder().slot(3).astab(10).aslash(10).acrush(10).amagic(5).build()));
		GearSortMode mode = GearSortMode.values()[0];
		ItemEquipmentStats meleeBis = ItemEquipmentStats.builder().slot(3).aslash(150).str(150).build();
		ItemEquipmentStats rangedBis = ItemEquipmentStats.builder().slot(3).isTwoHanded(true).arange(150).rstr(150).build();
		ItemEquipmentStats mageBis = ItemEquipmentStats.builder().slot(3).amagic(150).mdmg(20).build();
		long scythe = categorizer.getGearFullSortKey("Holy scythe of vitur (uncharged)", 25736, null, mode);
		long tbow = categorizer.getGearFullSortKey("Twisted bow", 20997, null, mode);
		long shadow = categorizer.getGearFullSortKey("Tumeken's shadow (uncharged)", 27277, null, mode);
		long rapier = categorizer.getGearFullSortKey("Ghrazi rapier", 22324, meleeBis, mode);
		long bowfa = categorizer.getGearFullSortKey("Bow of faerdhinen", 25865, rangedBis, mode);
		long sang = categorizer.getGearFullSortKey("Sanguinesti staff", 22323, mageBis, mode);
		// each pin heads its own style group...
		assertTrue(scythe < rapier);
		assertTrue(tbow < bowfa);
		assertTrue(shadow < sang);
		// ...but stays inside it: the melee group's rapier still precedes the ranged group's tbow
		assertEquals(GearSubCategory.MELEE_WEAPON, categorizer.getGearSubCategory("Scythe of vitur", 22325));
		assertEquals(GearSubCategory.RANGED_WEAPON, categorizer.getGearSubCategory("Twisted bow", 20997));
		assertEquals(GearSubCategory.MAGE_WEAPON, categorizer.getGearSubCategory("Tumeken's shadow", 27275));
		assertTrue((rapier >> 44) == (scythe >> 44));
		assertTrue((scythe >> 44) != (tbow >> 44));
	}
	/** Every gem on the wiki Gems page (uncut and cut) is a Material in the Mining group; quest gems are not. */
	@Test
	public void testWikiGemsAreMiningMaterials()
	{
		Object[][] gems = {
			{"Uncut sapphire", 1623}, {"Sapphire", 1607}, {"Uncut emerald", 1621}, {"Emerald", 1605},
			{"Uncut ruby", 1619}, {"Ruby", 1603}, {"Uncut diamond", 1617}, {"Diamond", 1601},
			{"Uncut dragonstone", 1631}, {"Dragonstone", 1615}, {"Uncut onyx", 6571}, {"Onyx", 6573},
			{"Uncut zenyte", 19496}, {"Zenyte", 19493}, {"Uncut opal", 1625}, {"Opal", 1609},
			{"Uncut jade", 1627}, {"Jade", 1611}, {"Uncut red topaz", 1629}, {"Red topaz", 1613},
			{"Crushed gem", 1633}, {"Zenyte shard", 19529}, {"Amethyst", 21347}, {"Oyster pearl", 411}, {"Oyster pearls", 413}
		};
		for (Object[] g : gems)
		{
			String name = (String) g[0]; int id = (Integer) g[1];
			assertEquals(name, ItemCategory.RAW_MATERIALS, categorizer.categorize(name, id));
			assertEquals(name, 4, categorizer.getMaterialGroupIndex(name, id));
		}
		for (String quest : new String[] {"Blood diamond", "Ice diamond", "Smoke diamond", "Shadow diamond", "Enchanted gem", "Eternal gem"})
		{
			assertEquals(quest, ItemCategory.QUEST_MISC, categorizer.categorize(quest, 99999));
		}
		// cut gem before its uncut version by id must not matter: the uncut block always comes first
		assertTrue(mat("Uncut sapphire") < mat("Sapphire"));
		assertTrue(mat("Uncut onyx") < mat("Onyx"));
		assertTrue(categorizer.getMaterialFullSortKey("Uncut zenyte", 19496) < categorizer.getMaterialFullSortKey("Zenyte shard", 19529));
		assertTrue(mat("Zenyte shard") < mat("Opal"));
	}

	/** Pinning a subcategory keeps ore -> bar -> gem -> tier order inside the pinned group. */
	@Test
	public void testSubCategoryOverrideKeepsMaterialTierOrder()
	{
		categorizer.setSubCategoryOverride(1623, 4); // Uncut sapphire -> Mining (already Mining by rule)
		categorizer.setSubCategoryOverride(19496, 4); // Uncut zenyte -> Mining
		categorizer.setSubCategoryOverride(440, 4); // Iron ore -> Mining
		long ore = categorizer.getMaterialFullSortKey("Iron ore", 440);
		long sapphire = categorizer.getMaterialFullSortKey("Uncut sapphire", 1623);
		long zenyte = categorizer.getMaterialFullSortKey("Uncut zenyte", 19496);
		assertTrue(ore < sapphire); assertTrue(sapphire < zenyte);
		assertEquals(4, categorizer.getMaterialGroupIndex("Uncut zenyte", 19496));
		// pinned to another group: moves group, keeps relative order
		categorizer.setSubCategoryOverride(1623, 9); // Uncut sapphire -> Crafting
		categorizer.setSubCategoryOverride(19496, 9); // Uncut zenyte -> Crafting
		assertEquals(9, categorizer.getMaterialGroupIndex("Uncut sapphire", 1623));
		assertTrue(categorizer.getMaterialFullSortKey("Uncut sapphire", 1623) < categorizer.getMaterialFullSortKey("Uncut zenyte", 19496));
	}

	@Test
	public void untradeableLeftoversAreQuestTradeableAreMisc()
	{
		ItemCategorizer c = new ItemCategorizer();
		c.setQuestHint(id -> id == 1);
		assertEquals(ItemCategory.QUEST, c.categorize("Ghost's skull", 1));
		assertEquals(ItemCategory.QUEST_MISC, c.categorize("Ghost's skull", 2));
		// a keyword match still wins over the quest hint
		assertEquals(ItemCategory.FOOD, c.categorize("Shark", 1));
	}

	@Test
	public void questSplitIsOffWithoutAHint()
	{
		assertEquals(ItemCategory.QUEST_MISC, new ItemCategorizer().categorize("Ghost's skull", 1));
	}
	/** Unstrung amulets and symbols are crafting intermediates: Materials, Crafting group, gold first then by gem. */
	@Test
	public void testUnstrungJewelleryIsCraftingMaterial()
	{
		Object[][] items = {
			{"Gold amulet (u)", 1673}, {"Sapphire amulet (u)", 1675}, {"Emerald amulet (u)", 1677}, {"Ruby amulet (u)", 1679},
			{"Diamond amulet (u)", 1681}, {"Dragonstone amulet (u)", 1683}, {"Onyx amulet (u)", 6579}, {"Zenyte amulet (u)", 19501},
			{"Opal amulet (u)", 21133}, {"Jade amulet (u)", 21139}, {"Topaz amulet (u)", 21145},
			{"Unstrung symbol", 1714}, {"Unstrung emblem", 1720}
		};
		for (Object[] it : items)
		{
			String name = (String) it[0]; int id = (Integer) it[1];
			assertEquals(name, ItemCategory.RAW_MATERIALS, categorizer.categorize(name, id));
			assertEquals(name, 9, categorizer.getMaterialGroupIndex(name, id));
		}
		assertTrue(mat("Gold amulet (u)") < mat("Sapphire amulet (u)"));
		assertTrue(mat("Sapphire amulet (u)") < mat("Zenyte amulet (u)"));
		assertTrue(mat("Zenyte amulet (u)") < mat("Unstrung symbol"));
		// strung jewellery is untouched
		assertEquals(ItemCategory.GEAR, categorizer.categorize("Sapphire amulet", 1694));
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Amulet of glory", 1704));
	}

	/** Common-item audit, 1.2.2: rules that used to send these to the wrong tab. */
	@Test
	public void testCommonItemAuditRules()
	{
		Object[][] items = {
			{"Mithril gloves", 7458, ItemCategory.GEAR}, {"Adamant gloves", 7459, ItemCategory.GEAR},
			{"Adamant platebody", 1123, ItemCategory.HIGH_ALCH},
			{"Lobster pot", 301, ItemCategory.SKILLING}, {"Lobster", 379, ItemCategory.FOOD},
			{"Sanfew serum(4)", 10925, ItemCategory.POTIONS},
			{"Purple sweets", 10476, ItemCategory.FOOD}, {"Curry", 2011, ItemCategory.FOOD},
			{"Wild pie", 7208, ItemCategory.FOOD}, {"Half a redberry pie", 2333, ItemCategory.FOOD},
			{"Ring of the elements", 26815, ItemCategory.TELEPORTS}, {"Construct. cape", 9789, ItemCategory.TELEPORTS},
			{"Ensouled dragon head", 13510, ItemCategory.RAW_MATERIALS},
			{"Yew longbow (u)", 66, ItemCategory.RAW_MATERIALS}, {"Runite crossbow (u)", 9465, ItemCategory.RAW_MATERIALS},
			// near misses the new rules must leave alone
			{"Map piece", 22009, ItemCategory.QUEST_MISC}, {"Unfired pie dish", 1789, ItemCategory.QUEST_MISC},
			{"Scurry", 28801, ItemCategory.QUEST_MISC}, {"Curry leaf", 5970, ItemCategory.RAW_MATERIALS},
			{"Super attack(4)", 2436, ItemCategory.POTIONS}, {"Super strength(4)", 2440, ItemCategory.POTIONS},
			{"Super defence(4)", 2442, ItemCategory.POTIONS}, {"Relicym's balm(4)", 4842, ItemCategory.POTIONS},
			{"Rainbow fish", 10136, ItemCategory.FOOD}, {"Cave eel", 5003, ItemCategory.FOOD}, {"Ugthanki kebab", 1883, ItemCategory.FOOD},
			{"Karambwan vessel", 3157, ItemCategory.SKILLING}, {"Ring mould", 1592, ItemCategory.SKILLING},
			{"Bird snare", 10006, ItemCategory.SKILLING}, {"Medium pouch", 5510, ItemCategory.SKILLING},
			{"Beekeeper's hat", 25129, ItemCategory.SKILLING}, {"Cake tin", 1887, ItemCategory.SKILLING},
			{"Amulet of eternal glory", 19707, ItemCategory.TELEPORTS}, {"Divine rune pouch", 27281, ItemCategory.TELEPORTS},
			{"Fiendish ashes", 25766, ItemCategory.RAW_MATERIALS}, {"Yew stock", 9452, ItemCategory.RAW_MATERIALS},
			{"Runite limbs", 9431, ItemCategory.RAW_MATERIALS}, {"Leaping sturgeon", 11332, ItemCategory.RAW_MATERIALS},
			{"Sacred eel", 13339, ItemCategory.RAW_MATERIALS}, {"Snakeskin", 6289, ItemCategory.RAW_MATERIALS},
			{"Embalming manual", 4686, ItemCategory.QUEST_MISC},
			{"Pike", 351, ItemCategory.FOOD}, {"Spiked manacles", 23389, ItemCategory.QUEST_MISC}, {"Kebbit spike", 10105, ItemCategory.QUEST_MISC},
			{"Soulreaper axe", 25484, ItemCategory.GEAR}, {"Cheese", 1985, ItemCategory.FOOD},
			{"Ancient emblem", 21807, ItemCategory.CURRENCY}, {"Blood money", 13307, ItemCategory.CURRENCY},
			{"Sunlight moth", 28890, ItemCategory.RAW_MATERIALS}, {"Calcified moth", 29090, ItemCategory.TELEPORTS},
			{"Gricoller's can", 13353, ItemCategory.SKILLING},
			{"Confliction gauntlets", 31106, ItemCategory.GEAR}, {"Avernic treads", 31088, ItemCategory.GEAR},
			{"Avernic treads (max)", 99998, ItemCategory.GEAR},
		};
		for (Object[] it : items)
		{
			assertEquals((String) it[0], it[2], categorizer.categorize((String) it[0], (Integer) it[1]));
		}
		assertEquals(5, categorizer.getMaterialGroupIndex("Ensouled dragon head", 13510));
		assertEquals(12, categorizer.getMaterialGroupIndex("Yew longbow (u)", 66));
		assertTrue(mat("Oak longbow (u)") < mat("Magic longbow (u)"));
		assertEquals(3, categorizer.getSkillGroupIndex("Lobster pot", 301));
	}

	/** Shipped defaults corrected in the 1.2.2 audit. */
	@Test
	public void testAuditedShippedDefaults() throws Exception
	{
		Map<Integer, ItemCategory> d = ItemCategorizer.parseCategoryOverrides(readResource("default_overrides.txt"));
		assertEquals(ItemCategory.GEAR, d.get(4153));      // Granite maul
		assertEquals(ItemCategory.GEAR, d.get(810));       // Adamant dart
		assertEquals(ItemCategory.GEAR, d.get(6328));      // Snakeskin boots
		assertEquals(ItemCategory.GEAR, d.get(892));       // Rune arrow
		assertEquals(ItemCategory.FOOD, d.get(329));       // Salmon
		assertEquals(ItemCategory.FOOD, d.get(333));       // Trout
		assertEquals(ItemCategory.SKILLING, d.get(1275));  // Rune pickaxe
		assertEquals(ItemCategory.SKILLING, d.get(1359));  // Rune axe
		assertEquals(ItemCategory.CURRENCY, d.get(11849)); // Mark of grace
		assertEquals(ItemCategory.HIGH_ALCH, d.get(1127)); // Rune platebody stays alch fodder
		assertEquals(ItemCategory.GEAR, d.get(890));       // Adamant arrow
		assertEquals(ItemCategory.GEAR, d.get(9245));      // Onyx bolts (e)
		// real gear is never pinned to Misc by a shipped default
		for (int id : new int[]{7158, 859, 12853, 2570, 22975, 4081, 9244, 1351})
		{
			assertNull("shipped default still pins " + id, d.get(id));
		}
	}
}
