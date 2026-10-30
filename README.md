# Bank Tab Organizer

A RuneLite plugin that helps you organize your bank by categorizing items, highlighting misplaced ones, and guiding you through reordering items within tabs.

## Features

### Category Overlays
- **Show Overlays** draws colored boxes on every bank item showing which category it belongs to
- 10 built-in categories: Teleports, Combat, Potions, Food, Skilling, Materials, High Alch, Main Tab (currency, clues, valuables), Quest, Misc
- **Merge Categories**: send one category to another category's tab (Food → Potions for a single supplies tab, Quest → Misc for one quest/misc tab). Scan treats merged items as correctly placed; Ordering sorts them after the tab's own items, each group by its own rules
- **Filter by Category** to highlight only one category at a time
- Customizable colors for each category in plugin settings (**Colors** section — every category colour is yours to change)
- Adjustable overlay opacity

### Item Categorization
- Items are auto-categorized by item ID, then rules (skilling outfits → Skilling; seeds, herbs, secondaries, uncut gems, ores, bars, logs, planks, hides, raw food, bones, ammo tips → Materials; adamant/mithril equipment → High Alch; Barrows gear → Combat), then name keywords, then equipment stats (anything wearable with combat stats → Combat; non-wearable items never match gear keywords). Whatever is left is Quest if it is untradeable and unstackable (keys, letters, quest-only tools), otherwise Misc
- **Right-click categorization**: toggle "Start Categorizing", then right-click any bank item to assign it to a category
- **Subcategory mode**: toggle to assign items to skill-based subcategories (Farming, Runecrafting, Woodcutting, etc.)
- **Mass categorize**: right-click a bank tab to categorize all visible items at once
- Custom regex patterns per category for advanced matching

### Smart Sorting
- **Combat tab**: items sorted by combat style (melee/ranged/mage), then by equipment slot, then by stat strength (highest first). **Keep armour sets together** puts Torva, Barrows, Void, Ancestral... pieces side by side, set by set, before the loose items
- **Teleports tab**: teleport items first (skill capes, quest/diary/music capes, farming-patch teleports like Ardougne cloak, Explorer's ring and Ectophial, other diary items (Morytania legs first), then the rest A-Z, teleport crystals last), then tablets and scrolls, jewellery (skills necklace, glory, dueling, games... highest charge first, uncharged last), and runes (rune pouches first, elemental > combination > catalytic). Anything with no teleport option (Blighted sacks, Kandarin headgear 1) goes to the bottom. Change the group order with **Teleport Sort Mode** (or **Custom**); **Rune pouch first** puts the pouches at the very top
- **Potions tab**: combat boosts, brews & restores, antidotes, energy, skilling & other, barbarian mixes, raid potions; inside each group divine first, then by priority, highest dose first
- **Food tab**: highest healing first, then drinks (wine, ales), part-made food, burnt food last
- **Main tab**: coins and platinum tokens, other currencies, pets, caskets, scroll boxes (and clue nests/bottles/geodes), clue scrolls; clue items beginner → master
- **Skilling tab**: grouped by skill (Farming > Runecrafting > Woodcutting > Fishing > Mining > Prayer > Agility, etc.)
- **Materials tab**: grouped by skill (Farming, Woodcutting, Fishing, Mining, Prayer, Crafting, Fletching, Herblore, Hunter…), then by tier within each group (ores/bars/gems, logs/planks, tips by metal, raw food by cooking level, bones by prayer XP, herbs by level, hides, bird houses by wood). Farming seeds go at the very bottom
- **Seed vault reminder** (off by default): green outline on seeds in the bank (store them in the seed vault), red outline on seed placeholders (release them)
- Bronze to adamant pickaxes and axes go to Misc; rune and better stay in Skilling. **Low-tier tools in Skilling** keeps them all

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
- Each profile records which plugin version its defaults came from (shown under the profile selector). When the plugin ships newer defaults they are **applied automatically** to the profile — anything you categorized yourself is kept exactly as you set it. **Manage Profiles → Re-apply Defaults** does the same on demand (also works on blank profiles)

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
2. Assign each bank tab (1-9) to a category (e.g., Tab 1 = Teleports, Tab 2 = Combat). **Main tab (All)** (default: Main Tab category) sets the category for items that aren't in any numbered tab — the section at the bottom of the "All" view; scanning/ordering while viewing "All" works on just that section, and the All tab gets a colour bar too
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

## Contributing categorizations

Item categorization is data-driven. If items land in the wrong tab, open a "Wrong category"
issue with the item names, or — better — organise your bank, use the plugin's **Export** button
and post the result in a "Share my profile" issue. Good profiles get merged into the defaults.

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
| General | Show sidebar icon, tab colors, overlay opacity, category patterns, low-tier tools in Skilling, seed vault reminder |
| Tab Mappings | Assign categories to the main (All) tab and bank tabs 1-9 |
| Merge Categories | Per category, which other category's tab it shares. Quest → Misc by default; set it to Keep separate and map a tab to Quest for a dedicated quest tab |
| Sorting | Gear sort mode (Combat Style / Equipment Type), Keep armour sets together, Teleport sort mode + custom group order, Rune pouch first |
| Colors | Customize overlay color for each category (**Use new colours** in the panel resets them to the current defaults) |
| Custom Regex | Add regex patterns for advanced item matching |

## Changelog

1.2.3: patterns sit on a thin band around each item box so the item stays fully visible, and the bank tab bars show the same patterns. New default colours: Food green, Main Tab gold, Quest white with dots, Misc grey. New installs get them; existing installs keep their current colours until you press **Use new colours** at the top of the panel. Items are also sorted by what they do in your inventory: anything with a Teleport option is a Teleport (Desert amulet, Explorer's ring, Fremennik sea boots, Kandarin headgear, skillcapes), Drink is Potions for real potions (raid potions, Nectar, Anti-venom+) and Food for ales, cocktails and tea, Eat is Food (gnome food, Varlamore fish and bats), Break is Teleports, Bury/Scatter is Materials. The ordering guide notices when the bank is in Swap mode and tells you to switch to Insert. Bank placeholders and noted items now use the real item's category and overrides. Old defaults that a later version removed (Gold ring, Rune hasta, Bronze axe... in Misc) are cleaned out of existing profiles once; your own overrides stay. Pets are Main Tab; boss and upgrade parts (Godsword shards, Fangs of venenatis, Noxious halberd parts, Araxyte head), Sailing hull parts, corals, Cotton boll, Sacred oil and Cache of runes are Materials; Steam, Aether and Wrath runes and the Blighted vengeance sack are Teleports; Mixed hide base, Bucket of water and brutal arrows no longer land in Potions/Skilling/High Alch. Teleports tab: new default order (teleport items → tablets → jewellery → runes) with capes and farming-patch teleports leading; Ring of wealth (5) and other spaced/imbued jewellery is sorted as jewellery instead of near the top, uncharged jewellery ends its family, items that can't teleport go last; new **Rune pouch first** option (off by default). If you picked a Teleport Sort Mode yourself it is kept; "Teleport Items First" gives the new order. Morytania legs count as a diary item, not a farming teleport. Food: highest healing first, then drinks, part-made, burnt. Potions in groups: combat, brews & restores, antidotes, energy, skilling, mixes, raid. Main tab: currencies, pets, caskets, scroll boxes, clue scrolls. Farming seeds sort last in Materials; new **Seed vault reminder** outlines seeds (vault them) and seed placeholders (release them). Bronze to adamant pickaxes/axes go to Misc (**Low-tier tools in Skilling** keeps them). New **Keep armour sets together** for the Combat tab. All new options are off by default.

1.2.2: new default colours so no two categories look alike (Potions was black, Skilling and Main Tab were both orange, Food and Quest both green-ish). Potions, Food, Materials and Misc are striped, Main Tab and High Alch crosshatched; turn it off with General → Category Patterns. Teleports and Combat keep their colours; colours you changed yourself are kept. Fruit and veg (and their baskets/sacks) are Food; herblore-only berries and Zamorak's grapes stay Materials. Categorisation audit of ~700 common items: 81 shipped defaults that sent real gear to Misc are gone (Dragon 2h sword, Magic longbow, Amulet of the damned, Ring of life, Brimstone ring, Salve amulet, enchanted bolts...); super attack/strength/defence, Sanfew serum, Relicym's balm and weapon poison are Potions; pies, pizzas, kebabs, sweets, curry, rainbow fish and cave eel are Food; moulds, hunter snares, RC pouches, Lobster pot, Karambwan vessel and beekeeper's outfit are Skilling; ensouled heads, unstrung bows, stocks/limbs, demonic ashes, leaping fish, sacred/infernal eels, silk, snakeskin and granite are Materials; Ring of the elements, eternal glory, Western banner, crafting/farming/construction capes, divine rune pouch and Dorgesh-kaan spheres are Teleports. Soulreaper and zombie axes, Confliction gauntlets and every Avernic treads variant are Combat, Spiked manacles and kebbit spikes are no longer Food; PvP ancient emblems/totems/relics, blood money, golden nuggets and agility arena tickets are Main Tab; Varlamore moths and antelope fur are Materials, Calcified moth is a Teleport. Mithril/adamant gloves are Combat, rune arrows/pickaxe/axe no longer High Alch, Mark of grace is Main Tab.

1.2.1: unstrung jewellery (every "amulet (u)", Unstrung symbol, Unstrung emblem) is a Material in the Crafting group, gold first then by gem. It used to be Gear.

1.2.0: Quest is its own category (untradeable, unstackable leftovers; 19 shipped defaults such as the Family Crest 'perfect' items, Bone key and Ring of Charos moved there), the old Quest/Misc is now Misc. New Merge Categories settings: any category can share another category's tab, e.g. Food → Potions; merged items count as correctly placed in Scan and are ordered after the tab's own items by their own rules. Quest is merged into Misc by default, so existing layouts keep working (#6).

1.1.5: gems follow the wiki Gems list: every uncut and cut gem (opal to zenyte) is a Material in the Mining group, uncut gems sort before cut gems, each by tier. Crushed gem, Amethyst, Zenyte shard and Oyster pearls are Materials too (they used to be Quest/Misc). Pinning a Mining/Crafting/... subcategory on a Material now keeps the ore, bar, gem and tier order inside that group instead of falling back to item id.

1.1.4: staves, battlestaves, wands, tridents and sceptres are always Magic Weapons (their crush bonus used to win). Ordering now says which items it left in place and why, and tells you when a tab is mapped to a category with no sort order, instead of a bare "complete". "Current: <skill>" in the right-click menu is clickable and pins that subcategory. Stuck-step counters reset on every Start Ordering.

## License

BSD 2-Clause License
