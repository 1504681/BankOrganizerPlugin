# Bank Organizer

A RuneLite plugin that helps you organize your bank by categorizing items, highlighting misplaced ones, and guiding you through reordering items within tabs.

## Features

### Category Overlays
- **Show Overlays** draws colored boxes on every bank item showing which category it belongs to
- 9 built-in categories: Teleports, Combat, Potions, Food, Skilling, Materials, High Alch, Currency, Quest/Misc
- **Filter by Category** to highlight only one category at a time
- Customizable colors for each category in plugin settings
- Adjustable overlay opacity

### Item Categorization
- Items are auto-categorized by item ID, name keywords, and equipment stats
- **Right-click categorization**: toggle "Start Categorizing", then right-click any bank item to assign it to a category
- **Subcategory mode**: toggle to assign items to skill-based subcategories (Farming, Runecrafting, Woodcutting, etc.)
- **Mass categorize**: right-click a bank tab to categorize all visible items at once
- Custom regex patterns per category for advanced matching

### Smart Sorting
- **Combat tab**: items sorted by combat style (melee/ranged/mage), then by equipment slot, then by stat strength (highest first)
- **Teleports tab**: rune pouch first, then skill capes, other teleport items, runes (elemental > catalytic), jewelry (grouped by type, highest charge first), tablets
- **Potions tab**: divine variants first, then by priority (super combat > ranging > sara brew > super restore > prayer pot, etc.), highest dose first
- **Food tab**: sorted by healing amount (anglerfish > shark > monkfish, etc.)
- **Skilling tab**: grouped by skill (Farming > Runecrafting > Woodcutting > Fishing > Mining > Prayer > Agility, etc.)
- **Materials tab**: grouped by related skills (Mining/Smithing > Woodcutting/Fletching > Crafting > Farming/Herblore, etc.), then by tier

### Step-by-Step Reordering
1. Navigate to a bank tab and click **Start Ordering**
2. The plugin highlights the item to move (green "MOVE") and where to insert it (yellow "INSERT BEFORE")
3. Make sure **Insert mode** is enabled in your bank settings (not Swap)
4. Move the item as instructed - the plugin auto-detects the change and shows the next step
5. Phase display shows which group is being sorted (e.g., "Grouping Farming items")
6. Items without a subcategory are skipped automatically
7. Click **Skip** to skip a problematic item, **Stop** to cancel

### Profiles
- **Default Layout**: comes pre-loaded with 400+ item categorizations
- **New Default**: create a new profile with all default categorizations
- **New Blank**: create an empty profile for fully manual categorization
- **Export/Import**: share profiles with other players via clipboard
- Switch between profiles instantly using the dropdown

### Highlight Untagged
- Toggle **Highlight Untagged** to see red dashed boxes on Skilling and Materials items that don't have a subcategory assigned
- Useful for finding items that need manual tagging

## Getting Started

1. Install the plugin from the Plugin Hub
2. Open your bank and look for the blue grid icon in the sidebar
3. Click **Show Overlays** to see category colors on all items
4. Open plugin settings (gear icon) to configure tab mappings and colors

### Setting Up Your Tabs
1. Go to plugin settings > **Tab Mappings**
2. Assign each bank tab (1-9) to a category (e.g., Tab 1 = Teleports, Tab 2 = Combat)
3. Click **Scan Tab** while viewing a tab to see which items are misplaced

### Categorizing Items
1. Click **Start Categorizing** in the sidebar
2. Right-click any item to see category options in colored text
3. Click a category to assign that item (persists across sessions)
4. Toggle **Mode: Subcategory** to assign skill-based subcategories instead
5. Right-click a bank tab to mass-categorize all items in it
6. Click **Stop Categorizing** when done

### Reordering a Tab
1. Navigate to the tab you want to sort
2. Enable **Insert mode** in the bank settings (click the arrows icon at the bottom of the bank)
3. Click **Start Ordering** in the sidebar
4. Follow the green/yellow highlights to move items one at a time
5. The sidebar shows the current phase and remaining items
6. The plugin auto-advances when you complete each step

## Building from Source

```bash
./gradlew build
```

To run in development mode:
```bash
./gradlew run
```

## Configuration

All settings are in the RuneLite plugin configuration panel:

| Section | Settings |
|---------|----------|
| General | Show sidebar icon, tab colors, overlay opacity |
| Tab Mappings | Assign categories to bank tabs 1-9 |
| Sorting | Gear sort mode (Combat Style / Equipment Type), Teleport sort mode |
| Colors | Customize overlay color for each category |
| Custom Regex | Add regex patterns for advanced item matching |

## License

BSD 2-Clause License
