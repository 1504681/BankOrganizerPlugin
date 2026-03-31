package com.bankorganizer;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages profile storage, switching, import/export.
 * Profiles are stored in RuneLite config as Base64-encoded strings.
 */
public class ProfileManager
{
	private static final Logger log = LoggerFactory.getLogger(ProfileManager.class);

	private final BankOrganizerConfig config;
	private final Map<String, BankOrganizerProfile> profiles = new LinkedHashMap<>();
	private String activeProfileName;

	public ProfileManager(BankOrganizerConfig config)
	{
		this.config = config;
	}

	public void loadProfiles()
	{
		profiles.clear();

		String profileListStr = config.profileList();
		if (profileListStr == null || profileListStr.isEmpty())
		{
			profileListStr = "Default Layout";
		}

		String[] names = profileListStr.split("\\|");
		String dataStr = config.profileData();
		Map<String, String> dataMap = new LinkedHashMap<>();

		if (dataStr != null && !dataStr.isEmpty())
		{
			// Format: name1::base64data1||name2::base64data2
			for (String block : dataStr.split("\\|\\|"))
			{
				int sep = block.indexOf("::");
				if (sep > 0)
				{
					dataMap.put(block.substring(0, sep), block.substring(sep + 2));
				}
			}
		}

		for (String name : names)
		{
			name = name.trim();
			if (name.isEmpty()) continue;

			String encoded = dataMap.get(name);
			if (encoded != null)
			{
				try
				{
					String decoded = new String(Base64.getDecoder().decode(encoded));
					BankOrganizerProfile profile = BankOrganizerProfile.deserialize(decoded);
					if (profile != null)
					{
						profiles.put(name, profile);
						continue;
					}
				}
				catch (Exception e)
				{
					log.warn("Failed to decode profile: {}", name, e);
				}
			}

			// Create empty profile if data not found
			profiles.put(name, new BankOrganizerProfile(name));
		}

		// Ensure at least "Default Layout" exists
		if (!profiles.containsKey("Default Layout"))
		{
			profiles.put("Default Layout", BankOrganizerProfile.createDefault());
		}

		activeProfileName = config.activeProfile();
		if (activeProfileName == null || !profiles.containsKey(activeProfileName))
		{
			activeProfileName = "Default Layout";
		}

		log.info("Loaded {} profiles, active: {}", profiles.size(), activeProfileName);
	}

	public void saveProfiles()
	{
		StringBuilder nameList = new StringBuilder();
		StringBuilder dataList = new StringBuilder();

		for (Map.Entry<String, BankOrganizerProfile> entry : profiles.entrySet())
		{
			if (nameList.length() > 0) nameList.append("|");
			nameList.append(entry.getKey());

			if (dataList.length() > 0) dataList.append("||");
			String serialized = entry.getValue().serialize();
			String encoded = Base64.getEncoder().encodeToString(serialized.getBytes());
			dataList.append(entry.getKey()).append("::").append(encoded);
		}

		config.setProfileList(nameList.toString());
		config.setProfileData(dataList.toString());
		config.setActiveProfile(activeProfileName);
	}

	public BankOrganizerProfile getActiveProfile()
	{
		return profiles.getOrDefault(activeProfileName,
			BankOrganizerProfile.createDefault());
	}

	public String getActiveProfileName()
	{
		return activeProfileName;
	}

	public List<String> getProfileNames()
	{
		return new ArrayList<>(profiles.keySet());
	}

	/**
	 * Switch to a different profile. Returns the profile.
	 */
	public BankOrganizerProfile switchProfile(String name)
	{
		if (!profiles.containsKey(name))
		{
			log.warn("Profile not found: {}", name);
			return getActiveProfile();
		}

		activeProfileName = name;
		saveProfiles();
		log.info("Switched to profile: {}", name);
		return profiles.get(name);
	}

	/**
	 * Save current state to the active profile.
	 */
	public void saveCurrentState(String catOverrides, String subOverrides)
	{
		BankOrganizerProfile profile = getActiveProfile();
		profile.setCategoryOverrides(catOverrides);
		profile.setSubCategoryOverrides(subOverrides);
		saveProfiles();
	}

	/**
	 * Create a new profile from defaults.
	 */
	public BankOrganizerProfile createFromDefault(String name)
	{
		BankOrganizerProfile profile = BankOrganizerProfile.createDefault();
		profile.setName(name);

		// Copy default overrides from resource files
		try
		{
			java.io.InputStream catStream = getClass().getResourceAsStream("/com/bankorganizer/default_overrides.txt");
			if (catStream != null)
			{
				profile.setCategoryOverrides(new String(catStream.readAllBytes()).trim());
				catStream.close();
			}
			java.io.InputStream subStream = getClass().getResourceAsStream("/com/bankorganizer/default_sub_overrides.txt");
			if (subStream != null)
			{
				profile.setSubCategoryOverrides(new String(subStream.readAllBytes()).trim());
				subStream.close();
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to load default overrides for new profile", e);
		}

		profiles.put(name, profile);
		saveProfiles();
		log.info("Created profile from defaults: {}", name);
		return profile;
	}

	/**
	 * Create a blank profile with no overrides.
	 */
	public BankOrganizerProfile createBlank(String name)
	{
		BankOrganizerProfile profile = BankOrganizerProfile.createBlank(name);
		profiles.put(name, profile);
		saveProfiles();
		log.info("Created blank profile: {}", name);
		return profile;
	}

	/**
	 * Delete a profile.
	 */
	public boolean deleteProfile(String name)
	{
		if ("Default Layout".equals(name))
		{
			return false; // Can't delete default
		}

		profiles.remove(name);
		if (activeProfileName.equals(name))
		{
			activeProfileName = "Default Layout";
		}
		saveProfiles();
		log.info("Deleted profile: {}", name);
		return true;
	}

	/**
	 * Export active profile as a shareable string (Base64).
	 */
	public String exportProfile()
	{
		BankOrganizerProfile profile = getActiveProfile();
		String serialized = profile.serialize();
		return Base64.getEncoder().encodeToString(serialized.getBytes());
	}

	/**
	 * Import a profile from Base64 string.
	 */
	public BankOrganizerProfile importProfile(String encoded)
	{
		try
		{
			String decoded = new String(Base64.getDecoder().decode(encoded.trim()));
			BankOrganizerProfile profile = BankOrganizerProfile.deserialize(decoded);
			if (profile == null)
			{
				return null;
			}

			// Avoid name collision
			String name = profile.getName();
			int i = 1;
			while (profiles.containsKey(name))
			{
				name = profile.getName() + " (" + i + ")";
				i++;
			}
			profile.setName(name);

			profiles.put(name, profile);
			saveProfiles();
			log.info("Imported profile: {}", name);
			return profile;
		}
		catch (Exception e)
		{
			log.error("Failed to import profile", e);
			return null;
		}
	}
}
