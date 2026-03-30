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
