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
	public void testIssue7TabsFinish()
	{
		// Ideal positions from the two "Ordering loop" dumps in issue #7 (main tab mapped to Currency, then
		// Teleports): the planner sorts both, so those stops were the guard tripping on a re-plan, not a loop
		int[] currency = {0,67,66,46,17,72,68,40,3,81,26,31,7,18,27,42,54,16,21,13,28,49,61,43,14,20,58,50,83,38,37,44,
			11,71,51,55,6,5,4,45,25,57,73,76,10,29,30,41,15,23,22,33,32,48,56,34,35,63,64,65,62,79,69,70,9,47,52,59,75,1,
			19,36,24,53,2,8,12,39,60,74,77,78,80,82};
		int[] teleports = {65,78,77,15,64,16,39,68,40,41,42,43,0,8,9,2,70,49,52,56,57,10,4,76,71,50,51,54,55,3,13,14,72,
			53,58,48,44,25,26,27,73,45,46,47,19,1,11,69,34,35,36,30,7,59,6,31,32,28,29,33,5,82,37,38,18,20,21,22,23,24,12,
			66,74,61,62,63,67,60,75,79,80,81,17,83};
		drags(currency);
		drags(teleports);
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

	@Test
	public void testDuplicateItemsDoNotLoop()
	{
		// Construct. cape, Farming cape, X, Crafting cape, Ectophial, X, Pharaoh's sceptre, Xeric's talisman
		// where X is the same item twice: the old id -> position map looped "cape after Ectophial" /
		// "cape before Farming cape" forever
		long[] keys = {0, 1, 4, 2, 3, 4, 5, 6};
		int[] perm = BankOrganizerPlugin.idealPositions(keys);
		assertEquals(0, perm[0]); // the cape is already first
		assertEquals(1, drags(perm)); // only the first X moves, down after Ectophial
		boolean[] seen = new boolean[perm.length];
		for (int v : perm) seen[v] = true;
		for (boolean b : seen) assertTrue(b);
	}
}
