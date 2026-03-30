# Bank Organizer Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a RuneLite plugin that scans bank items and highlights misplaced ones based on configurable category presets with tab mappings.

**Architecture:** Package `com.bankorganizer` with 6 files: an enum defining categories with colors/keywords, a matching engine that categorizes items by ID/keyword/regex, a config interface for persistence, a sidebar panel for tab mapping and scan controls, an overlay that highlights misplaced bank items, and the main plugin class wiring everything together.

**Tech Stack:** Java 11, RuneLite Client API (OverlayPanel, PluginPanel, ConfigManager, ItemManager, Widget API), Lombok, Swing for sidebar UI.

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| Create | `src/main/java/com/bankorganizer/ItemCategory.java` | Enum: 7 categories with display names, colors, keyword lists |
| Create | `src/main/java/com/bankorganizer/ItemCategorizer.java` | Matching engine: ID map + keywords + regex -> category |
| Create | `src/main/java/com/bankorganizer/BankOrganizerConfig.java` | Config interface for tab mappings, regex, persistence |
| Create | `src/main/java/com/bankorganizer/BankOrganizerPlugin.java` | Main plugin: events, scan logic, overlay/panel lifecycle |
| Create | `src/main/java/com/bankorganizer/BankOrganizerPanel.java` | Sidebar UI: tab mapping dropdowns, scan button, results |
| Create | `src/main/java/com/bankorganizer/BankOrganizerOverlay.java` | Draws colored rectangles on misplaced bank item slots |
| Modify | `src/main/resources/runelite_plugin.json` | Update plugin class reference and metadata |
| Modify | `src/test/java/com/debugplugin/DebugPluginLauncher.java` | Move to new package, reference new plugin class |
| Delete | `src/main/java/com/debugplugin/DebugPlugin.java` | Replaced by BankOrganizerPlugin |
| Delete | `src/main/java/com/debugplugin/DebugOverlay.java` | Replaced by BankOrganizerOverlay |
| Delete | `src/test/java/com/debugplugin/DebugPluginTest.java` | Replaced by new tests |
| Create | `src/test/java/com/bankorganizer/ItemCategorizerTest.java` | Unit tests for categorization logic |
| Create | `src/test/java/com/bankorganizer/BankOrganizerPluginTest.java` | Integration tests for plugin lifecycle |
| Create | `src/test/java/com/bankorganizer/BankOrganizerLauncher.java` | Dev launcher for the new plugin |

---

### Task 1: ItemCategory Enum

**Files:**
- Create: `src/main/java/com/bankorganizer/ItemCategory.java`

- [ ] **Step 1: Create the ItemCategory enum**

```java
package com.bankorganizer;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCategory
{
    TELEPORTS("Teleports", new Color(0, 150, 255), Arrays.asList(
        "teleport", "teletab"
    )),
    GEAR("Gear", new Color(220, 50, 50), Arrays.asList(
        "helm", "full helm", "med helm", "platebody", "platelegs", "plateskirt",
        "chainbody", "shield", "kiteshield", "sq shield",
        "sword", "longsword", "scimitar", "dagger", "mace", "warhammer", "battleaxe",
        "halberd", "spear", "hasta", "crossbow", "shortbow", "longbow",
        "dart", "knife", "javelin", "thrownaxe",
        "arrow", "bolt", "whip", "godsword", "defender",
        "boots", "gloves", "vambraces", "bracelet", "amulet", "necklace", "ring",
        "coif", "hood", "hat", "body", "chaps", "d'hide"
    )),
    POTIONS("Potions", new Color(50, 200, 50), Arrays.asList(
        "potion", "brew", "restore", "mix", "overload", "prayer renewal",
        "antidote", "antifire", "antipoison", "antivenom", "battlemage",
        "bastion", "saradomin brew", "super combat", "ranging potion",
        "stamina", "energy"
    )),
    FOOD("Food", new Color(255, 165, 0), Arrays.asList(
        "shark", "lobster", "swordfish", "tuna", "salmon", "trout",
        "monkfish", "manta ray", "dark crab", "anglerfish", "karambwan",
        "bass", "pike", "shrimps", "anchovies", "sardine", "herring",
        "mackerel", "cod", "cake", "bread", "meat", "chicken",
        "wine", "stew", "potato", "mushroom", "sweetcorn",
        "cooked"
    )),
    TOOLS("Tools", new Color(255, 255, 0), Arrays.asList(
        "pickaxe", "hammer", "chisel", "knife", "saw", "tinderbox",
        "needle", "spade", "rake", "seed dibber", "secateurs",
        "watering can", "trowel", "pestle and mortar",
        "glassblowing pipe", "shears", "bucket"
    )),
    RAW_MATERIALS("Raw Materials", new Color(160, 82, 45), Arrays.asList(
        " ore", "bronze bar", "iron bar", "steel bar", "mithril bar",
        "adamantite bar", "runite bar", "gold bar", "silver bar",
        "logs", "hide", "leather", "essence",
        "seed", "grimy", "herb", "feather", "bone",
        "wool", "flax", "clay", "sand"
    )),
    QUEST_MISC("Quest/Misc", new Color(180, 100, 255), Arrays.asList());

    private final String displayName;
    private final Color color;
    private final List<String> keywords;
}
```

- [ ] **Step 2: Verify it compiles**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew compileJava 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (existing files may have errors, that's fine — just verify no syntax errors in ItemCategory.java itself by checking the error output)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/bankorganizer/ItemCategory.java
git commit -m "feat: add ItemCategory enum with colors and keyword patterns"
```

---

### Task 2: ItemCategorizer Matching Engine

**Files:**
- Create: `src/main/java/com/bankorganizer/ItemCategorizer.java`
- Create: `src/test/java/com/bankorganizer/ItemCategorizerTest.java`

- [ ] **Step 1: Write the failing tests**

```java
package com.bankorganizer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

public class ItemCategorizerTest
{
    private ItemCategorizer categorizer;

    @Before
    public void setUp()
    {
        categorizer = new ItemCategorizer();
    }

    @Test
    public void testHardcodedIdTakesPriority()
    {
        // Law rune (ID 563) should be Teleports, not Raw Materials
        // even though "rune" could match elsewhere
        assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Law rune", 563));
    }

    @Test
    public void testKeywordMatchPotion()
    {
        assertEquals(ItemCategory.POTIONS, categorizer.categorize("Super combat potion(4)", 99999));
    }

    @Test
    public void testKeywordMatchGear()
    {
        assertEquals(ItemCategory.GEAR, categorizer.categorize("Abyssal whip", 99999));
    }

    @Test
    public void testKeywordMatchFood()
    {
        assertEquals(ItemCategory.FOOD, categorizer.categorize("Shark", 99999));
    }

    @Test
    public void testKeywordMatchTools()
    {
        assertEquals(ItemCategory.TOOLS, categorizer.categorize("Rune pickaxe", 99999));
    }

    @Test
    public void testKeywordMatchRawMaterials()
    {
        assertEquals(ItemCategory.RAW_MATERIALS, categorizer.categorize("Mithril ore", 99999));
    }

    @Test
    public void testUnknownItemFallsToQuestMisc()
    {
        assertEquals(ItemCategory.QUEST_MISC, categorizer.categorize("Strange widget", 99999));
    }

    @Test
    public void testRegexPattern()
    {
        Map<ItemCategory, String> regexPatterns = new HashMap<>();
        regexPatterns.put(ItemCategory.GEAR, "dragonfire.*ward");
        categorizer.setRegexPatterns(regexPatterns);

        assertEquals(ItemCategory.GEAR, categorizer.categorize("Dragonfire ward", 99999));
    }

    @Test
    public void testAxeMatchesToolsNotGear()
    {
        // "axe" alone is in Tools keywords ("pickaxe" etc.)
        // but "battleaxe" contains "axe" too — we need to check Tools has "pickaxe"
        // and item name "Bronze pickaxe" matches tools
        assertEquals(ItemCategory.TOOLS, categorizer.categorize("Bronze pickaxe", 99999));
    }

    @Test
    public void testTeleportTablet()
    {
        assertEquals(ItemCategory.TELEPORTS, categorizer.categorize("Varrock teleport", 99999));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew test --tests "com.bankorganizer.ItemCategorizerTest" 2>&1 | tail -10`
Expected: FAIL — class ItemCategorizer does not exist

- [ ] **Step 3: Write the ItemCategorizer implementation**

```java
package com.bankorganizer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ItemCategorizer
{
    private final Map<Integer, ItemCategory> itemIdMap = new HashMap<>();
    private final Map<ItemCategory, Pattern> regexPatternCache = new EnumMap<>(ItemCategory.class);

    public ItemCategorizer()
    {
        initializeItemIdMap();
    }

    private void initializeItemIdMap()
    {
        // === TELEPORTS ===
        // Runes used for teleports
        itemIdMap.put(554, ItemCategory.TELEPORTS); // Fire rune
        itemIdMap.put(555, ItemCategory.TELEPORTS); // Water rune
        itemIdMap.put(556, ItemCategory.TELEPORTS); // Air rune
        itemIdMap.put(557, ItemCategory.TELEPORTS); // Earth rune
        itemIdMap.put(558, ItemCategory.TELEPORTS); // Mind rune
        itemIdMap.put(559, ItemCategory.TELEPORTS); // Body rune
        itemIdMap.put(560, ItemCategory.TELEPORTS); // Death rune
        itemIdMap.put(561, ItemCategory.TELEPORTS); // Nature rune
        itemIdMap.put(562, ItemCategory.TELEPORTS); // Chaos rune
        itemIdMap.put(563, ItemCategory.TELEPORTS); // Law rune
        itemIdMap.put(564, ItemCategory.TELEPORTS); // Cosmic rune
        itemIdMap.put(565, ItemCategory.TELEPORTS); // Blood rune
        itemIdMap.put(566, ItemCategory.TELEPORTS); // Soul rune
        itemIdMap.put(9075, ItemCategory.TELEPORTS); // Astral rune
        itemIdMap.put(21880, ItemCategory.TELEPORTS); // Wrath rune

        // Teleport tablets
        itemIdMap.put(8007, ItemCategory.TELEPORTS); // Varrock teleport
        itemIdMap.put(8008, ItemCategory.TELEPORTS); // Lumbridge teleport
        itemIdMap.put(8009, ItemCategory.TELEPORTS); // Falador teleport
        itemIdMap.put(8010, ItemCategory.TELEPORTS); // Camelot teleport
        itemIdMap.put(8011, ItemCategory.TELEPORTS); // Ardougne teleport
        itemIdMap.put(8012, ItemCategory.TELEPORTS); // Watchtower teleport
        itemIdMap.put(8013, ItemCategory.TELEPORTS); // House teleport

        // Rune pouch
        itemIdMap.put(12791, ItemCategory.TELEPORTS); // Rune pouch

        // === GEAR (items that might conflict with other keywords) ===
        // Barrows gloves
        itemIdMap.put(7462, ItemCategory.GEAR); // Barrows gloves
        // Fire cape
        itemIdMap.put(6570, ItemCategory.GEAR); // Fire cape
        // Infernal cape
        itemIdMap.put(21295, ItemCategory.GEAR); // Infernal cape
        // Ava's devices
        itemIdMap.put(10499, ItemCategory.GEAR); // Ava's accumulator
        itemIdMap.put(22109, ItemCategory.GEAR); // Ava's assembler

        // === POTIONS (by ID for common doses) ===
        // Super combat potions (4-1 dose)
        itemIdMap.put(12695, ItemCategory.POTIONS);
        itemIdMap.put(12697, ItemCategory.POTIONS);
        itemIdMap.put(12699, ItemCategory.POTIONS);
        itemIdMap.put(12701, ItemCategory.POTIONS);

        // Prayer potions (4-1 dose)
        itemIdMap.put(2434, ItemCategory.POTIONS);
        itemIdMap.put(139, ItemCategory.POTIONS);
        itemIdMap.put(141, ItemCategory.POTIONS);
        itemIdMap.put(143, ItemCategory.POTIONS);

        // Saradomin brews (4-1 dose)
        itemIdMap.put(6685, ItemCategory.POTIONS);
        itemIdMap.put(6687, ItemCategory.POTIONS);
        itemIdMap.put(6689, ItemCategory.POTIONS);
        itemIdMap.put(6691, ItemCategory.POTIONS);

        // === FOOD ===
        itemIdMap.put(385, ItemCategory.FOOD);  // Shark
        itemIdMap.put(379, ItemCategory.FOOD);  // Lobster
        itemIdMap.put(373, ItemCategory.FOOD);  // Swordfish
        itemIdMap.put(7946, ItemCategory.FOOD); // Monkfish
        itemIdMap.put(391, ItemCategory.FOOD);  // Manta ray
        itemIdMap.put(13441, ItemCategory.FOOD); // Anglerfish
        itemIdMap.put(11936, ItemCategory.FOOD); // Dark crab
        itemIdMap.put(3144, ItemCategory.FOOD);  // Karambwan

        // === TOOLS ===
        itemIdMap.put(1755, ItemCategory.TOOLS); // Chisel
        itemIdMap.put(2347, ItemCategory.TOOLS); // Hammer
        itemIdMap.put(590, ItemCategory.TOOLS);  // Tinderbox
        itemIdMap.put(946, ItemCategory.TOOLS);  // Knife
        itemIdMap.put(1735, ItemCategory.TOOLS); // Shears
        itemIdMap.put(952, ItemCategory.TOOLS);  // Spade

        // === RAW MATERIALS ===
        itemIdMap.put(1436, ItemCategory.RAW_MATERIALS); // Rune essence
        itemIdMap.put(7936, ItemCategory.RAW_MATERIALS); // Pure essence
        itemIdMap.put(314, ItemCategory.RAW_MATERIALS);  // Feather
        itemIdMap.put(526, ItemCategory.RAW_MATERIALS);  // Bones
    }

    /**
     * Categorize an item by ID first, then keywords, then regex, then catch-all.
     */
    public ItemCategory categorize(String itemName, int itemId)
    {
        // Priority 1: Hardcoded item ID
        ItemCategory idMatch = itemIdMap.get(itemId);
        if (idMatch != null)
        {
            return idMatch;
        }

        String lowerName = itemName.toLowerCase();

        // Priority 2: Keyword matching
        for (ItemCategory category : ItemCategory.values())
        {
            if (category == ItemCategory.QUEST_MISC)
            {
                continue;
            }
            for (String keyword : category.getKeywords())
            {
                if (lowerName.contains(keyword.toLowerCase()))
                {
                    return category;
                }
            }
        }

        // Priority 3: User-defined regex
        for (Map.Entry<ItemCategory, Pattern> entry : regexPatternCache.entrySet())
        {
            if (entry.getValue().matcher(lowerName).find())
            {
                return entry.getKey();
            }
        }

        // Priority 4: Catch-all
        return ItemCategory.QUEST_MISC;
    }

    /**
     * Update regex patterns from user config. Compiles them and caches.
     * Invalid patterns are silently ignored.
     */
    public void setRegexPatterns(Map<ItemCategory, String> patterns)
    {
        regexPatternCache.clear();
        for (Map.Entry<ItemCategory, String> entry : patterns.entrySet())
        {
            String pattern = entry.getValue();
            if (pattern != null && !pattern.trim().isEmpty())
            {
                try
                {
                    regexPatternCache.put(entry.getKey(), Pattern.compile(pattern.trim(), Pattern.CASE_INSENSITIVE));
                }
                catch (PatternSyntaxException ignored)
                {
                    // Invalid regex — skip
                }
            }
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew test --tests "com.bankorganizer.ItemCategorizerTest" 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL, all 10 tests pass

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/bankorganizer/ItemCategorizer.java src/test/java/com/bankorganizer/ItemCategorizerTest.java
git commit -m "feat: add ItemCategorizer with ID map, keyword, and regex matching"
```

---

### Task 3: BankOrganizerConfig

**Files:**
- Create: `src/main/java/com/bankorganizer/BankOrganizerConfig.java`

- [ ] **Step 1: Create the config interface**

```java
package com.bankorganizer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("bankorganizer")
public interface BankOrganizerConfig extends Config
{
    @ConfigSection(
        name = "Tab Mappings",
        description = "Assign a category to each bank tab",
        position = 0
    )
    String tabMappingSection = "tabMappings";

    @ConfigSection(
        name = "Custom Regex",
        description = "Custom regex patterns per category for additional item matching",
        position = 1
    )
    String regexSection = "customRegex";

    // --- Tab mappings (tabs 1-9) ---

    @ConfigItem(keyName = "tab1Category", name = "Tab 1", description = "Category for bank tab 1", position = 0, section = tabMappingSection)
    default ItemCategory tab1Category() { return ItemCategory.TELEPORTS; }

    @ConfigItem(keyName = "tab2Category", name = "Tab 2", description = "Category for bank tab 2", position = 1, section = tabMappingSection)
    default ItemCategory tab2Category() { return ItemCategory.GEAR; }

    @ConfigItem(keyName = "tab3Category", name = "Tab 3", description = "Category for bank tab 3", position = 2, section = tabMappingSection)
    default ItemCategory tab3Category() { return ItemCategory.POTIONS; }

    @ConfigItem(keyName = "tab4Category", name = "Tab 4", description = "Category for bank tab 4", position = 3, section = tabMappingSection)
    default ItemCategory tab4Category() { return ItemCategory.FOOD; }

    @ConfigItem(keyName = "tab5Category", name = "Tab 5", description = "Category for bank tab 5", position = 4, section = tabMappingSection)
    default ItemCategory tab5Category() { return ItemCategory.TOOLS; }

    @ConfigItem(keyName = "tab6Category", name = "Tab 6", description = "Category for bank tab 6", position = 5, section = tabMappingSection)
    default ItemCategory tab6Category() { return ItemCategory.RAW_MATERIALS; }

    @ConfigItem(keyName = "tab7Category", name = "Tab 7", description = "Category for bank tab 7", position = 6, section = tabMappingSection)
    default ItemCategory tab7Category() { return ItemCategory.QUEST_MISC; }

    @ConfigItem(keyName = "tab8Category", name = "Tab 8", description = "Category for bank tab 8", position = 7, section = tabMappingSection)
    default ItemCategory tab8Category() { return ItemCategory.QUEST_MISC; }

    @ConfigItem(keyName = "tab9Category", name = "Tab 9", description = "Category for bank tab 9", position = 8, section = tabMappingSection)
    default ItemCategory tab9Category() { return ItemCategory.QUEST_MISC; }

    // --- Custom regex per category ---

    @ConfigItem(keyName = "regexTeleports", name = "Teleports Regex", description = "Custom regex for Teleports category", position = 0, section = regexSection)
    default String regexTeleports() { return ""; }

    @ConfigItem(keyName = "regexGear", name = "Gear Regex", description = "Custom regex for Gear category", position = 1, section = regexSection)
    default String regexGear() { return ""; }

    @ConfigItem(keyName = "regexPotions", name = "Potions Regex", description = "Custom regex for Potions category", position = 2, section = regexSection)
    default String regexPotions() { return ""; }

    @ConfigItem(keyName = "regexFood", name = "Food Regex", description = "Custom regex for Food category", position = 3, section = regexSection)
    default String regexFood() { return ""; }

    @ConfigItem(keyName = "regexTools", name = "Tools Regex", description = "Custom regex for Tools category", position = 4, section = regexSection)
    default String regexTools() { return ""; }

    @ConfigItem(keyName = "regexRawMaterials", name = "Raw Materials Regex", description = "Custom regex for Raw Materials category", position = 5, section = regexSection)
    default String regexRawMaterials() { return ""; }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew compileJava 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL (or only errors from old debug files, not from BankOrganizerConfig)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/bankorganizer/BankOrganizerConfig.java
git commit -m "feat: add BankOrganizerConfig with tab mappings and regex settings"
```

---

### Task 4: BankOrganizerOverlay

**Files:**
- Create: `src/main/java/com/bankorganizer/BankOrganizerOverlay.java`

- [ ] **Step 1: Create the overlay**

This overlay draws colored rectangles over bank item slots. It uses `OverlayLayer.ABOVE_WIDGETS` and `OverlayPosition.DYNAMIC` to draw on top of the bank interface. It reads misplaced item data from the plugin.

```java
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

            // Apply category filter
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
```

- [ ] **Step 2: Verify it compiles**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew compileJava 2>&1 | tail -10`
Expected: May fail because BankOrganizerPlugin doesn't exist yet — that's expected. Verify no syntax errors in the overlay file itself.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/bankorganizer/BankOrganizerOverlay.java
git commit -m "feat: add BankOrganizerOverlay for highlighting misplaced items"
```

---

### Task 5: BankOrganizerPanel (Sidebar)

**Files:**
- Create: `src/main/java/com/bankorganizer/BankOrganizerPanel.java`

- [ ] **Step 1: Create the sidebar panel**

```java
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

        // Category buttons
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
            updateFilterButtonColors();
        });
        return button;
    }

    private void updateFilterButtonColors()
    {
        for (JButton btn : filterButtons)
        {
            btn.setBackground(null);
        }
    }

    /**
     * Update the results list with misplaced items.
     * Called from the plugin after a scan.
     *
     * @param misplacedItems map of slot index to correct category
     * @param itemNames map of slot index to item name
     * @param tabMappings map of category to assigned tab number
     */
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
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/bankorganizer/BankOrganizerPanel.java
git commit -m "feat: add BankOrganizerPanel sidebar with scan, filter, and results"
```

---

### Task 6: BankOrganizerPlugin (Main Plugin)

**Files:**
- Create: `src/main/java/com/bankorganizer/BankOrganizerPlugin.java`

- [ ] **Step 1: Create the main plugin class**

```java
package com.bankorganizer;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
    name = "Bank Organizer",
    description = "Scans bank items and highlights misplaced ones based on category presets",
    tags = {"bank", "organizer", "sort", "tab", "category"}
)
public class BankOrganizerPlugin extends Plugin
{
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

    @Getter
    private Map<Integer, ItemCategory> misplacedItems = new HashMap<>();

    @Getter
    private Map<Integer, String> misplacedItemNames = new HashMap<>();

    @Getter
    @Setter
    private ItemCategory activeFilter;

    @Override
    protected void startUp()
    {
        categorizer = new ItemCategorizer();
        updateRegexFromConfig();

        panel = new BankOrganizerPanel(this);

        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/net/runelite/client/plugins/bank/bank_icon.png");
        navButton = NavigationButton.builder()
            .tooltip("Bank Organizer")
            .icon(icon != null ? icon : new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))
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

    private void updateRegexFromConfig()
    {
        Map<ItemCategory, String> patterns = new EnumMap<>(ItemCategory.class);
        patterns.put(ItemCategory.TELEPORTS, config.regexTeleports());
        patterns.put(ItemCategory.GEAR, config.regexGear());
        patterns.put(ItemCategory.POTIONS, config.regexPotions());
        patterns.put(ItemCategory.FOOD, config.regexFood());
        patterns.put(ItemCategory.TOOLS, config.regexTools());
        patterns.put(ItemCategory.RAW_MATERIALS, config.regexRawMaterials());
        categorizer.setRegexPatterns(patterns);
    }

    /**
     * Get the category mapped to a given tab number (1-9).
     */
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

    /**
     * Build a reverse map: category -> tab number.
     */
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

    /**
     * Detect which bank tab is currently selected (1-9), or 0 for main tab.
     */
    private int getCurrentBankTab()
    {
        // The bank tab index is stored in a client varclient value.
        // Varbit 4150 tracks the current bank tab (0 = main/all, 1-9 = tabs)
        return client.getVarbitValue(4150);
    }

    /**
     * Called from the panel when user clicks "Scan Current Tab".
     */
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

            ItemContainer bankContainer = client.getItemContainer(InventoryID.BANK);
            if (bankContainer == null)
            {
                return;
            }

            Map<Integer, ItemCategory> newMisplaced = new HashMap<>();
            Map<Integer, String> newNames = new HashMap<>();

            Item[] items = bankContainer.getItems();

            // Get visible items from the bank item container widget
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

                if (expectedCategory != null && correctCategory != expectedCategory)
                {
                    newMisplaced.put(slot, correctCategory);
                    newNames.put(slot, itemName);
                }
                else if (expectedCategory == null)
                {
                    // Main tab / unmapped tab: show where each item should go
                    newMisplaced.put(slot, correctCategory);
                    newNames.put(slot, itemName);
                }
            }

            misplacedItems = newMisplaced;
            misplacedItemNames = newNames;

            Map<ItemCategory, Integer> tabMappings = getTabMappings();

            SwingUtilities.invokeLater(() ->
                panel.updateResults(newMisplaced, newNames, tabMappings));

            log.info("Scan complete: {} misplaced items found", newMisplaced.size());
        });
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew compileJava 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL (old debug plugin files may still exist and have their own compilation — focus on bankorganizer package)

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/bankorganizer/BankOrganizerPlugin.java
git commit -m "feat: add BankOrganizerPlugin with scan logic and sidebar integration"
```

---

### Task 7: Update Project Metadata and Launcher

**Files:**
- Modify: `src/main/resources/runelite_plugin.json`
- Create: `src/test/java/com/bankorganizer/BankOrganizerLauncher.java`
- Delete: `src/main/java/com/debugplugin/DebugPlugin.java`
- Delete: `src/main/java/com/debugplugin/DebugOverlay.java`
- Delete: `src/test/java/com/debugplugin/DebugPluginTest.java`
- Delete: `src/test/java/com/debugplugin/DebugPluginLauncher.java`
- Modify: `build.gradle`

- [ ] **Step 1: Update runelite_plugin.json**

```json
{
  "plugins": ["com.bankorganizer.BankOrganizerPlugin"],
  "displayName": "Bank Organizer",
  "author": "1504681",
  "description": "Scans bank items and highlights misplaced ones based on category presets",
  "tags": ["bank", "organizer", "sort", "tab", "category"],
  "version": "1.0.0"
}
```

- [ ] **Step 2: Create the new launcher**

```java
package com.bankorganizer;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankOrganizerLauncher
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(BankOrganizerPlugin.class);
        RuneLite.main(args);
    }
}
```

- [ ] **Step 3: Update build.gradle**

Change the `pluginMainClass` line:

```
def pluginMainClass = 'com.bankorganizer.BankOrganizerLauncher'
```

- [ ] **Step 4: Delete old debug plugin files**

```bash
rm src/main/java/com/debugplugin/DebugPlugin.java
rm src/main/java/com/debugplugin/DebugOverlay.java
rm src/test/java/com/debugplugin/DebugPluginTest.java
rm src/test/java/com/debugplugin/DebugPluginLauncher.java
rmdir src/main/java/com/debugplugin
rmdir src/test/java/com/debugplugin
```

- [ ] **Step 5: Full compile and test**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew clean build 2>&1 | tail -20`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: replace debug plugin with Bank Organizer plugin

Removes old DebugPlugin/DebugOverlay, updates launcher and metadata."
```

---

### Task 8: Plugin Integration Test

**Files:**
- Create: `src/test/java/com/bankorganizer/BankOrganizerPluginTest.java`

- [ ] **Step 1: Write plugin lifecycle tests**

```java
package com.bankorganizer;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BankOrganizerPluginTest
{
    @Inject
    private BankOrganizerPlugin plugin;

    @Mock
    @Bind
    private Client client;

    @Mock
    @Bind
    private ClientThread clientThread;

    @Mock
    @Bind
    private BankOrganizerConfig config;

    @Mock
    @Bind
    private OverlayManager overlayManager;

    @Mock
    @Bind
    private BankOrganizerOverlay overlay;

    @Mock
    @Bind
    private ClientToolbar clientToolbar;

    @Mock
    @Bind
    private ItemManager itemManager;

    @Mock
    @Bind
    private ConfigManager configManager;

    @Before
    public void before()
    {
        // Set up default config returns
        when(config.regexTeleports()).thenReturn("");
        when(config.regexGear()).thenReturn("");
        when(config.regexPotions()).thenReturn("");
        when(config.regexFood()).thenReturn("");
        when(config.regexTools()).thenReturn("");
        when(config.regexRawMaterials()).thenReturn("");
        when(config.tab1Category()).thenReturn(ItemCategory.TELEPORTS);

        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    }

    @Test
    public void testStartUp() throws Exception
    {
        plugin.startUp();
        verify(overlayManager).add(overlay);
        verify(clientToolbar).addNavigation(any());
    }

    @Test
    public void testShutDown() throws Exception
    {
        plugin.startUp();
        plugin.shutDown();
        verify(overlayManager).remove(overlay);
        verify(clientToolbar).removeNavigation(any());
    }

    @Test
    public void testGetCategoryForTab()
    {
        plugin.startUp();
        assertEquals(ItemCategory.TELEPORTS, plugin.getCategoryForTab(1));
    }
}
```

- [ ] **Step 2: Run all tests**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew test 2>&1 | tail -15`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/bankorganizer/BankOrganizerPluginTest.java
git commit -m "test: add BankOrganizerPlugin lifecycle tests"
```

---

### Task 9: Manual Testing

- [ ] **Step 1: Launch the plugin in dev mode**

Run: `cd C:/RuneLiteDev/DebugPlugin && ./gradlew run`

- [ ] **Step 2: Verify in RuneLite**

1. Open RuneLite and log into a character
2. Look for "Bank Organizer" in the sidebar (left panel)
3. Open the bank
4. Configure tab mappings in RuneLite plugin settings (gear icon)
5. Click "Scan Current Tab" on the sidebar
6. Verify colored overlays appear on misplaced items
7. Test category filter buttons
8. Verify the results list shows item names with correct category destinations
