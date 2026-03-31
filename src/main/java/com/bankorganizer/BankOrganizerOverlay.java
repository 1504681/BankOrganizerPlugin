package com.bankorganizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class BankOrganizerOverlay extends Overlay
{
	// Bank tab widget IDs (WidgetInfo for tabs 1-9)
	private static final int BANK_TAB_CONTAINER_GROUP = 12;
	private static final int BANK_TAB_CONTAINER_CHILD = 42;

	private final Client client;
	private final BankOrganizerPlugin plugin;

	@Inject
	public BankOrganizerOverlay(Client client, BankOrganizerPlugin plugin)
	{
		super(plugin);
		this.client = client;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getConfig().showTabColors())
		{
			drawTabColors(graphics);
		}

		// Overlay toggle always takes priority — draws on all items
		if (plugin.isOverlayEnabled())
		{
			drawAllItemCategories(graphics);
		}

		if (plugin.isOrderingActive())
		{
			drawOrderingHighlight(graphics);
		}
		else if (!plugin.isOverlayEnabled() && plugin.isScanActive())
		{
			drawMisplacedItems(graphics);
		}

		// Highlight items without subcategory if toggle is on
		if (plugin.isHighlightUntagged())
		{
			drawUntaggedItems(graphics);
		}

		return null;
	}

	private void drawUntaggedItems(Graphics2D graphics)
	{
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden()) return;
		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null) return;
		Rectangle containerBounds = bankItemContainer.getBounds();

		for (Widget child : children)
		{
			if (child == null || child.isHidden()) continue;
			int itemId = child.getItemId();
			if (itemId <= 0) continue;

			Rectangle bounds = child.getBounds();
			if (bounds == null || bounds.width <= 0) continue;
			if (containerBounds != null && !containerBounds.contains(bounds)) continue;

			String itemName = plugin.getItemManager().getItemComposition(itemId).getName();
			if (itemName == null || itemName.equals("null")) continue;

			ItemCategory cat = plugin.getCategorizer().categorize(itemName, itemId);
			boolean untagged = false;

			if (cat == ItemCategory.SKILLING)
			{
				untagged = plugin.getCategorizer().getSkillGroupIndex(itemName, itemId) >= 99;
			}
			else if (cat == ItemCategory.RAW_MATERIALS)
			{
				untagged = plugin.getCategorizer().getMaterialGroupIndex(itemName, itemId) >= 99;
			}

			if (untagged)
			{
				Color fill = new Color(255, 50, 50, 40);
				Color border = new Color(255, 50, 50, 180);
				graphics.setColor(fill);
				graphics.fill(bounds);
				graphics.setColor(border);
				graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 10, new float[]{3, 3}, 0));
				graphics.draw(bounds);
				graphics.setStroke(new BasicStroke(1));
			}
		}
	}

	private void drawTabColors(Graphics2D graphics)
	{
		Widget tabContainer = client.getWidget(WidgetInfo.BANK_TAB_CONTAINER);
		if (tabContainer == null || tabContainer.isHidden())
		{
			return;
		}

		Widget[] tabs = tabContainer.getDynamicChildren();
		if (tabs == null)
		{
			tabs = tabContainer.getStaticChildren();
		}
		if (tabs == null)
		{
			return;
		}

		// Tab container children: index 0 is usually "all" tab, indices 1-9 are tabs 1-9
		for (int i = 0; i < tabs.length && i <= 9; i++)
		{
			if (i == 0)
			{
				continue; // Skip "all items" tab
			}

			ItemCategory category = plugin.getCategoryForTab(i);
			if (category == null)
			{
				continue;
			}

			Widget tabWidget = tabs[i];
			if (tabWidget == null || tabWidget.isHidden())
			{
				continue;
			}

			Rectangle tabBounds = tabWidget.getBounds();
			if (tabBounds == null || tabBounds.width <= 0)
			{
				continue;
			}

			Color color = plugin.getColorForCategory(category);
			Color barColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 160);

			int barHeight = 3;
			graphics.setColor(barColor);
			graphics.fillRect(tabBounds.x, tabBounds.y + tabBounds.height - barHeight,
				tabBounds.width, barHeight);
		}
	}

	private void drawAllItemCategories(Graphics2D graphics)
	{
		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden())
		{
			return;
		}

		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null)
		{
			return;
		}

		Rectangle containerBounds = bankItemContainer.getBounds();
		ItemCategory activeFilter = plugin.getActiveFilter();

		for (Widget child : children)
		{
			if (child == null || child.isHidden()) continue;
			int itemId = child.getItemId();
			if (itemId <= 0) continue;

			Rectangle bounds = child.getBounds();
			if (bounds == null || bounds.width <= 0) continue;
			if (containerBounds != null && !containerBounds.contains(bounds)) continue;

			String itemName = plugin.getItemManager().getItemComposition(itemId).getName();
			if (itemName == null || itemName.equals("null")) continue;

			ItemCategory category = plugin.getCategorizer().categorize(itemName, itemId);

			if (activeFilter != null && activeFilter != category)
			{
				continue;
			}

			Color color = plugin.getColorForCategory(category);
			int opacity = Math.max(0, Math.min(100, plugin.getConfig().overlayOpacity()));
			int fillAlpha = (int)(opacity * 2.55);
			int borderAlpha = Math.min(255, fillAlpha + 120);
			Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha);
			Color borderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), borderAlpha);

			graphics.setColor(fillColor);
			graphics.fill(bounds);
			graphics.setColor(borderColor);
			graphics.draw(bounds);
		}
	}

	private void drawMisplacedItems(Graphics2D graphics)
	{
		Map<Integer, ItemCategory> misplacedItems = plugin.getMisplacedItems();
		ItemCategory activeFilter = plugin.getActiveFilter();

		if (misplacedItems == null || misplacedItems.isEmpty())
		{
			return;
		}

		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden())
		{
			return;
		}

		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null)
		{
			return;
		}

		Rectangle containerBounds = bankItemContainer.getBounds();

		// Check each visible widget slot against misplaced item IDs
		for (Widget child : children)
		{
			if (child == null || child.isHidden())
			{
				continue;
			}

			int itemId = child.getItemId();
			if (itemId <= 0)
			{
				continue;
			}

			ItemCategory correctCategory = misplacedItems.get(itemId);
			if (correctCategory == null)
			{
				continue;
			}

			if (activeFilter != null && activeFilter != correctCategory)
			{
				continue;
			}

			Rectangle bounds = child.getBounds();
			if (bounds == null || bounds.width <= 0)
			{
				continue;
			}

			// Only draw if within the visible bank scroll area
			if (containerBounds != null && !containerBounds.contains(bounds))
			{
				continue;
			}

			Color color = plugin.getColorForCategory(correctCategory);
			Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 80);
			Color borderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);

			graphics.setColor(fillColor);
			graphics.fill(bounds);
			graphics.setColor(borderColor);
			graphics.draw(bounds);
		}
	}

	private void drawOrderingHighlight(Graphics2D graphics)
	{
		List<BankOrganizerPlugin.OrderStep> steps = plugin.getOrderSteps();
		int currentStep = plugin.getCurrentOrderStep();
		if (currentStep >= steps.size())
		{
			return;
		}

		BankOrganizerPlugin.OrderStep step = steps.get(currentStep);

		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden())
		{
			return;
		}

		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null)
		{
			return;
		}

		Rectangle containerBounds = bankItemContainer.getBounds();
		Stroke oldStroke = graphics.getStroke();

		for (Widget child : children)
		{
			if (child == null || child.isHidden()) continue;
			int itemId = child.getItemId();
			if (itemId <= 0) continue;

			Rectangle bounds = child.getBounds();
			if (bounds == null || bounds.width <= 0) continue;
			if (containerBounds != null && !containerBounds.contains(bounds)) continue;

			if (itemId == step.itemId)
			{
				Color highlight = false
					? new Color(0, 200, 255, 180)   // Cyan for swap
					: new Color(0, 255, 100, 180);   // Green for insert
				Color fill = false
					? new Color(0, 200, 255, 50)
					: new Color(0, 255, 100, 50);
				graphics.setColor(fill);
				graphics.fill(bounds);
				graphics.setColor(highlight);
				graphics.setStroke(new BasicStroke(2));
				graphics.draw(bounds);

				Font oldFont = graphics.getFont();
				graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 10f));
				graphics.setColor(Color.WHITE);
				String label = false ? "SWAP" : "MOVE";
				int textX = bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(label)) / 2;
				int textY = bounds.y - 3;
				graphics.drawString(label, textX, textY);
				graphics.setFont(oldFont);
			}
		}

		// Highlight the destination item (the item to insert before) by item ID
		if (step.targetItemId > 0)
		{
			for (Widget targetChild : children)
			{
				if (targetChild == null || targetChild.isHidden()) continue;
				if (targetChild.getItemId() != step.targetItemId) continue;

				Rectangle targetBounds = targetChild.getBounds();
				if (targetBounds == null || targetBounds.width <= 0) continue;
				if (containerBounds != null && !containerBounds.contains(targetBounds)) continue;

				Color targetFill = false
					? new Color(0, 200, 255, 50)   // Cyan for swap target too
					: new Color(255, 255, 0, 50);
				Color targetBorder = false
					? new Color(0, 200, 255, 200)
					: new Color(255, 255, 0, 200);
				graphics.setColor(targetFill);
				graphics.fill(targetBounds);
				graphics.setColor(targetBorder);
				graphics.setStroke(new BasicStroke(2));
				graphics.draw(targetBounds);

				Font oldFont = graphics.getFont();
				graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 10f));
				graphics.setColor(false ? new Color(100, 220, 255) : new Color(255, 255, 100));
				String label = false ? "SWAP" : "INSERT BEFORE";
				int textX = targetBounds.x + (targetBounds.width - graphics.getFontMetrics().stringWidth(label)) / 2;
				int textY = targetBounds.y - 3;
				graphics.drawString(label, textX, textY);
				graphics.setFont(oldFont);
				break;
			}
		}

		graphics.setStroke(oldStroke);
	}

	private void drawPreview(Graphics2D graphics)
	{
		java.util.List<BankOrganizerPlugin.PreviewItem> previewItems = plugin.getPreviewItems();
		if (previewItems == null || previewItems.isEmpty())
		{
			return;
		}

		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden())
		{
			return;
		}

		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null)
		{
			return;
		}

		Rectangle containerBounds = bankItemContainer.getBounds();

		// Build a map of itemId -> preview position
		java.util.Map<Integer, BankOrganizerPlugin.PreviewItem> previewMap = new java.util.HashMap<>();
		for (BankOrganizerPlugin.PreviewItem item : previewItems)
		{
			previewMap.put(item.itemId, item);
		}

		// Find each item's current slot index
		int slotIndex = 0;
		for (Widget child : children)
		{
			if (child == null || child.isHidden())
			{
				slotIndex++;
				continue;
			}

			int itemId = child.getItemId();
			if (itemId <= 0)
			{
				slotIndex++;
				continue;
			}

			BankOrganizerPlugin.PreviewItem preview = previewMap.get(itemId);
			if (preview == null)
			{
				slotIndex++;
				continue;
			}

			Rectangle bounds = child.getBounds();
			if (bounds == null || bounds.width <= 0)
			{
				slotIndex++;
				continue;
			}
			if (containerBounds != null && !containerBounds.contains(bounds))
			{
				slotIndex++;
				continue;
			}

			boolean inPlace = (preview.position == slotIndex);

			// Draw position number and color based on whether it's in the right spot
			Color bgColor = inPlace
				? new Color(0, 200, 0, 60)   // Green = correct
				: new Color(255, 100, 0, 60); // Orange = needs moving

			Color borderColor = inPlace
				? new Color(0, 200, 0, 150)
				: new Color(255, 100, 0, 150);

			graphics.setColor(bgColor);
			graphics.fill(bounds);
			graphics.setColor(borderColor);
			graphics.draw(bounds);

			// Draw position number
			Font oldFont = graphics.getFont();
			graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 9f));
			graphics.setColor(Color.WHITE);
			String posLabel = String.valueOf(preview.position + 1);
			int textX = bounds.x + 2;
			int textY = bounds.y + 10;
			graphics.drawString(posLabel, textX, textY);
			graphics.setFont(oldFont);

			slotIndex++;
		}
	}
}
