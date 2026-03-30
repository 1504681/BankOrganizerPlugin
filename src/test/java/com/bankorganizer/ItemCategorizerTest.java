package com.bankorganizer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

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
		assertEquals(ItemCategory.TOOLS, categorizer.categorize("Rune pickaxe", 99999));
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
		assertEquals(ItemCategory.TOOLS, categorizer.categorize("Bronze pickaxe", 99999));
	}

	@Test
	public void testTeleportTablet()
	{
		assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Varrock teleport", 99999));
	}
}
