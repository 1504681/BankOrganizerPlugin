package com.bankorganizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class BankOrganizerPanel extends PluginPanel
{
	private final BankOrganizerPlugin plugin;
	private final JPanel resultsPanel;
	private final JPanel orderingPanel;
	private final JLabel orderingStepLabel;
	private final JLabel orderingSubCatLabel;
	private final JLabel orderingProgressLabel;
	private final JButton categorizeButton;
	private final JButton subCatToggle;
	private final JButton orderButton;
	private final JButton nextStepButton;
	private final JButton stopOrderButton;
	private final List<JButton> filterButtons = new ArrayList<>();

	private static final Color ACCENT = new Color(0, 180, 255);
	private static final Color BTN_ACTIVE = new Color(40, 80, 40);
	private static final Color BTN_DANGER = new Color(100, 40, 40);

	public BankOrganizerPanel(BankOrganizerPlugin plugin)
	{
		super(false);
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Scrollable main content
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

		// === HEADER ===
		JLabel title = new JLabel("Bank Organizer");
		title.setForeground(ACCENT);
		title.setFont(FontManager.getRunescapeBoldFont().deriveFont(18f));
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(title);
		mainPanel.add(Box.createVerticalStrut(2));

		JLabel presetLabel = new JLabel("Default Layout");
		presetLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		presetLabel.setFont(FontManager.getRunescapeSmallFont());
		presetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(presetLabel);
		mainPanel.add(Box.createVerticalStrut(8));

		// === QUICK ACTIONS (2-column grid) ===
		JPanel actionsGrid = new JPanel(new GridLayout(0, 2, 4, 4));
		actionsGrid.setBackground(ColorScheme.DARK_GRAY_COLOR);
		actionsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton overlayToggle = makeButton("Overlays", false);
		overlayToggle.addActionListener(e ->
		{
			boolean enabled = !plugin.isOverlayEnabled();
			plugin.setOverlayEnabled(enabled);
			overlayToggle.setText(enabled ? "Overlays ON" : "Overlays");
			overlayToggle.setBackground(enabled ? BTN_ACTIVE : null);
		});
		actionsGrid.add(overlayToggle);

		JButton scanButton = makeButton("Scan Tab", false);
		scanButton.addActionListener(e -> plugin.scanCurrentTab());
		actionsGrid.add(scanButton);

		JButton highlightUntaggedBtn = makeButton("Untagged", false);
		highlightUntaggedBtn.addActionListener(e ->
		{
			boolean hl = !plugin.isHighlightUntagged();
			plugin.setHighlightUntagged(hl);
			highlightUntaggedBtn.setText(hl ? "Untagged ON" : "Untagged");
			highlightUntaggedBtn.setBackground(hl ? new Color(100, 40, 40) : null);
		});
		actionsGrid.add(highlightUntaggedBtn);

		JButton exportButton = makeButton("Export", false);
		exportButton.addActionListener(e -> plugin.exportOverrides());
		actionsGrid.add(exportButton);

		mainPanel.add(actionsGrid);
		mainPanel.add(Box.createVerticalStrut(6));

		// === CATEGORIZE SECTION ===
		JPanel catSection = createSection("Categorize");

		categorizeButton = makeButton("Start Categorizing", true);
		categorizeButton.addActionListener(e -> toggleCategorizeMode());
		catSection.add(categorizeButton);

		subCatToggle = makeButton("Mode: Category", false);
		subCatToggle.setVisible(false);
		subCatToggle.addActionListener(e ->
		{
			boolean newMode = !plugin.isSubCategoryMode();
			plugin.setSubCategoryMode(newMode);
			subCatToggle.setText(newMode ? "Mode: Subcategory" : "Mode: Category");
		});
		catSection.add(subCatToggle);

		JButton importButton = makeButton("Import Overrides", false);
		importButton.addActionListener(e -> plugin.importOverrides());
		catSection.add(importButton);

		mainPanel.add(catSection);
		mainPanel.add(Box.createVerticalStrut(6));

		// === ORDERING SECTION ===
		JPanel orderSection = createSection("Order Items");

		orderButton = makeButton("Start Ordering", true);
		orderButton.addActionListener(e -> plugin.startOrdering());
		orderSection.add(orderButton);

		// Ordering guide (hidden by default)
		orderingPanel = new JPanel();
		orderingPanel.setLayout(new BoxLayout(orderingPanel, BoxLayout.Y_AXIS));
		orderingPanel.setBackground(new Color(30, 45, 30));
		orderingPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(80, 180, 80), 1),
			new EmptyBorder(6, 6, 6, 6)
		));
		orderingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		orderingPanel.setVisible(false);

		JLabel insertNote = new JLabel("Use Insert mode!");
		insertNote.setForeground(new Color(255, 200, 100));
		insertNote.setFont(FontManager.getRunescapeSmallFont());
		orderingPanel.add(insertNote);
		orderingPanel.add(Box.createVerticalStrut(4));

		orderingSubCatLabel = new JLabel("");
		orderingSubCatLabel.setForeground(new Color(80, 220, 80));
		orderingSubCatLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		orderingPanel.add(orderingSubCatLabel);
		orderingPanel.add(Box.createVerticalStrut(2));

		orderingStepLabel = new JLabel("");
		orderingStepLabel.setForeground(Color.WHITE);
		orderingStepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		orderingPanel.add(orderingStepLabel);
		orderingPanel.add(Box.createVerticalStrut(4));

		orderingProgressLabel = new JLabel("");
		orderingProgressLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		orderingPanel.add(orderingProgressLabel);
		orderingPanel.add(Box.createVerticalStrut(6));

		JPanel orderBtnPanel = new JPanel(new GridLayout(1, 2, 4, 0));
		orderBtnPanel.setBackground(new Color(30, 45, 30));
		orderBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		nextStepButton = makeButton("Skip", false);
		nextStepButton.addActionListener(e -> plugin.advanceOrderStep());
		orderBtnPanel.add(nextStepButton);

		stopOrderButton = makeButton("Stop", false);
		stopOrderButton.setBackground(BTN_DANGER);
		stopOrderButton.addActionListener(e -> plugin.stopOrdering());
		orderBtnPanel.add(stopOrderButton);

		orderingPanel.add(orderBtnPanel);
		orderSection.add(orderingPanel);

		mainPanel.add(orderSection);
		mainPanel.add(Box.createVerticalStrut(6));

		// === FILTER SECTION ===
		JPanel filterSection = createSection("Filter");

		JPanel filterGrid = new JPanel(new GridLayout(0, 2, 3, 3));
		filterGrid.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		filterGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

		JButton allButton = makeFilterButton("All", null);
		filterGrid.add(allButton);
		filterButtons.add(allButton);

		for (ItemCategory category : ItemCategory.values())
		{
			JButton btn = makeFilterButton(category.getDisplayName(), category);
			filterGrid.add(btn);
			filterButtons.add(btn);
		}

		filterSection.add(filterGrid);
		mainPanel.add(filterSection);
		mainPanel.add(Box.createVerticalStrut(6));

		// === RESULTS SECTION ===
		JPanel resultsSection = createSection("Scan Results");

		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		resultsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JScrollPane scrollPane = new JScrollPane(resultsPanel);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		scrollPane.setPreferredSize(new Dimension(0, 250));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		resultsSection.add(scrollPane);
		mainPanel.add(resultsSection);

		// Wrap everything in a scroll pane for the full panel
		JScrollPane mainScroll = new JScrollPane(mainPanel);
		mainScroll.setBorder(BorderFactory.createEmptyBorder());
		mainScroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainScroll.getVerticalScrollBar().setUnitIncrement(16);
		add(mainScroll, BorderLayout.CENTER);
	}

	// === UI Helpers ===

	private JPanel createSection(String title)
	{
		JPanel section = new JPanel();
		section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
		section.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		section.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.MEDIUM_GRAY_COLOR),
			new EmptyBorder(6, 6, 6, 6)
		));
		section.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel label = new JLabel(title);
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		section.add(label);
		section.add(Box.createVerticalStrut(4));

		return section;
	}

	private JButton makeButton(String text, boolean wide)
	{
		JButton btn = new JButton(text);
		btn.setFocusPainted(false);
		btn.setFont(FontManager.getRunescapeSmallFont());
		btn.setAlignmentX(Component.LEFT_ALIGNMENT);
		if (wide)
		{
			btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		}
		return btn;
	}

	private JButton makeFilterButton(String text, ItemCategory category)
	{
		JButton button = new JButton(text);
		button.setFocusPainted(false);
		button.setFont(FontManager.getRunescapeSmallFont());
		button.setMargin(new Insets(2, 4, 2, 4));
		if (category != null)
		{
			button.setForeground(plugin.getColorForCategory(category));
		}
		button.addActionListener(e -> plugin.setActiveFilter(category));
		return button;
	}

	// === State Updates ===

	private void toggleCategorizeMode()
	{
		boolean newMode = !plugin.isCategorizeMode();
		plugin.setCategorizeMode(newMode);
		if (newMode)
		{
			categorizeButton.setText("Stop Categorizing");
			categorizeButton.setBackground(BTN_DANGER);
			subCatToggle.setVisible(true);
		}
		else
		{
			categorizeButton.setText("Start Categorizing");
			categorizeButton.setBackground(null);
			subCatToggle.setVisible(false);
			plugin.setSubCategoryMode(false);
			subCatToggle.setText("Mode: Category");
		}
	}

	public void updateResults(Map<Integer, ItemCategory> misplacedItems,
							  Map<Integer, String> itemNames,
							  Map<ItemCategory, Integer> tabMappings)
	{
		resultsPanel.removeAll();

		if (misplacedItems.isEmpty())
		{
			JLabel noItems = new JLabel("All items are in the correct tab!");
			noItems.setForeground(new Color(80, 220, 80));
			noItems.setFont(FontManager.getRunescapeSmallFont());
			noItems.setBorder(new EmptyBorder(4, 4, 4, 4));
			resultsPanel.add(noItems);
		}
		else
		{
			JLabel countLabel = new JLabel(misplacedItems.size() + " misplaced items");
			countLabel.setForeground(new Color(255, 150, 150));
			countLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD));
			countLabel.setBorder(new EmptyBorder(2, 4, 4, 4));
			resultsPanel.add(countLabel);

			int shown = 0;
			for (Map.Entry<Integer, ItemCategory> entry : misplacedItems.entrySet())
			{
				if (shown >= 100)
				{
					JLabel moreLabel = new JLabel("... +" + (misplacedItems.size() - 100) + " more");
					moreLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
					moreLabel.setFont(FontManager.getRunescapeSmallFont());
					moreLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
					resultsPanel.add(moreLabel);
					break;
				}

				int itemId = entry.getKey();
				ItemCategory correctCategory = entry.getValue();
				String itemName = itemNames.getOrDefault(itemId, "Unknown");

				Integer tabNum = tabMappings.get(correctCategory);
				String tabStr = tabNum != null ? "Tab " + tabNum : "?";

				JPanel row = new JPanel(new BorderLayout());
				row.setBackground(shown % 2 == 0 ? ColorScheme.DARKER_GRAY_COLOR : new Color(35, 35, 35));
				row.setBorder(new EmptyBorder(2, 4, 2, 4));
				row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

				JLabel nameLabel = new JLabel(itemName);
				nameLabel.setForeground(plugin.getColorForCategory(correctCategory));
				nameLabel.setFont(FontManager.getRunescapeSmallFont());

				JLabel destLabel = new JLabel(tabStr);
				destLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				destLabel.setFont(FontManager.getRunescapeSmallFont());
				destLabel.setHorizontalAlignment(SwingConstants.RIGHT);

				row.add(nameLabel, BorderLayout.WEST);
				row.add(destLabel, BorderLayout.EAST);

				resultsPanel.add(row);
				shown++;
			}
		}

		resultsPanel.revalidate();
		resultsPanel.repaint();
	}

	public void updateOrderingState()
	{
		if (plugin.isOrderingActive())
		{
			orderingPanel.setVisible(true);
			List<BankOrganizerPlugin.OrderStep> steps = plugin.getOrderSteps();
			int current = plugin.getCurrentOrderStep();

			if (current < steps.size())
			{
				BankOrganizerPlugin.OrderStep step = steps.get(current);
				orderingSubCatLabel.setText("<html><b>" + step.phaseDescription + "</b></html>");
				orderingSubCatLabel.setFont(FontManager.getRunescapeBoldFont().deriveFont(15f));
				orderingSubCatLabel.setForeground(new Color(80, 220, 80));
				orderingStepLabel.setText("<html>" + step.instruction + "</html>");
				orderingStepLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(12f));
				orderingProgressLabel.setText(step.totalOutOfPlace + " remaining");
				orderingProgressLabel.setFont(FontManager.getRunescapeSmallFont().deriveFont(Font.BOLD, 11f));
			}
		}
		else
		{
			orderingPanel.setVisible(false);
			orderingStepLabel.setText("");
			orderingSubCatLabel.setText("");
			orderingProgressLabel.setText("");
		}

		revalidate();
		repaint();
	}
}
