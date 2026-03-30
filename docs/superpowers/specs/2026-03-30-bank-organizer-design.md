# Bank Organizer Plugin — Design Spec

## Overview

A RuneLite plugin that helps players organize their bank by scanning items and highlighting misplaced ones based on category presets. The user maps their in-game bank tabs to categories, clicks "Scan," and the plugin shows which items are in the wrong tab via overlays and a sidebar panel.

## Components

### 1. BankOrganizerPlugin (main plugin class)

- Replaces the existing DebugPlugin
- Subscribes to bank open/close events to enable/disable scanning
- Registers the sidebar panel and overlay
- Coordinates scanning: when the user clicks "Scan," reads current bank tab items, runs them through the categorizer, and sends results to the overlay and panel

### 2. BankOrganizerPanel (sidebar panel, extends PluginPanel)

Layout from top to bottom:

- **Preset dropdown** — selects a tab category preset (v1: "Default Layout" only)
- **Tab mapping section** — 9 rows, each: "Tab N → [Category dropdown]". User assigns a category to each of their in-game bank tabs. Unmapped tabs are ignored during scanning.
- **Custom regex section** — per-category text field for user-defined regex patterns to extend matching
- **Scan button** — triggers a scan of the currently viewed bank tab
- **Category filter buttons** — toggle which category's misplaced items are highlighted (e.g., click "Gear" to see all misplaced gear items). One category active at a time, or "All" to show everything.
- **Results list** — scrollable list of misplaced items: "Item Name → should be in Tab N (Category)"

### 3. BankOrganizerOverlay (draws on bank interface)

- Draws colored rectangles over misplaced item slots in the bank
- Each category has a distinct highlight color:
  - Teleports: blue
  - Gear: red
  - Potions: green
  - Food: orange
  - Tools: yellow
  - Raw Materials: brown
  - Quest/Misc: purple
  - Uncategorized: gray (items that matched no category but are not in Quest/Misc tab)
- Only draws when the bank is open and a scan has been performed
- Respects the active category filter from the panel

### 4. ItemCategorizer (matching engine)

Categorizes items using a priority chain (first match wins):

1. **Hardcoded item ID map** — curated list of common item IDs per category (fastest, most accurate)
2. **Keyword patterns** — substring matching on item names
3. **User-defined regex** — patterns entered in the sidebar panel
4. **Catch-all** — if no match, item falls into Quest/Misc

#### Default Layout Categories

| Category | Hardcoded IDs (examples) | Keyword Patterns |
|---|---|---|
| Teleports | Varrock teleport tab, law/air/fire/water/earth/nature/cosmic/astral/blood/death runes, skill capes (trimmed + untrimmed) | `teleport`, `teletab`, `rune$` (to avoid "rune platebody" etc.), common rune names, `cape` |
| Gear | Common weapons (whip, godswords, blowpipe), armor (barrows, d'hide, etc.), jewelry (berserker ring, fury) | `helm`, `platebody`, `platelegs`, `plateskirt`, `chainbody`, `shield`, `sword`, `scimitar`, `crossbow`, `bow`, `dart`, `arrow`, `bolt`, `whip`, `godsword`, `defender`, `boots`, `gloves`, `bracelet`, `amulet`, `ring`, `cape` (overlap with Teleports — ID check takes priority) |
| Potions | All standard potion IDs (4-dose through 1-dose) | `potion`, `brew`, `restore`, `mix`, `overload`, `prayer renewal` |
| Food | Shark, lobster, monkfish, manta ray, dark crab, anglerfish, etc. | Common food item names as keywords |
| Tools | Bronze through dragon axes/pickaxes, chisel, hammer, tinderbox, knife | `axe`, `pickaxe`, `hammer`, `chisel`, `knife`, `saw`, `tinderbox`, `needle`, `spade` |
| Raw Materials | Ores, bars, logs, hides, gems, seeds, herbs (grimy + clean), essence | `ore`, ` bar` (space-prefixed to avoid "barrows"), `logs`, `hide`, `leather`, `essence`, `seed`, `grimy`, `herb` |
| Quest/Misc | Catch-all for unmatched items | n/a |

Note: keyword conflicts (e.g., "cape" matching both Teleports and Gear) are resolved by the hardcoded ID map taking priority. Skill capes are mapped to Teleports by ID. Non-skill capes fall through to Gear keywords.

## Data Flow

1. User opens bank, opens the Bank Organizer sidebar panel
2. User configures tab mappings (Tab 1 → Teleports, Tab 2 → Gear, etc.)
3. User navigates to a bank tab and clicks "Scan"
4. Plugin reads all items in the current bank tab
5. For each item, `ItemCategorizer.categorize(itemName, itemId)` returns a Category
6. Plugin compares the item's category against the category assigned to the current tab
7. Mismatched items are sent to the overlay (for highlighting) and the panel (for the results list)
8. User can filter by category to focus on one type of misplaced item at a time

## Config Persistence

All settings saved via RuneLite's `ConfigManager`:

- Tab-to-category mappings (which category each tab 1-9 is assigned to)
- Custom regex patterns per category
- Selected preset name
- Last active category filter

Config interface: `BankOrganizerConfig` with `@ConfigGroup("bankorganizer")`

## Scanning Approach

**Scan-on-demand (v1):** The plugin only scans when the user clicks the Scan button. No background processing, no automatic re-scanning on bank changes.

## Future Features (not in v1)

- **Bank snapshot:** Snapshot current bank layout, name the tabs, save it. Plugin detects when items drift from the saved snapshot and alerts the user.
- **Live scanning:** Auto-scan on bank item changes with debounce (re-scan 1 tick after last change).
- **Multiple presets:** Allow users to create, name, and switch between custom presets.
- **Import/export:** Share preset configs as JSON files.

## File Structure

```
src/main/java/com/bankorganizer/
  BankOrganizerPlugin.java      — main plugin
  BankOrganizerConfig.java      — config interface
  BankOrganizerPanel.java       — sidebar panel UI
  BankOrganizerOverlay.java     — bank item overlay
  ItemCategorizer.java          — matching engine
  ItemCategory.java             — enum of categories with colors/keywords
```

Package rename from `com.debugplugin` to `com.bankorganizer`.
