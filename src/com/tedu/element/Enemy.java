package com.tedu.element;

import java.awt.Graphics;
import java.util.List;
import java.util.Random;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;
import com.tedu.show.GameMainJPanel;
import com.tedu.controller.GameThread;

/**
 * 敌人 — 合金弹头敌人精灵，按类型使用不同帧组
 */
public class Enemy extends ElementObj {

	private String enemyType = "SOLDIER";
	private int moveDir = -1;
	private int shootTimer = 0, dirChangeTimer = 0;
	private int animFrame = 0, animTimer = 0;
	private int attackPose = 0;
	private int deathFrame = 0, deathTimer = 0;
	private boolean dying = false;
	private static Random rand = new Random();

	public Enemy() { setHp(2); setMaxHp(2); setFrom("enemy"); shootTimer = 55 + rand.nextInt(40); }

	@Override
	public void showElement(Graphics g) {
		if (dying) {
			List<ImageIcon> dframes = GameLoad.getSprites("enemy", "death");
			if (dframes != null && !dframes.isEmpty()) {
				ImageIcon frame = dframes.get(deathFrame % dframes.size());
				if (frame != null && frame.getImage() != null) {
					int iw = frame.getIconWidth(), ih = frame.getIconHeight();
					int dx = getX(), dy = getY() + getH() - ih;
					if (moveDir <= 0) g.drawImage(frame.getImage(), dx, dy, iw, ih, null);
					else g.drawImage(frame.getImage(), dx + iw, dy, -iw, ih, null);
				}
			}
			return;
		}

		String anim = getAnimGroup();
		List<ImageIcon> frames = GameLoad.getSprites("enemy", anim);
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("enemy");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) frame = frames.get(animFrame % frames.size());

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			int dx = getX(), dy = getY() + getH() - ih;
			if (moveDir <= 0) {
				g.drawImage(frame.getImage(), dx, dy, iw, ih, null);
			} else {
				g.drawImage(frame.getImage(), dx + iw, dy, -iw, ih, null);
			}
		} else {
			g.setColor(java.awt.Color.RED); g.fillRect(getX(), getY(), getW(), getH());
		}

		if (getHp() < getMaxHp()) {
			int hpW = getW() * Math.max(0, getHp()) / getMaxHp();
			g.setColor(new java.awt.Color(80, 15, 12)); g.fillRect(getX(), getY() - 5, getW(), 2);
			g.setColor(new java.awt.Color(240, 175, 44)); g.fillRect(getX(), getY() - 5, hpW, 2);
		}
	}

	private String getAnimGroup() {
		switch (enemyType) {
			case "SNIPER": return "stand_shoot";
			case "TURRET": return "stand_shoot2";
			case "SOLDIER": return Math.abs(getVx()) > 0 ? "walk_shoot" : "walk_shoot_idle";
			case "RUNNER": return Math.abs(getVx()) > 0 ? "walk_shoot2" : "walk_shoot2_idle";
			case "GUARD": return "static_guard";
			default: return "walk_shoot";
		}
	}

	@Override
	protected void move() {
		if (dying) {
			deathTimer++;
			if (deathTimer >= 6) { deathTimer = 0; deathFrame++; }
			if (deathFrame >= 4) setLive(false);
			return;
		}

		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players == null || players.isEmpty()) { setVx(-1); setX(getX() + getVx()); return; }
		ElementObj player = players.get(0);
		int dx = player.getX() - getX();
		if (Math.abs(dx) > 720) { setVx(0); return; }

		switch (enemyType) {
			case "SNIPER":
			case "TURRET":
			case "GUARD":
				setVx(0); moveDir = dx > 0 ? 1 : -1;
				break;
			case "RUNNER":
				moveDir = dx > 0 ? 1 : -1;
				setVx(Math.abs(dx) > 180 ? moveDir * 2 : 0);
				break;
			default: // SOLDIER: enter range, then strafe instead of blindly touching the player
				moveDir = dx > 0 ? 1 : -1;
				if (Math.abs(dx) > 230) setVx(moveDir);
				else if (Math.abs(dx) < 115) setVx(-moveDir);
				else setVx(0);
				break;
		}
		setX(getX() + getVx());

		int h = getH(), prevBottom = getY() + h;
		setVy(getVy() + 1); setY(getY() + getVy());

		int landingY = Integer.MAX_VALUE;
		for (ElementObj map : ElementManager.getManager().getElementsByKey(GameElement.MAPS)) {
			if (!(map instanceof Platform)) continue;
			if (getX() + getW() > map.getX() && getX() < map.getX() + map.getW()
					&& prevBottom <= map.getY() && getY() + h >= map.getY())
				landingY = Math.min(landingY, map.getY() - h);
		}
		if (landingY != Integer.MAX_VALUE) { setY(landingY); setVy(0); }
		if (getY() + h >= 430) { setY(430 - h); setVy(0); }

		if (getX() < 0) setX(0); if (getX() > 2350) setX(2350);

		// 需要动画的类型才推进帧
		if (needsAnim()) {
			animTimer++;
			if (animTimer >= 8) { animTimer = 0; animFrame++; }
		}
	}

	private boolean needsAnim() {
		switch (enemyType) {
			case "SOLDIER": return Math.abs(getVx()) > 0;
			case "RUNNER": return Math.abs(getVx()) > 0;
			default: return false;
		}
	}

	@Override
	protected void add(long gameTime) {
		if (!isLive() || dying) return;
		if (GameThread.getStageTime() < 120) return;
		if (attackPose > 0) attackPose--;
		if (--shootTimer > 0) return;

		// GUARD 不射击
		if ("GUARD".equals(enemyType)) return;

		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players == null || players.isEmpty()) return;
		ElementObj player = players.get(0);
		int dx = player.getX() - getX();
		if (Math.abs(dx) > 650) { shootTimer = 10; return; }
		int interval = enemyType.equals("SNIPER") || enemyType.equals("TURRET") ? 68
				: enemyType.equals("RUNNER") ? 125 : 92;

		if (Math.abs(dx) < 500) {
			shootTimer = interval;
			moveDir = dx > 0 ? 1 : -1;
			int dir = moveDir;
			Bullet bullet = new Bullet();
			bullet.createElement((dir > 0 ? getX() + 28 : getX() - 10) + "," + (getY() + 10) + ",6,4," + (dir * 6) + ",0,normal,1,enemy");
			ElementManager.getManager().addElement(bullet, GameElement.PLAYFILE);
			CombatEffect flash = new CombatEffect();
			flash.createElement((dir > 0 ? getX() + 34 : getX() - 8) + "," + (getY() + 12) + ",muzzle," + dir + ",");
			ElementManager.getManager().addElement(flash, GameElement.EFFECT);
			attackPose = 12;
		}
	}

	@Override
	public void onHit(ElementObj attacker) {
		if (dying) return;
		if ("enemy".equals(attacker.getFrom())) return;
		setHp(getHp() - (attacker instanceof Bullet ? ((Bullet) attacker).getDamage() : 1));
		CombatEffect hit = new CombatEffect(); hit.createElement((getX() + getW()/2) + "," + (getY() + getH()/2) + ",impact,1,");
		ElementManager.getManager().addElement(hit, GameElement.EFFECT);
		GameMainJPanel.shake(2);
		if (getHp() <= 0) {
			dying = true; deathFrame = 0; deathTimer = 0;
			addScoreToPlayer(); createExplosion(); tryDropItem();
			try { SoundManager.getInstance().playSFX("die"); } catch (Exception e) {}
		}
	}

	private void addScoreToPlayer() {
		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players.isEmpty()) return;
		int pts = enemyType.equals("RUNNER") ? 50 : enemyType.equals("SNIPER") ? 100 : 30;
		players.get(0).setScore(players.get(0).getScore() + pts);
	}

	private void createExplosion() {
		Explosion exp = new Explosion();
		exp.createElement(getX() + "," + getY() + ",30");
		ElementManager.getManager().addElement(exp, GameElement.EFFECT);
	}

	private void tryDropItem() {
		if (rand.nextInt(100) < 20) {
			String[] types = {"WEAPON_H", "WEAPON_S", "WEAPON_F", "HEALTH", "LIFE"};
			ItemDrop item = new ItemDrop();
			item.createElement(getX() + "," + (getY() - 10) + "," + types[rand.nextInt(types.length)]);
			ElementManager.getManager().addElement(item, GameElement.ITEM);
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int idx = split[0].equalsIgnoreCase("ENEMY") ? 1 : 0;
		setX(Integer.parseInt(split[idx])); setY(Integer.parseInt(split[idx + 1]));
		enemyType = split.length > idx + 2 ? split[idx + 2] : "SOLDIER";
		setW(48); setH(52);
		switch (enemyType) {
			case "RUNNER": setHp(1); setMaxHp(1); break;
			case "SNIPER": case "TURRET": setHp(3); setMaxHp(3); break;
			default: setHp(2); setMaxHp(2); break;
		}
		return this;
	}
}
