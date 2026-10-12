package com.bankorganizer;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/** Merge rules: a category's items are expected in the tab of the category it is merged into. One hop only. */
final class CategoryMerges
{
	private CategoryMerges() { }

	/** True when an item of {@code itemCat} belongs in a tab mapped to {@code tabCat}. */
	static boolean accepts(ItemCategory tabCat, ItemCategory itemCat, UnaryOperator<ItemCategory> target)
	{
		return itemCat == tabCat || target.apply(itemCat) == tabCat;
	}

	/**
	 * Point every merged category at its target's tab (the merge wins over a tab mapped to the category itself).
	 * A target without a tab leaves the category's own mapping, if any, untouched.
	 */
	static Map<ItemCategory, Integer> applyMerges(Map<ItemCategory, Integer> mappings, UnaryOperator<ItemCategory> target)
	{
		Map<ItemCategory, Integer> base = new EnumMap<>(ItemCategory.class);
		base.putAll(mappings);
		for (ItemCategory c : ItemCategory.values())
		{
			ItemCategory t = target.apply(c);
			if (t == null || t == c) continue;
			Integer tab = base.get(t);
			if (tab != null) mappings.put(c, tab);
		}
		return mappings;
	}
}
