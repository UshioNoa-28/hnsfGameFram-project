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

/**
 * 玩家角色 — 两层精灵组合: 底层=腿脚(stand/run/jump_leg/squat), 上层=身体(attack/knife/jump_body)
 */
public class Player extends ElementObj {

	private boolean left, right, jump, crouch, shoot, melee;
	private boolean onGround = true;
	private int shootCooldown, hurtCooldown, meleeTimer;
	private int facing = 1;
	private static int savedScore, savedLives = 3, savedHp = 8, savedLevel = 1;
	private static String savedWeapon = "NORMAL";

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
			savedScore = 0; savedLives = 3; savedHp = 8; savedWeapon = "NORMAL";
		}
		savedLevel = level;
		setHp(savedHp); setMaxHp(8);
		setLives(savedLives); setScore(savedScore);
		setWeaponName(savedWeapon);
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

		// 头顶血条
		int drawW = getW(), drawH = getH();
		int hpW = Math.max(1, drawW * Math.max(0, getHp()) / Math.max(1, getMaxHp()));
		g.setColor(java.awt.Color.RED);
		g.fillRect(getX(), getY() - 7, drawW, 3);
		g.setColor(java.awt.Color.GREEN);
		g.fillRect(getX(), getY() - 7, hpW, 3);
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
			case KeyEvent.VK_W: case KeyEvent.VK_UP:
				if (bl && onGround) { jump = true; onGround = false; setVy(JUMP_FORCE); }
				break;
			case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
				crouch = bl; if (bl && onGround) setVy(0); break;
			case KeyEvent.VK_J: case KeyEvent.VK_SPACE:
				shoot = bl;
				if (bl && onGround) melee = true;
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

		// 近战攻击
		if (melee && onGround && meleeTimer <= 0 && shootCooldown <= 0) {
			ElementObj nearby = findNearbyEnemy();
			if (nearby != null) {
				meleeTimer = 18;
				shootCooldown = 18;
				nearby.onHit(this);
				try { SoundManager.getInstance().playSFX("fire"); } catch (Exception e) {}
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
		savedHp = Math.max(1, getHp()); savedWeapon = getWeaponName();
		savedLevel = GameThread.getCurrentLevel();
	}

	private void fire() {
		int bw = 8, bh = 4, speed = 12, damage = 1;
		String type = "normal";
		switch (getWeaponName()) {
			case "HEAVY": bw = 14; bh = 6; speed = 14; damage = 2; type = "heavy"; shootCooldown = 7; break;
			case "SPREAD": bw = 6; bh = 4; speed = 10; damage = 1; type = "spread"; shootCooldown = 14; break;
			case "FLAME": bw = 18; bh = 10; speed = 6; damage = 3; type = "flame"; shootCooldown = 18; break;
			default: bw = 8; bh = 4; speed = 12; damage = 1; type = "normal"; shootCooldown = 10; break;
		}

		int bx = facing > 0 ? getX() + getW() - 10 : getX() - bw - 5;
		int by = crouch ? getY() + 28 : getY() + 20;

		Bullet bullet = new Bullet();
		bullet.createElement(bx + "," + by + "," + bw + "," + bh + "," + (facing * speed) + ",0," + type + "," + damage + ",player");
		ElementManager.getManager().addElement(bullet, GameElement.PLAYFILE);

		if ("SPREAD".equals(getWeaponName())) {
			for (int ang = -1; ang <= 1; ang += 2) {
				Bullet b2 = new Bullet();
				b2.createElement(bx + "," + by + "," + bw + "," + bh + "," + (int)(facing * speed * 0.65) + "," + (ang * 3) + ",spread," + damage + ",player");
				ElementManager.getManager().addElement(b2, GameElement.PLAYFILE);
			}
		}
		try { SoundManager.getInstance().playSFX("fire"); } catch (Exception e) {}
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
		if (getHp() <= 0) {
			if (getLives() > 1) {
				setLives(getLives() - 1); setHp(getMaxHp());
				setX(80); setY(382); setVy(0); setVx(0);
				onGround = true; hurtCooldown = 90;
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
			case "WEAPON_H": setWeaponName("HEAVY"); break;
			case "WEAPON_S": setWeaponName("SPREAD"); break;
			case "WEAPON_F": setWeaponName("FLAME"); break;
			case "HEALTH": setHp(Math.min(getHp() + 3, getMaxHp())); break;
			case "LIFE": setLives(getLives() + 1); break;
		}
	}
}
