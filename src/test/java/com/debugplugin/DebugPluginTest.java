package com.debugplugin;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DebugPluginTest
{
	@Inject
	private DebugPlugin debugPlugin;

	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private OverlayManager overlayManager;

	@Mock
	@Bind
	private DebugOverlay debugOverlay;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void testStartUp() throws Exception
	{
		debugPlugin.startUp();
		verify(overlayManager).add(debugOverlay);
	}

	@Test
	public void testShutDown() throws Exception
	{
		debugPlugin.shutDown();
		verify(overlayManager).remove(debugOverlay);
	}

	@Test
	public void testOnGameTickWithNoNpcs()
	{
		when(client.getNpcs()).thenReturn(Collections.emptyList());
		debugPlugin.onGameTick(new GameTick());
	}

	@Test
	public void testOnGameTickWithNpcs()
	{
		NPC mockNpc = mock(NPC.class);
		when(mockNpc.getName()).thenReturn("Goblin");
		when(client.getNpcs()).thenReturn(Arrays.asList(mockNpc));

		debugPlugin.onGameTick(new GameTick());
	}
}
