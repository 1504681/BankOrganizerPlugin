package com.bankorganizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
	private final JButton orderButton;
	private final JButton nextStepButton;
	private final JButton stopOrderButton;
	private final List<JButton> filterButtons = new ArrayList<>();

	public BankOrganizerPanel(BankOrganizerPlugin plugin)
	{
		super(false);
		this.plugin = plugin;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Title
		JLabel title = new JLabel("Bank Organizer");
		title.setForeground(Color.WHITE);
		title.setFont(title.getFont().deriveFont(16f));
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(title);
		mainPanel.add(Box.createVerticalStrut(5));

		// Preset label
		JLabel presetLabel = new JLabel("Preset: Default Layout");
		presetLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		presetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(presetLabel);
		mainPanel.add(Box.createVerticalStrut(10));

		// === Preview toggle at top ===
		JButton previewButton = new JButton("Preview Order");
		previewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		previewButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		previewButton.addActionListener(e ->
		{
			plugin.togglePreview();
			if (plugin.isPreviewMode())
			{
				previewButton.setText("Hide Preview");
				previewButton.setBackground(new Color(60, 100, 60));
			}
			else
			{
				previewButton.setText("Preview Order");
				previewButton.setBackground(null);
			}
		});
		mainPanel.add(previewButton);
		mainPanel.add(Box.createVerticalStrut(5));

		// === Action buttons ===
		JButton scanButton = new JButton("Scan Current Tab");
		scanButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		scanButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		scanButton.addActionListener(e -> plugin.scanCurrentTab());
		mainPanel.add(scanButton);
		mainPanel.add(Box.createVerticalStrut(5));

		categorizeButton = new JButton("Start Categorizing");
		categorizeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		categorizeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		categorizeButton.addActionListener(e -> toggleCategorizeMode());
		mainPanel.add(categorizeButton);
		mainPanel.add(Box.createVerticalStrut(5));

		orderButton = new JButton("Start Ordering Items");
		orderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		orderButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		orderButton.addActionListener(e -> plugin.startOrdering());
		mainPanel.add(orderButton);
		mainPanel.add(Box.createVerticalStrut(5));

		// Export/Import overrides
		JPanel overridePanel = new JPanel(new GridBagLayout());
		overridePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		overridePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		GridBagConstraints obc = new GridBagConstraints();
		obc.fill = GridBagConstraints.HORIZONTAL;
		obc.weightx = 1.0;
		obc.insets = new Insets(0, 0, 0, 4);

		JButton exportButton = new JButton("Export Overrides");
		exportButton.setFont(exportButton.getFont().deriveFont(11f));
		exportButton.addActionListener(e -> plugin.exportOverrides());
		obc.gridx = 0;
		overridePanel.add(exportButton, obc);

		JButton importButton = new JButton("Import Overrides");
		importButton.setFont(importButton.getFont().deriveFont(11f));
		importButton.addActionListener(e -> plugin.importOverrides());
		obc.gridx = 1;
		obc.insets = new Insets(0, 0, 0, 0);
		overridePanel.add(importButton, obc);

		mainPanel.add(overridePanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// === Ordering guide panel ===
		orderingPanel = new JPanel();
		orderingPanel.setLayout(new BoxLayout(orderingPanel, BoxLayout.Y_AXIS));
		orderingPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		orderingPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(80, 180, 80), 1),
			new EmptyBorder(8, 8, 8, 8)
		));
		orderingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		orderingPanel.setVisible(false);

		JLabel orderingTitle = new JLabel("Ordering Guide");
		orderingTitle.setForeground(new Color(80, 180, 80));
		orderingTitle.setFont(orderingTitle.getFont().deriveFont(13f));
		orderingPanel.add(orderingTitle);
		orderingPanel.add(Box.createVerticalStrut(3));

		JLabel insertNote = new JLabel("(Make sure Insert mode is ON)");
		insertNote.setForeground(new Color(255, 200, 100));
		insertNote.setFont(insertNote.getFont().deriveFont(10f));
		orderingPanel.add(insertNote);
		orderingPanel.add(Box.createVerticalStrut(5));

		orderingSubCatLabel = new JLabel("");
		orderingSubCatLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		orderingPanel.add(orderingSubCatLabel);
		orderingPanel.add(Box.createVerticalStrut(3));

		orderingStepLabel = new JLabel("");
		orderingStepLabel.setForeground(Color.WHITE);
		orderingStepLabel.setFont(orderingStepLabel.getFont().deriveFont(12f));
		orderingPanel.add(orderingStepLabel);
		orderingPanel.add(Box.createVerticalStrut(5));

		orderingProgressLabel = new JLabel("");
		orderingProgressLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		orderingPanel.add(orderingProgressLabel);
		orderingPanel.add(Box.createVerticalStrut(8));

		JPanel orderBtnPanel = new JPanel(new GridBagLayout());
		orderBtnPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		orderBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		GridBagConstraints obgc = new GridBagConstraints();
		obgc.fill = GridBagConstraints.HORIZONTAL;
		obgc.weightx = 1.0;
		obgc.insets = new Insets(0, 0, 0, 4);

		nextStepButton = new JButton("Next");
		nextStepButton.addActionListener(e -> plugin.advanceOrderStep());
		obgc.gridx = 0;
		orderBtnPanel.add(nextStepButton, obgc);

		stopOrderButton = new JButton("Stop");
		stopOrderButton.addActionListener(e -> plugin.stopOrdering());
		obgc.gridx = 1;
		obgc.insets = new Insets(0, 0, 0, 0);
		orderBtnPanel.add(stopOrderButton, obgc);

		orderingPanel.add(orderBtnPanel);

		mainPanel.add(orderingPanel);
		mainPanel.add(Box.createVerticalStrut(10));

		// === Category filter section ===
		JLabel filterLabel = new JLabel("Filter by Category:");
		filterLabel.setForeground(Color.WHITE);
		filterLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(filterLabel);
		mainPanel.add(Box.createVerticalStrut(5));

		JPanel filterPanel = new JPanel(new GridBagLayout());
		filterPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.weightx = 1.0;

		JButton allButton = createFilterButton("All", null);
		gbc.gridx = 0;
		gbc.gridy = 0;
		filterPanel.add(allButton, gbc);
		filterButtons.add(allButton);

		int col = 1;
		int row = 0;
		for (ItemCategory category : ItemCategory.values())
		{
			JButton btn = createFilterButton(category.getDisplayName(), category);
			gbc.gridx = col;
			gbc.gridy = row;
			filterPanel.add(btn, gbc);
			filterButtons.add(btn);

			col++;
			if (col > 1)
			{
				col = 0;
				row++;
			}
		}

		mainPanel.add(filterPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// === Results section ===
		JLabel resultsLabel = new JLabel("Misplaced Items:");
		resultsLabel.setForeground(Color.WHITE);
		resultsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(resultsLabel);
		mainPanel.add(Box.createVerticalStrut(5));

		resultsPanel = new JPanel();
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		resultsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		resultsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JScrollPane scrollPane = new JScrollPane(resultsPanel);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		mainPanel.add(scrollPane);

		add(mainPanel, BorderLayout.CENTER);
	}

	private void toggleCategorizeMode()
	{
		boolean newMode = !plugin.isCategorizeMode();
		plugin.setCategorizeMode(newMode);
		if (newMode)
		{
			categorizeButton.setText("Stop Categorizing");
			categorizeButton.setBackground(new Color(100, 60, 60));
		}
		else
		{
			categorizeButton.setText("Start Categorizing");
			categorizeButton.setBackground(null);
		}
	}

	private JButton createFilterButton(String text, ItemCategory category)
	{
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(0, 25));
		button.setFont(button.getFont().deriveFont(11f));
		button.addActionListener(e ->
		{
			plugin.setActiveFilter(category);
		});
		return button;
	}

	public void updateResults(Map<Integer, ItemCategory> misplacedItems,
							  Map<Integer, String> itemNames,
							  Map<ItemCategory, Integer> tabMappings)
	{
		resultsPanel.removeAll();

		if (misplacedItems.isEmpty())
		{
			JLabel noItems = new JLabel("All items are in the correct tab!");
			noItems.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			noItems.setBorder(new EmptyBorder(5, 5, 5, 5));
			resultsPanel.add(noItems);
		}
		else
		{
			for (Map.Entry<Integer, ItemCategory> entry : misplacedItems.entrySet())
			{
				int itemId = entry.getKey();
				ItemCategory correctCategory = entry.getValue();
				String itemName = itemNames.getOrDefault(itemId, "Unknown");

				Integer tabNum = tabMappings.get(correctCategory);
				String tabStr = tabNum != null ? "Tab " + tabNum : "?";

				JPanel row = new JPanel(new BorderLayout());
				row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				row.setBorder(new EmptyBorder(3, 5, 3, 5));
				row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

				JLabel nameLabel = new JLabel(itemName);
				nameLabel.setForeground(correctCategory.getColor());

				JLabel destLabel = new JLabel(" -> " + tabStr + " (" + correctCategory.getDisplayName() + ")");
				destLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				destLabel.setHorizontalAlignment(SwingConstants.RIGHT);

				row.add(nameLabel, BorderLayout.WEST);
				row.add(destLabel, BorderLayout.EAST);

				resultsPanel.add(row);
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
				orderingStepLabel.setText(step.instruction);
				orderingSubCatLabel.setText("Group: " + step.subCategory);
				orderingProgressLabel.setText("Step " + (current + 1) + " / " + steps.size());
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
