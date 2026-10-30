package com.bankorganizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderPlannerTest
{
	/** Plays the planner against a simulated bank in insert mode; returns the number of drags. */
	private static int drags(int[] start)
	{
		List<Integer> bank = new ArrayList<>();
		for (int v : start) bank.add(v);
		int n = start.length;
		boolean[] movable = new boolean[n];
		Arrays.fill(movable, true);
		int count = 0;
		while (true)
		{
			int[] perm = bank.stream().mapToInt(Integer::intValue).toArray();
			int[] move = BankOrganizerPlugin.nextMove(perm, movable);
			if (move == null) break;
			// insert mode: the dragged item takes the slot it is dropped on, the items in between shift
			int item = bank.remove(move[0]);
			bank.add(move[1], item);
			count++;
			assertTrue("planner looped", count <= n);
		}
		for (int i = 0; i < n; i++) assertEquals(i, (int) bank.get(i));
		return count;
	}

	@Test
	public void testItemAtTopIsDraggedDownOnce()
	{
		// Blighted sack (belongs last) above ten runes: one drag down, not ten drags up
		assertEquals(1, drags(new int[]{10, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}));
		int[] move = BankOrganizerPlugin.nextMove(new int[]{10, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, all(11));
		assertEquals(0, move[0]);
		assertEquals(10, move[1]); // dropped on the last rune
		assertEquals(1, move[2]); // lands after it
	}

	@Test
	public void testItemAtBottomIsDraggedUpOnce()
	{
		assertEquals(1, drags(new int[]{1, 2, 3, 4, 0}));
		assertEquals(0, BankOrganizerPlugin.nextMove(new int[]{1, 2, 3, 4, 0}, all(5))[2]);
	}

	@Test
	public void testRandomTabsUseTheMinimumDrags()
	{
		Random r = new Random(7);
		for (int t = 0; t < 300; t++)
		{
			int n = 1 + r.nextInt(40);
			List<Integer> l = new ArrayList<>();
			for (int i = 0; i < n; i++) l.add(i);
			java.util.Collections.shuffle(l, r);
			int[] perm = l.stream().mapToInt(Integer::intValue).toArray();
			assertEquals(n - BankOrganizerPlugin.computeLIS(perm).size(), drags(perm));
		}
	}

	@Test
	public void testUnmovableItemsAreLeftAlone()
	{
		boolean[] movable = all(3);
		movable[2] = false;
		assertNull(BankOrganizerPlugin.nextMove(new int[]{2, 0, 1}, movable));
	}

	private static boolean[] all(int n)
	{
		boolean[] b = new boolean[n];
		Arrays.fill(b, true);
		return b;
	}
}
