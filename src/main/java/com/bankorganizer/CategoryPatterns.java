package com.bankorganizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;

/** Draws the per-category patterns (stripes, crosshatch, dots) for item boxes, tab bars and panel swatches. */
final class CategoryPatterns
{
	/** Width of the patterned edge around an item box; the centre stays clear so the item is readable. */
	static final int EDGE_BAND = 4;
	static final int TAB_BAR_HEIGHT = 4;

	private CategoryPatterns()
	{
	}

	/** Pattern across the whole of r, clipped to r. SOLID draws nothing. */
	static void fill(Graphics2D g, Rectangle r, ItemCategory.Pattern pattern, Color color)
	{
		if (pattern == ItemCategory.Pattern.SOLID) return;
		Shape oldClip = g.getClip();
		g.clip(r);
		g.setColor(color);
		if (pattern == ItemCategory.Pattern.DOTS)
		{
			for (int y = r.y + 1; y < r.y + r.height; y += 3)
			{
				int offset = ((y - r.y) / 3) % 2;
				for (int x = r.x + 1 + offset; x < r.x + r.width; x += 3)
				{
					g.fillRect(x, y, 1, 1);
				}
			}
		}
		else
		{
			int h = r.height;
			int spacing = pattern == ItemCategory.Pattern.CROSSHATCH ? 6 : 4;
			for (int d = -h; d < r.width + h; d += spacing)
			{
				g.drawLine(r.x + d, r.y + h, r.x + d + h, r.y); // "/"
				if (pattern == ItemCategory.Pattern.CROSSHATCH)
				{
					g.drawLine(r.x + d, r.y, r.x + d + h, r.y + h); // "\"
				}
			}
		}
		g.setClip(oldClip);
	}

	/** Pattern in a band along the inside edge of an item box only. */
	static void edgeBand(Graphics2D g, Rectangle bounds, ItemCategory.Pattern pattern, Color color)
	{
		if (pattern == ItemCategory.Pattern.SOLID) return;
		Area ring = new Area(bounds);
		ring.subtract(new Area(new Rectangle(bounds.x + EDGE_BAND, bounds.y + EDGE_BAND,
			bounds.width - 2 * EDGE_BAND, bounds.height - 2 * EDGE_BAND)));
		Shape oldClip = g.getClip();
		g.clip(ring);
		fill(g, bounds, pattern, color);
		g.setClip(oldClip);
	}

	/** Category bar along the bottom of a bank tab: solid, or a faint base with the pattern on top. */
	static void tabBar(Graphics2D g, Rectangle tab, ItemCategory.Pattern pattern, Color color, boolean patterns)
	{
		Rectangle bar = new Rectangle(tab.x, tab.y + tab.height - TAB_BAR_HEIGHT, tab.width, TAB_BAR_HEIGHT);
		if (!patterns || pattern == ItemCategory.Pattern.SOLID)
		{
			g.setColor(withAlpha(color, 200));
			g.fill(bar);
			return;
		}
		g.setColor(withAlpha(color, 70));
		g.fill(bar);
		Color strong = withAlpha(color, 235);
		if (pattern == ItemCategory.Pattern.DOTS)
		{
			g.setColor(strong);
			for (int x = bar.x + 1; x < bar.x + bar.width; x += 4)
			{
				g.fillRect(x, bar.y + 1, 2, 2);
			}
			return;
		}
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(1.5f));
		fill(g, bar, pattern, strong);
		g.setStroke(oldStroke);
	}

	static Color withAlpha(Color c, int alpha)
	{
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
	}
}
