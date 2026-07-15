package com.tedu.element;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;
import com.tedu.controller.GameThread;
import com.tedu.show.GameMainJPanel;

/**
 * 玩家角色 — 两层精灵组合: 底层=腿脚(stand/run/jump_leg/squat), 上层=身体(attack/knife/jump_body)
 */
public class Player extends ElementObj {

	private boolean left, right, jump, crouch, shoot, melee, throwGrenade, aimUp;
	private boolean onGround = true;
	private int shootCooldown, hurtCooldown, meleeTimer, grenadeCooldown;
	private int ammo = -1, grenades = 5;
	private int facing = 1;
	private static final int DEFAULT_MAX_HP = 20;
	private static int savedScore, savedLives = 3, savedHp = DEFAULT_MAX_HP, savedLevel = 1;
	private static String savedWeapon = "NORMAL";
	private static int savedAmmo = -1, savedGrenades = 5;

	// 两层动画状态
	private String legAnim = "stand", bodyAnim = "";
	private int legFrame, bodyFrame, legTimer, bodyTimer;
	private static final int ANIM_SPEED = 6;

	private static final int GRAVITY = 1;
	private static final int JUMP_FORCE = -15;
	private static final int GROUND_Y = 430;
	private static final int MOVE_SPEED = 5;

	public Player() {
		int level = GameThread.getCurrentLevel();
		if (level <= 1 || level < savedLevel) {
			savedScore = 0; savedLives = 3; savedHp = DEFAULT_MAX_HP; savedWeapon = "NORMAL"; savedAmmo = -1; savedGrenades = 5;
		}
		savedLevel = level;
		setMaxHp(DEFAULT_MAX_HP); setHp(Math.min(savedHp, getMaxHp()));
		setLives(savedLives); setScore(savedScore);
		setWeaponName(savedWeapon);
		ammo = savedAmmo; grenades = savedGrenades;
		setW(52); setH(54);
	}

	@Override
	public void showElement(Graphics g) {
		if (hurtCooldown > 0 && System.currentTimeMillis() / 80 % 2 == 0) return;

		updateAnimState();

		// 底层：腿脚 — 底部对齐
		List<ImageIcon> baseFrames = GameLoad.getSprites("player", legAnim);
		if (baseFrames == null || baseFrames.isEmpty()) baseFrames = GameLoad.getSprites("player", "stand");
		int legTop = drawLayerAt(g, baseFrames, legFrame, getY() + getH(), 5, 5);

		// 上层：身体 — 腿的上面，不重叠；Y下移6px
		// 地面时随面向偏移身体X；跳跃时不偏移(身体居中)
		List<ImageIcon> overFrames = GameLoad.getSprites("player", bodyAnim);
		if (overFrames == null || overFrames.isEmpty()) overFrames = GameLoad.getSprites("player", "attack");
		int bodyLeftOff = onGround ? 50 : 5;
		drawLayerAt(g, overFrames, bodyFrame, legTop + 6, 5, bodyLeftOff);

	}

	/**
	 * 绘制精灵层，底部对齐bottomY，返回该层顶部Y供上层使用
	 */
	private int drawLayerAt(Graphics g, List<ImageIcon> frames, int idx, int bottomY, int baseOff, int leftOff) {
		if (frames == null || frames.isEmpty()) return bottomY;
		ImageIcon frame = frames.get(idx % frames.size());
		if (frame == null || frame.getImage() == null) return bottomY;
		int iw = frame.getIconWidth(), ih = frame.getIconHeight();
		int dy = bottomY - ih;
		if (facing >= 0) {
			g.drawImage(frame.getImage(), getX() - baseOff, dy, iw, ih, null);
		} else {
			g.drawImage(frame.getImage(), getX() - leftOff + iw, dy, -iw, ih, null);
		}
		return dy;
	}

	/**
	 * 更新两层动画状态
	 */
	private void updateAnimState() {
		// 确定腿层动画
		String newLeg;
		if (!onGround) newLeg = "jump_leg";
		else if (crouch && Math.abs(getVx()) > 0) newLeg = "squat_run";
		else if (crouch) newLeg = "squat_stand";
		else if (Math.abs(getVx()) > 0) newLeg = "run";
		else newLeg = "stand";

		// 身体层始终显示：默认attack(持枪姿态)，跳跃用jump_body，近战用knife
		String newBody;
		if (!onGround) newBody = "jump_body";
		else if (meleeTimer > 0) newBody = "knife";
		else newBody = "attack";

		// 切换时重置
		if (!newLeg.equals(legAnim)) { legAnim = newLeg; legFrame = 0; legTimer = 0; }
		if (!newBody.equals(bodyAnim)) { bodyAnim = newBody; bodyFrame = 0; bodyTimer = 0; }

		// 仅对应动作时推进帧
		if (shouldAdvanceLeg()) {
			legTimer++;
			if (legTimer >= ANIM_SPEED) { legTimer = 0; legFrame++; }
		}
		if (!bodyAnim.isEmpty() && shouldAdvanceBody()) {
			bodyTimer++;
			if (bodyTimer >= ANIM_SPEED) { bodyTimer = 0; bodyFrame++; }
		}
	}

	private boolean shouldAdvanceLeg() {
		switch (legAnim) {
			case "stand": case "squat_stand": return false;
			case "run": return Math.abs(getVx()) > 0 && onGround;
			case "jump_leg": return !onGround;
			case "squat_run": return crouch && Math.abs(getVx()) > 0;
			default: return true;
		}
	}

	private boolean shouldAdvanceBody() {
		switch (bodyAnim) {
			case "jump_body": return !onGround;
			case "attack": return shoot && onGround;  // 仅开枪时动画，否则持枪静止
			case "knife": return meleeTimer > 0;
			default: return false;
		}
	}

	@Override
	public void keyClick(boolean bl, int key) {
		switch (key) {
			case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
				left = bl; if (bl) facing = -1; break;
			case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
				right = bl; if (bl) facing = 1; break;
			case KeyEvent.VK_W:
				if (bl && onGround) { jump = true; onGround = false; setVy(JUMP_FORCE); }
				break;
			case KeyEvent.VK_UP: case KeyEvent.VK_I:
				aimUp = bl; break;
			case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
				crouch = bl; if (bl && onGround) setVy(0); break;
			case KeyEvent.VK_J: case KeyEvent.VK_SPACE:
				shoot = bl;
				if (bl && onGround) melee = true;
				break;
			case KeyEvent.VK_K: case KeyEvent.VK_CONTROL:
				if (bl) throwGrenade = true;
				break;
		}
	}

	@Override
	protected void move() {
		int speed = crouch ? 1 : MOVE_SPEED;
		if (left && right) setVx(0);
		else if (left) { setVx(-speed); facing = -1; }
		else if (right) { setVx(speed); facing = 1; }
		else setVx(0);

		if (!onGround) setVy(getVy() + GRAVITY);

		int h = getH(), prevBottom = getY() + h;
		setX(getX() + getVx());
		constrainByLivingBoss();
		if (GameThread.isCombatActive()) {
			setX(Math.max(GameThread.getCombatLeft(), Math.min(GameThread.getCombatRight() - getW(), getX())));
		}
		if (onGround && !hasSupportBelow(h)) { onGround = false; setVy(Math.max(getVy(), GRAVITY)); }
		setY(getY() + getVy());

		if (getVy() >= 0) {
			int landingY = Integer.MAX_VALUE;
			List<ElementObj> maps = ElementManager.getManager().getElementsByKey(GameElement.MAPS);
			for (ElementObj map : maps) {
				if (!(map instanceof Platform)) continue;
				if (getX() + getW() > map.getX() && getX() < map.getX() + map.getW()
						&& prevBottom <= map.getY() && getY() + h >= map.getY())
					landingY = Math.min(landingY, map.getY() - h);
			}
			if (landingY != Integer.MAX_VALUE) { setY(landingY); setVy(0); onGround = true; jump = false; }
		}
		if (getY() + h >= GROUND_Y) { setY(GROUND_Y - h); setVy(0); onGround = true; jump = false; }

		if (getX() < 0) setX(0); if (getX() > 2320) setX(2320);
		if (getY() < 0) { setY(0); setVy(0); }
	}

	private boolean hasSupportBelow(int h) {
		int feetY = getY() + h;
		for (ElementObj map : ElementManager.getManager().getElementsByKey(GameElement.MAPS)) {
			if (!(map instanceof Platform)) continue;
			if (getX() + getW() > map.getX() && getX() < map.getX() + map.getW()
					&& Math.abs(feetY - map.getY()) <= 1) return true;
		}
		return false;
	}

	@Override
	protected void add(long gameTime) {
		if (shootCooldown > 0) shootCooldown--;
		if (hurtCooldown > 0) hurtCooldown--;
		if (meleeTimer > 0) meleeTimer--;
		if (grenadeCooldown > 0) grenadeCooldown--;

		if (throwGrenade && grenades > 0 && grenadeCooldown == 0) {
			throwGrenade = false; grenades--; grenadeCooldown = 35;
			Bullet bomb = new Bullet();
			int bx = facing > 0 ? getX() + getW() : getX() - 14;
			bomb.createElement(bx + "," + (getY() + 8) + ",14,14," + (facing * 6) + ",-9,grenade,4,player");
			ElementManager.getManager().addElement(bomb, GameElement.PLAYFILE);
		} else if (throwGrenade) throwGrenade = false;

		// 近战攻击
		if (melee && onGround && meleeTimer <= 0 && shootCooldown <= 0) {
			ElementObj nearby = findNearbyEnemy();
			if (nearby != null) {
				meleeTimer = 18;
				shootCooldown = 18;
				nearby.onHit(this);
				if (nearby.isLive()) nearby.onHit(this);
				try { SoundManager.getInstance().playSFX("knife"); } catch (Exception e) {}
			}
			melee = false;
		}

		if (shoot && shootCooldown == 0) fire();
		saveProgress();
	}

	private ElementObj findNearbyEnemy() {
		int range = 70;
		for (ElementObj enemy : ElementManager.getManager().getElementsByKey(GameElement.ENEMY)) {
			if (!enemy.isLive()) continue;
			if (Math.abs(getX() - enemy.getX()) < range
					&& Math.abs(getY() - enemy.getY()) < range)
				return enemy;
		}
		for (ElementObj boss : ElementManager.getManager().getElementsByKey(GameElement.BOSS)) {
			if (!boss.isLive()) continue;
			if (Math.abs(getX() - boss.getX()) < range + 60
					&& Math.abs(getY() - boss.getY()) < range + 40)
				return boss;
		}
		return null;
	}

	private void constrainByLivingBoss() {
		for (ElementObj boss : ElementManager.getManager().getElementsByKey(GameElement.BOSS)) {
			if (!boss.isLive()) continue;
			int limit = boss.getX() - 120;
			if (getX() > limit && getX() < boss.getX() + boss.getW()) setX(limit);
		}
	}

	private void saveProgress() {
		savedScore = getScore(); savedLives = getLives();
		savedHp = Math.max(1, getHp()); savedWeapon = getWeaponName(); savedAmmo = ammo; savedGrenades = grenades;
		savedLevel = GameThread.getCurrentLevel();
	}

	private void fire() {
		if (ammo == 0) { setWeaponName("NORMAL"); ammo = -1; }
		int bw = 8, bh = 4, speed = 12, damage = 1;
		String type = "normal";
		switch (getWeaponName()) {
			case "HEAVY": bw = 14; bh = 6; speed = 14; damage = 2; type = "heavy"; shootCooldown = 7; break;
			case "SPREAD": bw = 6; bh = 4; speed = 10; damage = 1; type = "spread"; shootCooldown = 14; break;
			case "FLAME": bw = 18; bh = 10; speed = 6; damage = 3; type = "flame"; shootCooldown = 18; break;
			default: bw = 8; bh = 4; speed = 12; damage = 1; type = "normal"; shootCooldown = 10; break;
		}

		int bulletVx = facing * speed, bulletVy = 0;
		int bx = facing > 0 ? getX() + getW() - 10 : getX() - bw - 5;
		int by = crouch ? getY() + 30 : getY() + 18;
		if (aimUp) {
			if (Math.abs(getVx()) > 0) { bulletVx = facing * Math.max(4, speed * 7 / 10); bulletVy = -Math.max(4, speed * 7 / 10); }
			else { bulletVx = 0; bulletVy = -speed; }
			bx = getX() + getW() / 2; by = getY() - bh;
		} else if (!onGround && crouch) {
			bulletVx = 0; bulletVy = speed; bx = getX() + getW() / 2; by = getY() + getH();
		}

		Bullet bullet = new Bullet();
		bullet.createElement(bx + "," + by + "," + bw + "," + bh + "," + bulletVx + "," + bulletVy + "," + type + "," + damage + ",player");
		ElementManager.getManager().addElement(bullet, GameElement.PLAYFILE);
		spawnEffect(bx + (bulletVx > 0 ? bw + 6 : bulletVx < 0 ? -2 : 0), by + bh / 2, "muzzle", bulletVx == 0 ? facing : Integer.signum(bulletVx), "");

		if ("SPREAD".equals(getWeaponName()) && !aimUp && !(!onGround && crouch)) {
			for (int ang = -1; ang <= 1; ang += 2) {
				Bullet b2 = new Bullet();
				b2.createElement(bx + "," + by + "," + bw + "," + bh + "," + (int)(facing * speed * 0.65) + "," + (ang * 3) + ",spread," + damage + ",player");
				ElementManager.getManager().addElement(b2, GameElement.PLAYFILE);
			}
		}
		try { SoundManager.getInstance().playSFX("fire"); } catch (Exception e) {}
		if (ammo > 0 && --ammo == 0) { setWeaponName("NORMAL"); ammo = -1; }
	}

	private void spawnEffect(int x, int y, String type, int dir, String label) {
		CombatEffect effect = new CombatEffect();
		effect.createElement(x + "," + y + "," + type + "," + dir + "," + label);
		ElementManager.getManager().addElement(effect, GameElement.EFFECT);
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		setX(Integer.parseInt(split[0])); setY(Integer.parseInt(split[1]));
		setW(52); setH(54); return this;
	}

	@Override
	public void onHit(ElementObj attacker) {
		if (hurtCooldown > 0) return;
		int damage = attacker instanceof Bullet ? ((Bullet) attacker).getDamage() : 1;
		setHp(getHp() - damage);
		hurtCooldown = 45;
		setX(getX() + (attacker.getX() < getX() ? 18 : -18));
		CombatEffect hurt = new CombatEffect(); hurt.createElement((getX()+getW()/2) + "," + (getY()+20) + ",impact,1,");
		ElementManager.getManager().addElement(hurt, GameElement.EFFECT);
		GameMainJPanel.shake(3);
		if (getHp() <= 0) {
			if (getLives() > 1) {
				setLives(getLives() - 1); setHp(getMaxHp());
				int checkpoint = GameThread.isCombatActive() ? GameThread.getCombatLeft() + 55
						: Math.max(80, GameMainJPanel.cameraX + 90);
				setX(Math.min(2050, checkpoint)); setY(382); setVy(0); setVx(0);
				onGround = true; hurtCooldown = 150;
				try { SoundManager.getInstance().playSFX("die"); } catch (Exception e) {}
			} else {
				setLives(0); setLive(false);
				try { SoundManager.getInstance().playSFX("die"); } catch (Exception e) {}
			}
		}
	}

	@Override
	public Rectangle getRectangle() {
		int rectH = crouch ? 28 : getH();
		return new Rectangle(getX() + 8, getY() + 8, getW() - 16, rectH - 8);
	}

	@Override
	public void onPickItem(ElementObj item) {
		String t = item.getItemType(); if (t == null) return;
		switch (t) {
			case "WEAPON_H": setWeaponName("HEAVY"); ammo = 140; break;
			case "WEAPON_S": setWeaponName("SPREAD"); ammo = 45; break;
			case "WEAPON_F": setWeaponName("FLAME"); ammo = 60; break;
			case "HEALTH": setHp(Math.min(getHp() + 8, getMaxHp())); break;
			case "LIFE": setLives(getLives() + 1); grenades = Math.min(9, grenades + 2); break;
		}
		spawnEffect(getX(), getY(), "text", 1, pickupLabel(t));
	}

	private String pickupLabel(String type) {
		switch (type) {
			case "WEAPON_H": return "HEAVY MACHINE GUN!";
			case "WEAPON_S": return "SHOTGUN!";
			case "WEAPON_F": return "FLAME SHOT!";
			case "HEALTH": return "RECOVER!";
			default: return "1UP!";
		}
	}

	public int getAmmo() { return ammo; }
	public int getGrenades() { return grenades; }
}
