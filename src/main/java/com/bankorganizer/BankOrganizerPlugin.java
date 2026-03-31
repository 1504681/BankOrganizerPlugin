package com.bankorganizer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Bank Organizer",
	description = "Scans bank items and highlights misplaced ones based on category presets",
	tags = {"bank", "organizer", "sort", "tab", "category"}
)
public class BankOrganizerPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(BankOrganizerPlugin.class);
	private static final String MENU_SET_PREFIX = "Set: ";
	private static final String MENU_REMOVE_OVERRIDE = "Remove Override";

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

	private Map<Integer, ItemCategory> misplacedItems = new HashMap<>();
	private Map<Integer, String> misplacedItemNames = new HashMap<>();
	private ItemCategory activeFilter;
	private boolean scanActive = false;
	private boolean categorizeMode = false;

	// Ordering state
	private boolean orderingActive = false;
	private List<OrderStep> orderSteps = new ArrayList<>();
	private int currentOrderStep = 0;

	public Map<Integer, ItemCategory> getMisplacedItems() { return misplacedItems; }
	public Map<Integer, String> getMisplacedItemNames() { return misplacedItemNames; }
	public ItemCategory getActiveFilter() { return activeFilter; }
	public void setActiveFilter(ItemCategory activeFilter) { this.activeFilter = activeFilter; }
	public boolean isScanActive() { return scanActive; }
	public boolean isCategorizeMode() { return categorizeMode; }
	public void setCategorizeMode(boolean mode) { this.categorizeMode = mode; }
	public boolean isOrderingActive() { return orderingActive; }
	public List<OrderStep> getOrderSteps() { return orderSteps; }
	public int getCurrentOrderStep() { return currentOrderStep; }
	public ItemCategorizer getCategorizer() { return categorizer; }
	public ItemManager getItemManager() { return itemManager; }
	public BankOrganizerConfig getConfig() { return config; }

	@Provides
	BankOrganizerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankOrganizerConfig.class);
	}

	@Override
	protected void startUp()
	{
		categorizer = new ItemCategorizer();
		updateRegexFromConfig();
		loadOverridesFromConfig();

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
		scanActive = false;
		categorizeMode = false;
		orderingActive = false;

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

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!orderingActive || event.getContainerId() != InventoryID.BANK.getId())
		{
			return;
		}

		// Re-scan and recompute remaining steps after each bank change
		recomputeOrderSteps();
	}

	private void recomputeOrderSteps()
	{
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null) return;
		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null) return;

		int currentTab = client.getVarbitValue(4150);
		ItemCategory tabCategory = getCategoryForTab(currentTab);
		if (tabCategory == null) return;

		// Collect current items
		List<BankItem> currentItems = new ArrayList<>();
		for (int slot = 0; slot < children.length; slot++)
		{
			Widget child = children[slot];
			if (child == null || child.isHidden()) continue;
			int itemId = child.getItemId();
			if (itemId <= 0) continue;
			String name = itemManager.getItemComposition(itemId).getName();
			if (name == null || name.equals("null")) continue;
			currentItems.add(new BankItem(itemId, name, slot));
		}

		// Compute ideal order
		List<BankItem> idealOrder = new ArrayList<>(currentItems);
		GearSortMode gearMode = config.gearSortMode();
		TeleportSortMode teleportMode = config.teleportSortMode();

		idealOrder.sort(Comparator.comparingInt(item ->
		{
			if (tabCategory == ItemCategory.GEAR)
			{
				GearSubCategory sub = categorizer.getGearSubCategory(item.name, item.itemId, getEquipmentStats(item.itemId));
				return categorizer.getGearSortOrder(sub, gearMode);
			}
			else if (tabCategory == ItemCategory.TELEPORTS)
			{
				TeleportSubCategory sub = categorizer.getTeleportSubCategory(item.name, item.itemId);
				return categorizer.getTeleportSortOrder(sub, teleportMode);
			}
			return 0;
		}));

		// Find remaining steps
		List<OrderStep> newSteps = new ArrayList<>();
		List<BankItem> working = new ArrayList<>(currentItems);

		for (int targetSlot = 0; targetSlot < idealOrder.size(); targetSlot++)
		{
			BankItem idealItem = idealOrder.get(targetSlot);
			int currentSlot = -1;
			for (int j = 0; j < working.size(); j++)
			{
				if (working.get(j).itemId == idealItem.itemId)
				{
					currentSlot = j;
					break;
				}
			}

			if (currentSlot != targetSlot && currentSlot >= 0)
			{
				String subCatName = "";
				if (tabCategory == ItemCategory.GEAR)
				{
					subCatName = categorizer.getGearSubCategory(idealItem.name, idealItem.itemId, getEquipmentStats(idealItem.itemId)).getDisplayName();
				}
				else if (tabCategory == ItemCategory.TELEPORTS)
				{
					subCatName = categorizer.getTeleportSubCategory(idealItem.name, idealItem.itemId).getDisplayName();
				}

				// The item to insert before (what's currently at the target slot)
				BankItem targetItem = working.get(targetSlot);

				newSteps.add(new OrderStep(
					idealItem.itemId,
					idealItem.name,
					targetSlot,
					"Insert " + idealItem.name + " before " + targetItem.name,
					subCatName,
					targetItem.itemId
				));

				BankItem removed = working.remove(currentSlot);
				working.add(targetSlot, removed);
			}
		}

		if (newSteps.isEmpty())
		{
			log.info("Ordering complete!");
			orderingActive = false;
			orderSteps.clear();
			currentOrderStep = 0;
			SwingUtilities.invokeLater(() ->
			{
				panel.updateOrderingState();
				javax.swing.JOptionPane.showMessageDialog(null,
					"All items are now in order!",
					"Ordering Complete",
					javax.swing.JOptionPane.INFORMATION_MESSAGE);
			});
		}
		else
		{
			orderSteps = newSteps;
			currentOrderStep = 0;
			SwingUtilities.invokeLater(() -> panel.updateOrderingState());
			log.info("Ordering: {} steps remaining", newSteps.size());
		}
	}

	// === Right-click category assignment ===

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!categorizeMode)
		{
			return;
		}

		// Only add to bank item container actions
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null)
		{
			return;
		}

		// Check if this menu is for a bank item (Examine option on bank items)
		String option = event.getOption();
		if (!"Examine".equals(option))
		{
			return;
		}

		int widgetId = event.getActionParam1();
		if (widgetId != bankItemContainer.getId())
		{
			return;
		}

		// Get actual item ID from the widget child at this slot
		int slot = event.getActionParam0();
		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null || slot < 0 || slot >= children.length)
		{
			return;
		}
		Widget child = children[slot];
		if (child == null)
		{
			return;
		}
		int itemId = child.getItemId();
		if (itemId <= 0)
		{
			return;
		}

		// Find the current category for this item
		String itemName = itemManager.getItemComposition(itemId).getName();
		ItemCategory currentCategory = categorizer.categorize(
			itemName != null ? itemName : "", itemId);

		// Add "Remove Override" if has manual override
		if (categorizer.hasManualOverride(itemId))
		{
			client.createMenuEntry(-1)
				.setOption(MENU_REMOVE_OVERRIDE)
				.setTarget(event.getTarget())
				.setIdentifier(itemId)
				.setType(MenuAction.RUNELITE)
				.setParam0(event.getActionParam0())
				.setParam1(widgetId);
		}

		// Add category options in reverse order (they stack, last added = top)
		// Skip the current category, show it with "Current:" prefix instead
		ItemCategory[] categories = ItemCategory.values();
		for (int i = categories.length - 1; i >= 0; i--)
		{
			ItemCategory cat = categories[i];
			String colorTag = ColorUtil.colorTag(cat.getColor());

			if (cat == currentCategory)
			{
				// Show current category as non-actionable indicator
				client.createMenuEntry(-1)
					.setOption(colorTag + "Current: " + cat.getDisplayName())
					.setTarget(event.getTarget())
					.setIdentifier(itemId)
					.setType(MenuAction.RUNELITE)
					.setParam0(event.getActionParam0())
					.setParam1(widgetId);
			}
			else
			{
				client.createMenuEntry(-1)
					.setOption(colorTag + MENU_SET_PREFIX + cat.getDisplayName())
					.setTarget(event.getTarget())
					.setIdentifier(itemId)
					.setType(MenuAction.RUNELITE)
					.setParam0(event.getActionParam0())
					.setParam1(widgetId);
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!categorizeMode)
		{
			return;
		}

		String option = event.getMenuOption();
		if (option == null)
		{
			return;
		}

		// Strip color tags for comparison
		String stripped = option.replaceAll("<[^>]+>", "");

		if (MENU_REMOVE_OVERRIDE.equals(stripped))
		{
			int itemId = event.getId();
			categorizer.removeManualOverride(itemId);
			saveOverridesToConfig();
			log.info("Removed category override for item ID {}", itemId);
			// Update overlay immediately: remove from misplaced or re-categorize
			String name = misplacedItemNames.get(itemId);
			if (name != null)
			{
				ItemCategory newCat = categorizer.categorize(name, itemId);
				int currentTab = client.getVarbitValue(4150);
				ItemCategory expectedCat = getCategoryForTab(currentTab);
				if (expectedCat != null && newCat == expectedCat)
				{
					misplacedItems.remove(itemId);
					misplacedItemNames.remove(itemId);
				}
				else
				{
					misplacedItems.put(itemId, newCat);
				}
			}
			refreshPanel();
			return;
		}

		if (stripped.startsWith(MENU_SET_PREFIX))
		{
			String categoryName = stripped.substring(MENU_SET_PREFIX.length());
			int itemId = event.getId();
			log.info("Category click: itemId={}, category={}", itemId, categoryName);

			for (ItemCategory cat : ItemCategory.values())
			{
				if (cat.getDisplayName().equals(categoryName))
				{
					categorizer.setManualOverride(itemId, cat);
					saveOverridesToConfig();
					log.info("Set item ID {} to category {}", itemId, cat);
					// Update overlay immediately
					int currentTab = client.getVarbitValue(4150);
					ItemCategory expectedCat = getCategoryForTab(currentTab);
					String itemName = itemManager.getItemComposition(itemId).getName();
					if (expectedCat != null && cat == expectedCat)
					{
						misplacedItems.remove(itemId);
						misplacedItemNames.remove(itemId);
					}
					else
					{
						misplacedItems.put(itemId, cat);
						misplacedItemNames.put(itemId, itemName != null ? itemName : "Unknown");
						scanActive = true;
					}
					refreshPanel();
					break;
				}
			}
		}
	}

	// === Override persistence ===

	private void loadOverridesFromConfig()
	{
		String json = config.manualOverrides();
		if (json == null || json.isEmpty())
		{
			return;
		}

		Map<Integer, ItemCategory> overrides = new HashMap<>();
		// Simple parsing: "itemId:CATEGORY,itemId:CATEGORY,..."
		for (String entry : json.split(","))
		{
			String[] parts = entry.split(":");
			if (parts.length == 2)
			{
				try
				{
					int id = Integer.parseInt(parts[0].trim());
					ItemCategory cat = ItemCategory.valueOf(parts[1].trim());
					overrides.put(id, cat);
				}
				catch (Exception ignored)
				{
				}
			}
		}
		categorizer.loadManualOverrides(overrides);
	}

	private void saveOverridesToConfig()
	{
		Map<Integer, ItemCategory> overrides = categorizer.getManualOverrides();
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, ItemCategory> entry : overrides.entrySet())
		{
			if (sb.length() > 0) sb.append(",");
			sb.append(entry.getKey()).append(":").append(entry.getValue().name());
		}
		config.setManualOverrides(sb.toString());
	}

	public void exportOverrides()
	{
		Map<Integer, ItemCategory> overrides = categorizer.getManualOverrides();
		if (overrides.isEmpty())
		{
			log.info("No manual overrides to export");
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, ItemCategory> entry : overrides.entrySet())
		{
			if (sb.length() > 0) sb.append(",");
			sb.append(entry.getKey()).append(":").append(entry.getValue().name());
		}

		String exportStr = sb.toString();
		java.awt.Toolkit.getDefaultToolkit()
			.getSystemClipboard()
			.setContents(new java.awt.datatransfer.StringSelection(exportStr), null);
		log.info("Exported {} overrides to clipboard", overrides.size());

		javax.swing.JOptionPane.showMessageDialog(null,
			"Copied " + overrides.size() + " overrides to clipboard!\n\nShare this with others to import.",
			"Export Overrides", javax.swing.JOptionPane.INFORMATION_MESSAGE);
	}

	public void importOverrides()
	{
		try
		{
			String clipText = (String) java.awt.Toolkit.getDefaultToolkit()
				.getSystemClipboard()
				.getData(java.awt.datatransfer.DataFlavor.stringFlavor);

			if (clipText == null || clipText.trim().isEmpty())
			{
				javax.swing.JOptionPane.showMessageDialog(null,
					"Clipboard is empty. Copy an override string first.",
					"Import Overrides", javax.swing.JOptionPane.WARNING_MESSAGE);
				return;
			}

			int imported = 0;
			for (String entry : clipText.split(","))
			{
				String[] parts = entry.split(":");
				if (parts.length == 2)
				{
					try
					{
						int id = Integer.parseInt(parts[0].trim());
						ItemCategory cat = ItemCategory.valueOf(parts[1].trim());
						categorizer.setManualOverride(id, cat);
						imported++;
					}
					catch (Exception ignored)
					{
					}
				}
			}

			if (imported > 0)
			{
				saveOverridesToConfig();
				log.info("Imported {} overrides from clipboard", imported);
				javax.swing.JOptionPane.showMessageDialog(null,
					"Imported " + imported + " overrides!",
					"Import Overrides", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				javax.swing.JOptionPane.showMessageDialog(null,
					"No valid overrides found in clipboard.",
					"Import Overrides", javax.swing.JOptionPane.WARNING_MESSAGE);
			}
		}
		catch (Exception e)
		{
			log.error("Failed to import overrides", e);
			javax.swing.JOptionPane.showMessageDialog(null,
				"Failed to read clipboard.",
				"Import Overrides", javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Get equipment stats for an item, or null if not equipable.
	 */
	private net.runelite.http.api.item.ItemEquipmentStats getEquipmentStats(int itemId)
	{
		net.runelite.http.api.item.ItemStats stats = itemManager.getItemStats(itemId, false);
		if (stats != null && stats.isEquipable() && stats.getEquipment() != null)
		{
			return stats.getEquipment();
		}
		return null;
	}

	private void refreshPanel()
	{
		Map<ItemCategory, Integer> tabMappings = getTabMappings();
		Map<Integer, ItemCategory> items = new HashMap<>(misplacedItems);
		Map<Integer, String> names = new HashMap<>(misplacedItemNames);
		SwingUtilities.invokeLater(() -> panel.updateResults(items, names, tabMappings));
	}

	// === Config helpers ===

	private void updateRegexFromConfig()
	{
		Map<ItemCategory, String> patterns = new EnumMap<>(ItemCategory.class);
		patterns.put(ItemCategory.TELEPORTS, config.regexTeleports());
		patterns.put(ItemCategory.GEAR, config.regexGear());
		patterns.put(ItemCategory.POTIONS, config.regexPotions());
		patterns.put(ItemCategory.FOOD, config.regexFood());
		patterns.put(ItemCategory.SKILLING, config.regexSkilling());
		patterns.put(ItemCategory.RAW_MATERIALS, config.regexRawMaterials());
		patterns.put(ItemCategory.HIGH_ALCH, config.regexHighAlch());
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

	// === Scan ===

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
				log.debug("Item: {} (ID: {}) -> {}", itemName, itemId, correctCategory);

				if (expectedCategory != null && correctCategory != expectedCategory)
				{
					newMisplaced.put(itemId, correctCategory);
					newNames.put(itemId, itemName);
				}
				else if (expectedCategory == null)
				{
					newMisplaced.put(itemId, correctCategory);
					newNames.put(itemId, itemName);
				}
			}

			misplacedItems = newMisplaced;
			misplacedItemNames = newNames;
			scanActive = true;

			Map<ItemCategory, Integer> tabMappings = getTabMappings();

			SwingUtilities.invokeLater(() ->
				panel.updateResults(newMisplaced, newNames, tabMappings));

			log.info("Scan complete: {} misplaced items found", newMisplaced.size());
		});
	}

	// === Ordering ===

	public void startOrdering()
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
			ItemCategory tabCategory = getCategoryForTab(currentTab);
			if (tabCategory == null)
			{
				log.debug("No category mapped to current tab");
				return;
			}

			Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
			if (bankItemContainer == null) return;
			Widget[] children = bankItemContainer.getDynamicChildren();
			if (children == null) return;

			// Collect all items in the tab with their current positions
			List<BankItem> currentItems = new ArrayList<>();
			for (int slot = 0; slot < children.length; slot++)
			{
				Widget child = children[slot];
				if (child == null || child.isHidden()) continue;
				int itemId = child.getItemId();
				if (itemId <= 0) continue;
				String name = itemManager.getItemComposition(itemId).getName();
				if (name == null || name.equals("null")) continue;
				currentItems.add(new BankItem(itemId, name, slot));
			}

			// Sort items into ideal order
			List<BankItem> idealOrder = new ArrayList<>(currentItems);
			GearSortMode gearMode = config.gearSortMode();
			TeleportSortMode teleportMode = config.teleportSortMode();

			idealOrder.sort(Comparator.comparingInt(item ->
			{
				if (tabCategory == ItemCategory.GEAR)
				{
					GearSubCategory sub = categorizer.getGearSubCategory(item.name, item.itemId, getEquipmentStats(item.itemId));
					return categorizer.getGearSortOrder(sub, gearMode);
				}
				else if (tabCategory == ItemCategory.TELEPORTS)
				{
					TeleportSubCategory sub = categorizer.getTeleportSubCategory(item.name, item.itemId);
					return categorizer.getTeleportSortOrder(sub, teleportMode);
				}
				return 0;
			}));

			// Generate order steps: find items that need to move
			List<OrderStep> steps = new ArrayList<>();
			for (int targetSlot = 0; targetSlot < idealOrder.size(); targetSlot++)
			{
				BankItem idealItem = idealOrder.get(targetSlot);
				// Find where this item currently is in the working order
				int currentSlot = -1;
				for (int j = 0; j < currentItems.size(); j++)
				{
					if (currentItems.get(j).itemId == idealItem.itemId)
					{
						currentSlot = j;
						break;
					}
				}

				if (currentSlot != targetSlot && currentSlot >= 0)
				{
					String subCatName = "";
					if (tabCategory == ItemCategory.GEAR)
					{
						subCatName = categorizer.getGearSubCategory(idealItem.name, idealItem.itemId, getEquipmentStats(idealItem.itemId)).getDisplayName();
					}
					else if (tabCategory == ItemCategory.TELEPORTS)
					{
						subCatName = categorizer.getTeleportSubCategory(idealItem.name, idealItem.itemId).getDisplayName();
					}

					BankItem targetItem = targetSlot < currentItems.size() ?
						currentItems.get(targetSlot) : null;
					String targetItemName = targetItem != null ? targetItem.name : "position " + (targetSlot + 1);
					int targetItemId = targetItem != null ? targetItem.itemId : -1;

					steps.add(new OrderStep(
						idealItem.itemId,
						idealItem.name,
						targetSlot,
						"Insert " + idealItem.name + " before " + targetItemName,
						subCatName,
						targetItemId
					));

					// Simulate the insert in our working list
					BankItem removed = currentItems.remove(currentSlot);
					currentItems.add(targetSlot, removed);
				}
			}

			orderSteps = steps;
			currentOrderStep = 0;
			orderingActive = !steps.isEmpty();

			SwingUtilities.invokeLater(() -> panel.updateOrderingState());

			log.info("Ordering: {} steps to reorder {} tab", steps.size(), tabCategory.getDisplayName());
		});
	}

	public void advanceOrderStep()
	{
		if (currentOrderStep < orderSteps.size() - 1)
		{
			currentOrderStep++;
			SwingUtilities.invokeLater(() -> panel.updateOrderingState());
		}
		else
		{
			stopOrdering();
		}
	}

	public void stopOrdering()
	{
		orderingActive = false;
		orderSteps.clear();
		currentOrderStep = 0;
		SwingUtilities.invokeLater(() -> panel.updateOrderingState());
	}

	// === Helper classes ===

	public static class BankItem
	{
		public final int itemId;
		public final String name;
		public final int slot;

		public BankItem(int itemId, String name, int slot)
		{
			this.itemId = itemId;
			this.name = name;
			this.slot = slot;
		}
	}

	public static class OrderStep
	{
		public final int itemId;
		public final String itemName;
		public final int targetSlot;
		public final String instruction;
		public final String subCategory;
		public final int targetItemId; // The item to insert before

		public OrderStep(int itemId, String itemName, int targetSlot, String instruction, String subCategory, int targetItemId)
		{
			this.itemId = itemId;
			this.itemName = itemName;
			this.targetSlot = targetSlot;
			this.instruction = instruction;
			this.subCategory = subCategory;
			this.targetItemId = targetItemId;
		}
	}
}
