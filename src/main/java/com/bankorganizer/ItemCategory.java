package com.bankorganizer;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public enum ItemCategory
{
	TELEPORTS("Teleports", new Color(0, 150, 255), Pattern.SOLID, Arrays.asList(
		"teleport", "teletab", "glory(", "dueling(", "games necklace(",
		"ring of wealth(", "skills necklace(", "combat bracelet(",
		"passage(", "burning amulet(", "digsite pendant(",
		"ectophial", "sceptre", "talisman", "medallion", "lyre",
		"slayer ring(", "seed pod", "chronicle", "ring of shadows",
		"ring of returning", "camulet", "quetzal whistle",
		"skull sceptre", "pharaoh", "basalt", "lunar seal",
		"kharedst", "memoirs"
	)),
	GEAR("Combat", new Color(220, 50, 50), Pattern.SOLID, Arrays.asList(
		"helm", "full helm", "med helm", "platebody", "platelegs", "plateskirt",
		"chainbody", "shield", "kiteshield", "sq shield",
		"sword", "longsword", "scimitar", "dagger", "mace", "warhammer", "battleaxe",
		"halberd", "spear", "hasta", "crossbow", "shortbow", "longbow",
		"dart", "knife", "javelin", "thrownaxe",
		"arrow", "bolt", "whip", "godsword", "defender",
		"boots", "gloves", "vambraces", "bracelet", "amulet", "necklace", "ring",
		"coif", "hood", "hat", "body", "chaps", "d'hide"
	)),
	POTIONS("Potions", new Color(190, 90, 255), Pattern.STRIPES, Arrays.asList(
		"potion", "brew", "restore", "mix", "overload", "prayer renewal",
		"antidote", "antifire", "antipoison", "antivenom", "battlemage",
		"bastion", "saradomin brew", "super combat", "ranging potion",
		"stamina", "energy"
	)),
	FOOD("Food", new Color(255, 225, 0), Pattern.STRIPES, Arrays.asList(
		"shark", "lobster", "swordfish", "tuna", "salmon", "trout",
		"monkfish", "manta ray", "dark crab", "anglerfish", "karambwan",
		"bass", "pike", "shrimps", "anchovies", "sardine", "herring",
		"mackerel", "cod", "cake", "bread", "meat", "chicken",
		"wine", "stew", "potato", "mushroom", "sweetcorn",
		"cooked"
	)),
	SKILLING("Skilling", new Color(255, 140, 0), Pattern.SOLID, Arrays.asList(
		"pickaxe", "hammer", "chisel", "saw", "tinderbox",
		"needle", "spade", "rake", "seed dibber", "secateurs",
		"watering can", "trowel", "pestle and mortar",
		"glassblowing pipe", "shears", "bucket",
		"fish barrel", "herb sack", "gem bag", "coal bag", "plank sack",
		"seed box", "log basket", "forestry kit", "tackle box",
		"graceful", "lumberjack", "angler", "farmer",
		"prospector", "pyromancer", "rogue",
		"axe", "harpoon", "net", "rod", "compost"
	)),
	RAW_MATERIALS("Materials", new Color(0, 225, 210), Pattern.STRIPES, Arrays.asList(
		" ore", "bronze bar", "iron bar", "steel bar", "mithril bar",
		"adamantite bar", "runite bar", "gold bar", "silver bar",
		"logs", "hide", "leather", "essence",
		"seed", "grimy", "herb", "feather", "bone",
		"wool", "flax", "clay", "sand"
	)),
	HIGH_ALCH("High Alch", new Color(255, 60, 190), Pattern.CROSSHATCH, Arrays.asList()),
	CURRENCY("Main Tab", new Color(255, 255, 255), Pattern.CROSSHATCH, Arrays.asList(
		"coins", "platinum token", "tokkul", "trading sticks",
		"numulite", "pieces of eight", "warrior guild token",
		"mermaid's tear", "hallowed mark", "molch pearl"
	)),
	QUEST("Quest", new Color(60, 200, 60), Pattern.SOLID, Arrays.asList()),
	// Constant name kept for saved profiles and config; shown as "Misc" since 1.2.0 (Quest is its own category)
	QUEST_MISC("Misc", new Color(170, 170, 170), Pattern.STRIPES, Arrays.asList());

	/** Fill pattern drawn over the colour so categories stay apart when hues are close at low opacity. */
	public enum Pattern { SOLID, STRIPES, CROSSHATCH }

	private final String displayName;
	private final Color color;
	private final Pattern pattern;
	private final List<String> keywords;

	ItemCategory(String displayName, Color color, Pattern pattern, List<String> keywords)
	{
		this.displayName = displayName;
		this.color = color;
		this.pattern = pattern;
		this.keywords = keywords;
	}

	public String getDisplayName() { return displayName; }
	public Color getColor() { return color; }
	public Pattern getPattern() { return pattern; }
	public List<String> getKeywords() { return keywords; }
}
