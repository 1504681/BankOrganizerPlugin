package com.bankorganizer;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Bank Organizer",
	description = "Scans bank items and highlights misplaced ones based on category presets",
	tags = {"bank", "organizer", "sort", "tab", "category"}
)
public class BankOrganizerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private BankOrganizerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private BankOrganizerOverlay overlay;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	private BankOrganizerPanel panel;
	private NavigationButton navButton;
	private ItemCategorizer categorizer;

	@Getter
	private Map<Integer, ItemCategory> misplacedItems = new HashMap<>();

	@Getter
	private Map<Integer, String> misplacedItemNames = new HashMap<>();

	@Getter
	@Setter
	private ItemCategory activeFilter;

	@Override
	protected void startUp()
	{
		categorizer = new ItemCategorizer();
		updateRegexFromConfig();

		panel = new BankOrganizerPanel(this);

		BufferedImage icon;
		try
		{
			icon = ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/bank/bank_icon.png");
		}
		catch (Exception e)
		{
			icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		}

		navButton = NavigationButton.builder()
			.tooltip("Bank Organizer")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(overlay);

		log.info("Bank Organizer started!");
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		overlayManager.remove(overlay);
		misplacedItems.clear();
		misplacedItemNames.clear();

		log.info("Bank Organizer stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("bankorganizer".equals(event.getGroup()))
		{
			updateRegexFromConfig();
		}
	}

	private void updateRegexFromConfig()
	{
		Map<ItemCategory, String> patterns = new EnumMap<>(ItemCategory.class);
		patterns.put(ItemCategory.TELEPORTS, config.regexTeleports());
		patterns.put(ItemCategory.GEAR, config.regexGear());
		patterns.put(ItemCategory.POTIONS, config.regexPotions());
		patterns.put(ItemCategory.FOOD, config.regexFood());
		patterns.put(ItemCategory.TOOLS, config.regexTools());
		patterns.put(ItemCategory.RAW_MATERIALS, config.regexRawMaterials());
		categorizer.setRegexPatterns(patterns);
	}

	public ItemCategory getCategoryForTab(int tabNumber)
	{
		switch (tabNumber)
		{
			case 1: return config.tab1Category();
			case 2: return config.tab2Category();
			case 3: return config.tab3Category();
			case 4: return config.tab4Category();
			case 5: return config.tab5Category();
			case 6: return config.tab6Category();
			case 7: return config.tab7Category();
			case 8: return config.tab8Category();
			case 9: return config.tab9Category();
			default: return null;
		}
	}

	public Map<ItemCategory, Integer> getTabMappings()
	{
		Map<ItemCategory, Integer> mappings = new EnumMap<>(ItemCategory.class);
		for (int i = 1; i <= 9; i++)
		{
			ItemCategory cat = getCategoryForTab(i);
			if (cat != null && !mappings.containsKey(cat))
			{
				mappings.put(cat, i);
			}
		}
		return mappings;
	}

	private int getCurrentBankTab()
	{
		return client.getVarbitValue(4150);
	}

	public void scanCurrentTab()
	{
		clientThread.invokeLater(() ->
		{
			Widget bankWidget = client.getWidget(WidgetInfo.BANK_CONTAINER);
			if (bankWidget == null || bankWidget.isHidden())
			{
				log.debug("Bank is not open");
				return;
			}

			int currentTab = getCurrentBankTab();
			ItemCategory expectedCategory = getCategoryForTab(currentTab);

			Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
			if (bankItemContainer == null)
			{
				return;
			}

			Widget[] children = bankItemContainer.getDynamicChildren();
			if (children == null)
			{
				return;
			}

			Map<Integer, ItemCategory> newMisplaced = new HashMap<>();
			Map<Integer, String> newNames = new HashMap<>();

			for (int slot = 0; slot < children.length; slot++)
			{
				Widget child = children[slot];
				if (child == null || child.isHidden())
				{
					continue;
				}

				int itemId = child.getItemId();
				if (itemId <= 0)
				{
					continue;
				}

				String itemName = itemManager.getItemComposition(itemId).getName();
				if (itemName == null || itemName.equals("null"))
				{
					continue;
				}

				ItemCategory correctCategory = categorizer.categorize(itemName, itemId);

				if (expectedCategory != null && correctCategory != expectedCategory)
				{
					newMisplaced.put(slot, correctCategory);
					newNames.put(slot, itemName);
				}
				else if (expectedCategory == null)
				{
					// Main tab or unmapped: show where each item should go
					newMisplaced.put(slot, correctCategory);
					newNames.put(slot, itemName);
				}
			}

			misplacedItems = newMisplaced;
			misplacedItemNames = newNames;

			Map<ItemCategory, Integer> tabMappings = getTabMappings();

			SwingUtilities.invokeLater(() ->
				panel.updateResults(newMisplaced, newNames, tabMappings));

			log.info("Scan complete: {} misplaced items found", newMisplaced.size());
		});
	}
}
