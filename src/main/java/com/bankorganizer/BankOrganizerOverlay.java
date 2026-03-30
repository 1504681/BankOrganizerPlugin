package com.bankorganizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
		Map<Integer, ItemCategory> misplacedItems = plugin.getMisplacedItems();
		ItemCategory activeFilter = plugin.getActiveFilter();

		if (misplacedItems == null || misplacedItems.isEmpty())
		{
			return null;
		}

		Widget bankItemContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
		if (bankItemContainer == null || bankItemContainer.isHidden())
		{
			return null;
		}

		Widget[] children = bankItemContainer.getDynamicChildren();
		if (children == null)
		{
			return null;
		}

		for (Map.Entry<Integer, ItemCategory> entry : misplacedItems.entrySet())
		{
			int slot = entry.getKey();
			ItemCategory correctCategory = entry.getValue();

			if (activeFilter != null && activeFilter != correctCategory)
			{
				continue;
			}

			if (slot < children.length)
			{
				Widget itemWidget = children[slot];
				if (itemWidget != null && !itemWidget.isHidden())
				{
					Rectangle bounds = itemWidget.getBounds();
					if (bounds != null && bounds.width > 0)
					{
						Color color = correctCategory.getColor();
						Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 80);
						Color borderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);

						graphics.setColor(fillColor);
						graphics.fill(bounds);
						graphics.setColor(borderColor);
						graphics.draw(bounds);
					}
				}
			}
		}

		return null;
	}
}
