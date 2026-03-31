package com.bankorganizer;

import java.util.HashMap;
import java.util.Map;

/**
 * A profile stores all user configuration: category overrides, subcategory overrides,
 * tab mappings, and custom category definitions.
 */
public class BankOrganizerProfile
{
	private String name;
	private String categoryOverrides = "";   // itemId:CATEGORY,...
	private String subCategoryOverrides = ""; // itemId:skillIndex,...
	private Map<String, String> tabMappings = new HashMap<>(); // tab1->TELEPORTS, etc.
	private Map<String, String> categoryColors = new HashMap<>(); // GEAR->#DC3232, etc.
	private Map<String, String> regexPatterns = new HashMap<>(); // GEAR->pattern, etc.

	public BankOrganizerProfile(String name)
	{
		this.name = name;
	}

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getCategoryOverrides() { return categoryOverrides; }
	public void setCategoryOverrides(String overrides) { this.categoryOverrides = overrides; }

	public String getSubCategoryOverrides() { return subCategoryOverrides; }
	public void setSubCategoryOverrides(String overrides) { this.subCategoryOverrides = overrides; }

	public Map<String, String> getTabMappings() { return tabMappings; }
	public Map<String, String> getCategoryColors() { return categoryColors; }
	public Map<String, String> getRegexPatterns() { return regexPatterns; }

	/**
	 * Serialize profile to a single string for storage/export.
	 * Format: name|catOverrides|subOverrides|tab1=CAT,tab2=CAT|color1=HEX,...|regex1=pat,...
	 */
	public String serialize()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\n");
		sb.append("CAT:").append(categoryOverrides).append("\n");
		sb.append("SUB:").append(subCategoryOverrides).append("\n");

		sb.append("TABS:");
		boolean first = true;
		for (Map.Entry<String, String> e : tabMappings.entrySet())
		{
			if (!first) sb.append(",");
			sb.append(e.getKey()).append("=").append(e.getValue());
			first = false;
		}
		sb.append("\n");

		sb.append("COLORS:");
		first = true;
		for (Map.Entry<String, String> e : categoryColors.entrySet())
		{
			if (!first) sb.append(",");
			sb.append(e.getKey()).append("=").append(e.getValue());
			first = false;
		}
		sb.append("\n");

		sb.append("REGEX:");
		first = true;
		for (Map.Entry<String, String> e : regexPatterns.entrySet())
		{
			if (!first) sb.append(",");
			sb.append(e.getKey()).append("=").append(e.getValue());
			first = false;
		}

		return sb.toString();
	}

	/**
	 * Deserialize profile from string.
	 */
	public static BankOrganizerProfile deserialize(String data)
	{
		if (data == null || data.isEmpty()) return null;

		String[] lines = data.split("\n");
		if (lines.length < 1) return null;

		BankOrganizerProfile profile = new BankOrganizerProfile(lines[0].trim());

		for (int i = 1; i < lines.length; i++)
		{
			String line = lines[i];
			if (line.startsWith("CAT:"))
			{
				profile.categoryOverrides = line.substring(4);
			}
			else if (line.startsWith("SUB:"))
			{
				profile.subCategoryOverrides = line.substring(4);
			}
			else if (line.startsWith("TABS:"))
			{
				parseMap(line.substring(5), profile.tabMappings);
			}
			else if (line.startsWith("COLORS:"))
			{
				parseMap(line.substring(7), profile.categoryColors);
			}
			else if (line.startsWith("REGEX:"))
			{
				parseMap(line.substring(6), profile.regexPatterns);
			}
		}

		return profile;
	}

	private static void parseMap(String data, Map<String, String> map)
	{
		if (data == null || data.isEmpty()) return;
		for (String entry : data.split(","))
		{
			int eq = entry.indexOf('=');
			if (eq > 0)
			{
				map.put(entry.substring(0, eq).trim(), entry.substring(eq + 1).trim());
			}
		}
	}

	/**
	 * Create default profile with all current defaults.
	 */
	public static BankOrganizerProfile createDefault()
	{
		BankOrganizerProfile p = new BankOrganizerProfile("Default Layout");
		// Tab mappings match BankOrganizerConfig defaults
		p.tabMappings.put("tab1", "TELEPORTS");
		p.tabMappings.put("tab2", "GEAR");
		p.tabMappings.put("tab3", "POTIONS");
		p.tabMappings.put("tab4", "FOOD");
		p.tabMappings.put("tab5", "SKILLING");
		p.tabMappings.put("tab6", "RAW_MATERIALS");
		p.tabMappings.put("tab7", "QUEST_MISC");
		p.tabMappings.put("tab8", "QUEST_MISC");
		p.tabMappings.put("tab9", "QUEST_MISC");
		return p;
	}

	/**
	 * Create blank profile with no overrides or mappings.
	 */
	public static BankOrganizerProfile createBlank(String name)
	{
		return new BankOrganizerProfile(name);
	}
}
