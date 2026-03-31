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
		drawTabColors(graphics);

		if (plugin.isOrderingActive())
		{
			drawOrderingHighlight(graphics);
		}
		else if (plugin.isScanActive())
		{
			drawMisplacedItems(graphics);
		}

		return null;
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

			Color color = category.getColor();
			Color barColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 160);

			int barHeight = 3;
			graphics.setColor(barColor);
			graphics.fillRect(tabBounds.x, tabBounds.y + tabBounds.height - barHeight,
				tabBounds.width, barHeight);
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

			Color color = correctCategory.getColor();
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
				// Highlight the item to move with a pulsing green border
				Color highlight = new Color(0, 255, 100, 180);
				Color fill = new Color(0, 255, 100, 50);
				graphics.setColor(fill);
				graphics.fill(bounds);
				graphics.setColor(highlight);
				graphics.setStroke(new BasicStroke(2));
				graphics.draw(bounds);

				// Draw label above
				Font oldFont = graphics.getFont();
				graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 10f));
				graphics.setColor(Color.WHITE);
				String label = "MOVE THIS";
				int textX = bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(label)) / 2;
				int textY = bounds.y - 3;
				graphics.drawString(label, textX, textY);
				graphics.setFont(oldFont);
			}
		}

		// Also highlight target position
		if (step.targetSlot < children.length)
		{
			Widget targetChild = children[step.targetSlot];
			if (targetChild != null && !targetChild.isHidden())
			{
				Rectangle targetBounds = targetChild.getBounds();
				if (targetBounds != null && targetBounds.width > 0
					&& (containerBounds == null || containerBounds.contains(targetBounds)))
				{
					Color targetColor = new Color(255, 255, 0, 120);
					graphics.setColor(targetColor);
					graphics.setStroke(new BasicStroke(2));
					graphics.draw(targetBounds);

					Font oldFont = graphics.getFont();
					graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 10f));
					graphics.setColor(new Color(255, 255, 100));
					String label = "INSERT HERE";
					int textX = targetBounds.x + (targetBounds.width - graphics.getFontMetrics().stringWidth(label)) / 2;
					int textY = targetBounds.y - 3;
					graphics.drawString(label, textX, textY);
					graphics.setFont(oldFont);
				}
			}
		}

		graphics.setStroke(oldStroke);
	}
}
