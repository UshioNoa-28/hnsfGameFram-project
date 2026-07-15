package com.tedu.game;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

/** Minimal resource/map test that does not open a Swing window. */
public final class GameSmokeTest {
	private GameSmokeTest() { }

	public static void main(String[] args) {
		GameLoad.loadImg(); GameLoad.loadObj();
		if (GameLoad.getSprites("player", "run") == null) throw new IllegalStateException("Player animation missing");
		if (GameLoad.getSprites("enemy", "death") == null) throw new IllegalStateException("Enemy death animation missing");
		if (GameLoad.getSprites("explosion", "bomb_bang") == null) throw new IllegalStateException("Explosion animation missing");

		ElementManager em = ElementManager.getManager();
		for (int level = 1; level <= 3; level++) {
			em.clearAll(); GameLoad.MapLoad(level); GameLoad.loadPlay();
			int maps = em.getElementsByKey(GameElement.MAPS).size();
			int players = em.getElementsByKey(GameElement.PLAY).size();
			int enemies = em.getElementsByKey(GameElement.ENEMY).size();
			if (maps < 2 || players != 1 || enemies < 1) throw new IllegalStateException("Level " + level + " failed to load");
			System.out.println("Level " + level + ": maps=" + maps + ", enemies=" + enemies + ", items=" + em.getElementsByKey(GameElement.ITEM).size());
		}
		System.out.println("SMOKE TEST PASSED");
	}
}
