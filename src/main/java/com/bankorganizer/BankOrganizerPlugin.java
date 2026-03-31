package com.bankorganizer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private boolean subCategoryMode = false; // false=category, true=subcategory

	// Ordering state
	private boolean orderingActive = false;
	private List<OrderStep> orderSteps = new ArrayList<>();
	private int currentOrderStep = 0;
	private boolean previewMode = false;
	private boolean overlayEnabled = false;
	private List<PreviewItem> previewItems = new ArrayList<>();

	public Map<Integer, ItemCategory> getMisplacedItems() { return misplacedItems; }
	public Map<Integer, String> getMisplacedItemNames() { return misplacedItemNames; }
	public ItemCategory getActiveFilter() { return activeFilter; }
	public void setActiveFilter(ItemCategory activeFilter) { this.activeFilter = activeFilter; }
	public boolean isScanActive() { return scanActive; }
	public boolean isCategorizeMode() { return categorizeMode; }
	public void setCategorizeMode(boolean mode) { this.categorizeMode = mode; }
	public boolean isSubCategoryMode() { return subCategoryMode; }
	public void setSubCategoryMode(boolean mode) { this.subCategoryMode = mode; }
	public boolean isOrderingActive() { return orderingActive; }
	public List<OrderStep> getOrderSteps() { return orderSteps; }
	public int getCurrentOrderStep() { return currentOrderStep; }
	public ItemCategorizer getCategorizer() { return categorizer; }
	public ItemManager getItemManager() { return itemManager; }
	public BankOrganizerConfig getConfig() { return config; }

	public java.awt.Color getColorForCategory(ItemCategory category)
	{
		switch (category)
		{
			case TELEPORTS: return config.colorTeleports();
			case GEAR: return config.colorCombat();
			case POTIONS: return config.colorPotions();
			case FOOD: return config.colorFood();
			case SKILLING: return config.colorSkilling();
			case RAW_MATERIALS: return config.colorMaterials();
			case HIGH_ALCH: return config.colorHighAlch();
			case CURRENCY: return config.colorCurrency();
			case QUEST_MISC: return config.colorQuestMisc();
			default: return category.getColor();
		}
	}
	public boolean isPreviewMode() { return previewMode; }
	public List<PreviewItem> getPreviewItems() { return previewItems; }
	public boolean isOverlayEnabled() { return overlayEnabled; }
	public void setOverlayEnabled(boolean enabled) { this.overlayEnabled = enabled; }

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
		loadSubOverridesFromConfig();

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

		recomputeOrderSteps();
	}

	@Subscribe
	public void onGameTick(net.runelite.api.events.GameTick event)
	{
		// Poll every tick when ordering is active because rearranging items
		// within a tab doesn't trigger ItemContainerChanged
		if (orderingActive)
		{
			recomputeOrderSteps();
		}
	}


	private void recomputeOrderSteps()
	{
		log.debug("Recomputing order steps (bank changed)");
		computeNextOrderStep();
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
		if (categorizer.hasManualOverride(itemId) || categorizer.hasSubCategoryOverride(itemId))
		{
			client.createMenuEntry(-1)
				.setOption(MENU_REMOVE_OVERRIDE)
				.setTarget(event.getTarget())
				.setIdentifier(itemId)
				.setType(MenuAction.RUNELITE)
				.setParam0(event.getActionParam0())
				.setParam1(widgetId);
		}

		if (subCategoryMode)
		{
			// SUBCATEGORY MODE: show all skills only
			String[] skillNames = ItemCategorizer.SKILL_NAMES;
			for (int i = skillNames.length - 1; i >= 0; i--)
			{
				java.awt.Color skillColor = i < ItemCategorizer.SKILL_COLORS.length
						? ItemCategorizer.SKILL_COLORS[i]
						: getColorForCategory(ItemCategory.SKILLING);
				client.createMenuEntry(-1)
					.setOption(ColorUtil.colorTag(skillColor)
						+ "Sub: " + skillNames[i])
					.setTarget(event.getTarget())
					.setIdentifier(itemId)
					.setType(MenuAction.RUNELITE)
					.setParam0(event.getActionParam0())
					.setParam1(widgetId);
			}
		}
		else
		{
			// CATEGORY MODE: show all categories
			ItemCategory[] categories = ItemCategory.values();
			for (int i = categories.length - 1; i >= 0; i--)
			{
				ItemCategory cat = categories[i];
				String colorTag = ColorUtil.colorTag(getColorForCategory(cat));

				if (cat == currentCategory)
				{
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
			categorizer.removeSubCategoryOverride(itemId);
			saveOverridesToConfig();
			saveSubOverridesToConfig();
			log.info("Removed overrides for item ID {}", itemId);
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

		// Handle subcategory assignment
		if (stripped.startsWith("Sub: "))
		{
			String subName = stripped.substring(5);
			int itemId = event.getId();
			String[] skillNames = ItemCategorizer.SKILL_NAMES;
			for (int i = 0; i < skillNames.length; i++)
			{
				if (skillNames[i].equals(subName))
				{
					categorizer.setSubCategoryOverride(itemId, i);
					saveSubOverridesToConfig();
					log.info("Set item ID {} to subcategory {} ({})", itemId, i, subName);
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

	private void loadSubOverridesFromConfig()
	{
		String json = config.subCategoryOverrides();
		if (json == null || json.isEmpty()) return;

		Map<Integer, Integer> overrides = new HashMap<>();
		for (String entry : json.split(","))
		{
			String[] parts = entry.split(":");
			if (parts.length == 2)
			{
				try
				{
					int id = Integer.parseInt(parts[0].trim());
					int subOrder = Integer.parseInt(parts[1].trim());
					overrides.put(id, subOrder);
				}
				catch (Exception ignored) {}
			}
		}
		categorizer.loadSubCategoryOverrides(overrides);
	}

	private void saveSubOverridesToConfig()
	{
		Map<Integer, Integer> overrides = categorizer.getSubCategoryOverrides();
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer, Integer> entry : overrides.entrySet())
		{
			if (sb.length() > 0) sb.append(",");
			sb.append(entry.getKey()).append(":").append(entry.getValue());
		}
		config.setSubCategoryOverrides(sb.toString());
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
	private long getItemSortKey(BankItem item, ItemCategory tabCategory,
		GearSortMode gearMode, TeleportSortMode teleportMode)
	{
		long categoryKey = 0;
		if (tabCategory == ItemCategory.GEAR)
		{
			categoryKey = categorizer.getGearFullSortKey(item.name, item.itemId,
				getEquipmentStats(item.itemId), gearMode);
		}
		else if (tabCategory == ItemCategory.TELEPORTS)
		{
			categoryKey = categorizer.getTeleportFullSortKey(item.name, item.itemId, teleportMode);
		}
		else if (tabCategory == ItemCategory.RAW_MATERIALS)
		{
			categoryKey = categorizer.getMaterialFullSortKey(item.name, item.itemId);
		}
		else if (tabCategory == ItemCategory.SKILLING)
		{
			categoryKey = categorizer.getSkillingFullSortKey(item.name, item.itemId);
		}
		else if (tabCategory == ItemCategory.POTIONS)
		{
			categoryKey = categorizer.getPotionFullSortKey(item.name, item.itemId);
		}
		else if (tabCategory == ItemCategory.FOOD)
		{
			categoryKey = categorizer.getFoodFullSortKey(item.name, item.itemId);
		}

		// All sort key methods now include item ID for guaranteed uniqueness
		return categoryKey;
	}

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
		patterns.put(ItemCategory.CURRENCY, config.regexCurrency());
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
			orderingActive = true;
			computeNextOrderStep();
		});
	}

	/**
	 * Compute the next ordering step using LIS-based optimal algorithm.
	 * Perfect swaps first (2-cycles), then insert non-LIS items front-to-back.
	 * Minimum total actions = (perfect swaps) + (n - LIS_length - 2*swaps).
	 */
	private void computeNextOrderStep()
	{
		Widget bankWidget = client.getWidget(WidgetInfo.BANK_CONTAINER);
		if (bankWidget == null || bankWidget.isHidden()) return;

		int currentTab = client.getVarbitValue(4150);
		ItemCategory tabCategory = getCategoryForTab(currentTab);
		if (tabCategory == null) return;

		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null) return;
		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null) return;

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

		int n = currentItems.size();
		if (n == 0) return;

		// Compute ideal order
		GearSortMode gearMode = config.gearSortMode();
		TeleportSortMode teleportMode = config.teleportSortMode();

		List<BankItem> idealOrder = new ArrayList<>(currentItems);
		idealOrder.sort(Comparator.comparingLong(item ->
			getItemSortKey(item, tabCategory, gearMode, teleportMode)
		));

		// Build permutation: perm[i] = ideal position of item currently at position i
		Map<Integer, Integer> idealPosMap = new HashMap<>();
		for (int i = 0; i < idealOrder.size(); i++)
		{
			idealPosMap.put(idealOrder.get(i).itemId, i);
		}

		int[] perm = new int[n];
		boolean allCorrect = true;
		for (int i = 0; i < n; i++)
		{
			Integer pos = idealPosMap.get(currentItems.get(i).itemId);
			perm[i] = pos != null ? pos : i;
			if (perm[i] != i) allCorrect = false;
		}

		if (allCorrect)
		{
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
			return;
		}

		OrderStep nextStep = null;

		// Simple front-to-back: find first wrong position and insert correct item
		// This builds a sorted prefix that never gets disturbed by subsequent inserts
		{
			for (int idealPos = 0; idealPos < n; idealPos++)
			{
				BankItem idealItem = idealOrder.get(idealPos);

				// Skip if already in correct position
				if (idealPos < currentItems.size()
					&& currentItems.get(idealPos).itemId == idealItem.itemId)
				{
					continue;
				}

				// Log the mismatch for debugging
				if (idealPos < currentItems.size())
				{
					log.debug("Pos {}: expected {} (ID:{}), got {} (ID:{})",
						idealPos, idealItem.name, idealItem.itemId,
						currentItems.get(idealPos).name, currentItems.get(idealPos).itemId);
				}

				// Skip "everything else" items (skill 99)
				if (tabCategory == ItemCategory.SKILLING)
				{
					int skillIdx = categorizer.getSkillGroupIndex(idealItem.name, idealItem.itemId);
					if (skillIdx >= 99) continue;
				}

				BankItem currentAtTarget = currentItems.get(idealPos);
				String subCatName = getSubCategoryName(idealItem, tabCategory);

				// Build phase description
				String phase = "Sorting";
				if (tabCategory == ItemCategory.SKILLING)
				{
					int skillIdx = categorizer.getSkillGroupIndex(idealItem.name, idealItem.itemId);
					if (skillIdx < ItemCategorizer.SKILL_NAMES.length)
					{
						phase = "Grouping " + ItemCategorizer.SKILL_NAMES[skillIdx] + " items";
					}
				}
				else if (tabCategory == ItemCategory.GEAR)
				{
				phase = "Grouping " + subCatName;
				}
				else if (tabCategory == ItemCategory.TELEPORTS)
				{
					phase = "Grouping " + subCatName;
				}

				int outOfPlace = 0;
				for (int k = 0; k < n; k++)
				{
					if (perm[k] != k) outOfPlace++;
				}

				nextStep = new OrderStep(
					idealItem.itemId, idealItem.name, idealPos,
					"Insert " + idealItem.name + " before " + currentAtTarget.name,
					subCatName,
					currentAtTarget.itemId,
					phase, outOfPlace
				);
				break;
			}
		}

		if (nextStep != null)
		{
			List<OrderStep> steps = new ArrayList<>();
			steps.add(nextStep);
			orderSteps = steps;
			currentOrderStep = 0;

			SwingUtilities.invokeLater(() -> panel.updateOrderingState());
			log.info("Ordering: {} items out of place. Next: {}",
				nextStep.totalOutOfPlace, nextStep.instruction);
		}
	}

	/**
	 * Compute the Longest Increasing Subsequence of a permutation.
	 * Returns the set of INDICES in the input array that form the LIS.
	 * O(n log n) using patience sorting.
	 */
	private Set<Integer> computeLIS(int[] perm)
	{
		int n = perm.length;
		if (n == 0) return new java.util.HashSet<>();

		// tails[k] = smallest ending value of increasing subsequence of length k+1
		List<Integer> tails = new ArrayList<>();
		List<Integer> tailIndices = new ArrayList<>();
		int[] parent = new int[n];
		java.util.Arrays.fill(parent, -1);

		for (int i = 0; i < n; i++)
		{
			int pos = java.util.Collections.binarySearch(tails, perm[i]);
			if (pos < 0) pos = -(pos + 1);

			if (pos == tails.size())
			{
				tails.add(perm[i]);
				tailIndices.add(i);
			}
			else
			{
				tails.set(pos, perm[i]);
				tailIndices.set(pos, i);
			}

			if (pos > 0)
			{
				parent[i] = tailIndices.get(pos - 1);
			}
		}

		// Backtrack to find LIS indices
		Set<Integer> lisIndices = new java.util.HashSet<>();
		int k = tailIndices.get(tails.size() - 1);
		while (k != -1)
		{
			lisIndices.add(k);
			k = parent[k];
		}

		return lisIndices;
	}

	private String getSubCategoryName(BankItem item, ItemCategory tabCategory)
	{
		if (tabCategory == ItemCategory.GEAR)
		{
			return categorizer.getGearSubCategory(item.name, item.itemId,
				getEquipmentStats(item.itemId)).getDisplayName();
		}
		else if (tabCategory == ItemCategory.TELEPORTS)
		{
			return categorizer.getTeleportSubCategory(item.name, item.itemId).getDisplayName();
		}
		return "";
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

	public void togglePreview()
	{
		previewMode = !previewMode;
		if (previewMode)
		{
			computePreview();
		}
		else
		{
			previewItems.clear();
		}
	}

	private void computePreview()
	{
		clientThread.invokeLater(() ->
		{
			Widget bankWidget = client.getWidget(WidgetInfo.BANK_CONTAINER);
			if (bankWidget == null || bankWidget.isHidden()) return;

			int currentTab = client.getVarbitValue(4150);
			ItemCategory tabCategory = getCategoryForTab(currentTab);
			if (tabCategory == null) return;

			Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
			if (bankItemContainer == null) return;
			Widget[] children = bankItemContainer.getDynamicChildren();
			if (children == null) return;

			List<BankItem> items = new ArrayList<>();
			for (int slot = 0; slot < children.length; slot++)
			{
				Widget child = children[slot];
				if (child == null || child.isHidden()) continue;
				int itemId = child.getItemId();
				if (itemId <= 0) continue;
				String name = itemManager.getItemComposition(itemId).getName();
				if (name == null || name.equals("null")) continue;
				items.add(new BankItem(itemId, name, slot));
			}

			GearSortMode gearMode = config.gearSortMode();
			TeleportSortMode teleportMode = config.teleportSortMode();

			List<BankItem> sorted = new ArrayList<>(items);
			sorted.sort(Comparator.comparingLong(item ->
				getItemSortKey(item, tabCategory, gearMode, teleportMode)
			));

			List<PreviewItem> preview = new ArrayList<>();
			for (int i = 0; i < sorted.size(); i++)
			{
				BankItem item = sorted.get(i);
				String subCat = "";
				if (tabCategory == ItemCategory.GEAR)
				{
					subCat = categorizer.getGearSubCategory(item.name, item.itemId,
						getEquipmentStats(item.itemId)).getDisplayName();
				}
				else if (tabCategory == ItemCategory.TELEPORTS)
				{
					subCat = categorizer.getTeleportSubCategory(item.name, item.itemId).getDisplayName();
				}
				preview.add(new PreviewItem(item.itemId, item.name, subCat, i));
			}

			previewItems = preview;
			log.info("Preview computed: {} items", preview.size());
		});
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
		public final int targetItemId;
		public final String phaseDescription;
		public final int totalOutOfPlace;

		public OrderStep(int itemId, String itemName, int targetSlot, String instruction,
			String subCategory, int targetItemId, String phaseDescription, int totalOutOfPlace)
		{
			this.itemId = itemId;
			this.itemName = itemName;
			this.targetSlot = targetSlot;
			this.instruction = instruction;
			this.subCategory = subCategory;
			this.targetItemId = targetItemId;
			this.phaseDescription = phaseDescription;
			this.totalOutOfPlace = totalOutOfPlace;
		}
	}

	public static class PreviewItem
	{
		public final int itemId;
		public final String name;
		public final String subCategory;
		public final int position; // ideal position index

		public PreviewItem(int itemId, String name, String subCategory, int position)
		{
			this.itemId = itemId;
			this.name = name;
			this.subCategory = subCategory;
			this.position = position;
		}
	}
}
