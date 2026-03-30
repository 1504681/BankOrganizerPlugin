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
	private final List<JButton> filterButtons = new ArrayList<>();
	private ItemCategory activeFilterSelection = null;

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
		mainPanel.add(Box.createVerticalStrut(15));

		// Scan button
		JButton scanButton = new JButton("Scan Current Tab");
		scanButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		scanButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		scanButton.addActionListener(e -> plugin.scanCurrentTab());
		mainPanel.add(scanButton);
		mainPanel.add(Box.createVerticalStrut(10));

		// Category filter section
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

		// "All" button
		JButton allButton = createFilterButton("All", null);
		gbc.gridx = 0;
		gbc.gridy = 0;
		filterPanel.add(allButton, gbc);
		filterButtons.add(allButton);

		// Category buttons in 2-column grid
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

		// Results section
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

	private JButton createFilterButton(String text, ItemCategory category)
	{
		JButton button = new JButton(text);
		button.setPreferredSize(new Dimension(0, 25));
		button.setFont(button.getFont().deriveFont(11f));
		button.addActionListener(e ->
		{
			activeFilterSelection = category;
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
				int slot = entry.getKey();
				ItemCategory correctCategory = entry.getValue();
				String itemName = itemNames.getOrDefault(slot, "Unknown");

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
}
