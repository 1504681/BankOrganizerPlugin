package com.debugplugin;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class DebugOverlay extends OverlayPanel
{
    private final Client client;

    @Inject
    private DebugOverlay(Client client)
    {
        this.client = client;
        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();
        
        int count = 0;
        for (NPC npc : client.getNpcs())
        {
            if (npc != null && npc.getName() != null && count < 15)
            {
                panelComponent.getChildren().add(LineComponent.builder()
                    .left(npc.getName())
                    .right(String.format("[%d, %d]", 
                        npc.getWorldLocation().getX(), 
                        npc.getWorldLocation().getY()))
                    .build());
                count++;
            }
        }
        
        if (count == 0)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("No NPCs found")
                .build());
        }
        else if (client.getNpcs().size() > 15)
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left("... and " + (client.getNpcs().size() - 15) + " more")
                .build());
        }
        
        return super.render(graphics);
    }
}