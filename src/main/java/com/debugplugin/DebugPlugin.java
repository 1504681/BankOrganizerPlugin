package com.debugplugin;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
    name = "Debug Enemy List",
    description = "Shows all NPCs on screen with their tile positions",
    tags = {"npcs", "debug", "overlay"}
)
public class DebugPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private DebugOverlay overlay;

    @Override
    protected void startUp() throws Exception
    {
        log.info("Debug Enemy List started!");
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        log.info("Debug Enemy List stopped!");
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        // Count NPCs for logging
        int npcCount = 0;
        for (NPC npc : client.getNpcs())
        {
            if (npc != null && npc.getName() != null)
            {
                npcCount++;
            }
        }
        
        if (npcCount > 0)
        {
            log.debug("Found {} NPCs on screen", npcCount);
        }
    }
}