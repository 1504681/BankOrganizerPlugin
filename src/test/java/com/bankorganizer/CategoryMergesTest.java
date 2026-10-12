package com.bankorganizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import org.junit.Test;

public class CategoryMergesTest
{
	private static final UnaryOperator<ItemCategory> FOOD_INTO_POTIONS_QUEST_INTO_MISC = c ->
		c == ItemCategory.FOOD ? ItemCategory.POTIONS : c == ItemCategory.QUEST ? ItemCategory.QUEST_MISC : c;

	@Test
	public void acceptsOwnAndMergedCategories()
	{
		assertTrue(CategoryMerges.accepts(ItemCategory.POTIONS, ItemCategory.POTIONS, FOOD_INTO_POTIONS_QUEST_INTO_MISC));
		assertTrue(CategoryMerges.accepts(ItemCategory.POTIONS, ItemCategory.FOOD, FOOD_INTO_POTIONS_QUEST_INTO_MISC));
		assertFalse(CategoryMerges.accepts(ItemCategory.FOOD, ItemCategory.POTIONS, FOOD_INTO_POTIONS_QUEST_INTO_MISC));
		assertFalse(CategoryMerges.accepts(ItemCategory.POTIONS, ItemCategory.GEAR, FOOD_INTO_POTIONS_QUEST_INTO_MISC));
	}

	@Test
	public void mergedCategoryPointsAtTargetTab()
	{
		Map<ItemCategory, Integer> m = new EnumMap<>(ItemCategory.class);
		m.put(ItemCategory.POTIONS, 3);
		m.put(ItemCategory.FOOD, 4);
		m.put(ItemCategory.QUEST_MISC, 7);
		CategoryMerges.applyMerges(m, FOOD_INTO_POTIONS_QUEST_INTO_MISC);
		assertEquals(Integer.valueOf(3), m.get(ItemCategory.FOOD));   // merge wins over Food's own tab
		assertEquals(Integer.valueOf(7), m.get(ItemCategory.QUEST));  // quest items go to the Misc tab
		assertEquals(Integer.valueOf(3), m.get(ItemCategory.POTIONS));
	}

	@Test
	public void targetWithoutATabLeavesMappingAlone()
	{
		Map<ItemCategory, Integer> m = new EnumMap<>(ItemCategory.class);
		m.put(ItemCategory.FOOD, 4);
		CategoryMerges.applyMerges(m, FOOD_INTO_POTIONS_QUEST_INTO_MISC);
		assertEquals(Integer.valueOf(4), m.get(ItemCategory.FOOD));
		assertNull(m.get(ItemCategory.QUEST));
	}
}
