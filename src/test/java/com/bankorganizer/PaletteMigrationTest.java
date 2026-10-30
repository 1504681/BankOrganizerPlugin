package com.bankorganizer;

import java.awt.Color;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class PaletteMigrationTest
{
	@Test
	public void freshInstallTakesNewDefaults()
	{
		assertTrue(BankOrganizerPlugin.colorsToPin(false, key -> false).isEmpty());
	}

	@Test
	public void existingInstallKeepsOldDefaultsAndCustomColours()
	{
		// user picked their own Food colour; everything else was on the old defaults
		Map<String, Color> pin = BankOrganizerPlugin.colorsToPin(true, "colorFood"::equals);
		assertEquals(BankOrganizerPlugin.COLOR_KEYS.length - 1, pin.size());
		assertFalse(pin.containsKey("colorFood"));
		assertEquals(new Color(60, 200, 60), pin.get("colorQuest"));
		assertEquals(new Color(255, 255, 255), pin.get("colorCurrency"));
	}

	@Test
	public void keysMatchConfigAndCategories()
	{
		assertEquals(ItemCategory.values().length, BankOrganizerPlugin.COLOR_KEYS.length);
		for (String key : BankOrganizerPlugin.COLOR_KEYS)
		{
			boolean found = false;
			for (java.lang.reflect.Method m : BankOrganizerConfig.class.getMethods())
			{
				net.runelite.client.config.ConfigItem ci = m.getAnnotation(net.runelite.client.config.ConfigItem.class);
				if (ci != null && ci.keyName().equals(key) && m.getReturnType() == Color.class) found = true;
			}
			assertTrue("no Color config item " + key, found);
		}
	}

	@Test
	public void newDefaultsDifferFromOldWhereRedesigned()
	{
		// Teleports and Combat are fixed; Quest must no longer be the old green
		assertEquals(new Color(0, 150, 255), ItemCategory.TELEPORTS.getColor());
		assertEquals(new Color(220, 50, 50), ItemCategory.GEAR.getColor());
		assertNotEquals(new Color(60, 200, 60), ItemCategory.QUEST.getColor());
		assertNotEquals(ItemCategory.QUEST.getPattern(), ItemCategory.QUEST_MISC.getPattern());
	}
}
