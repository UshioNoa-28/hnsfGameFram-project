package com.tedu.element;

import java.awt.Graphics;
import java.util.List;

import javax.swing.ImageIcon;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;
import com.tedu.manager.SoundManager;

/**
 * 飞机敌人 — 合金弹头飞机精灵 (fly_0左飞/fly_1右飞)
 */
public class Aircraft extends ElementObj {

	private int direction = 1;
	private int bombTimer = 0;
	private int animFrame = 0, animTimer = 0;

	public Aircraft() { setW(80); setH(40); setHp(5); setMaxHp(5); setFrom("enemy"); }

	@Override
	public void showElement(Graphics g) {
		String dirKey = direction >= 0 ? "fly_right" : "fly_left";
		List<ImageIcon> frames = GameLoad.getSprites("aircraft", dirKey);
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("aircraft", "plane");
		if (frames == null || frames.isEmpty()) frames = GameLoad.getSprites("aircraft");

		ImageIcon frame = null;
		if (frames != null && !frames.isEmpty()) frame = frames.get(animFrame % frames.size());

		if (frame != null && frame.getImage() != null) {
			int iw = frame.getIconWidth(), ih = frame.getIconHeight();
			g.drawImage(frame.getImage(), getX(), getY(), iw, ih, null);
		} else {
			g.setColor(new java.awt.Color(100, 110, 130)); g.fillRect(getX(), getY(), getW(), getH());
		}

		if (getHp() < getMaxHp()) {
			int hpW = getW() * getHp() / getMaxHp();
			g.setColor(java.awt.Color.RED); g.fillRect(getX(), getY() - 6, getW(), 3);
			g.setColor(java.awt.Color.GREEN); g.fillRect(getX(), getY() - 6, hpW, 3);
		}
	}

	@Override
	protected void move() {
		setX(getX() + getVx());
		if (direction > 0 && getX() > 2600) setLive(false);
		if (direction < 0 && getX() < -200) setLive(false);
		setY(getY() + (int)(Math.sin(System.currentTimeMillis() / 500.0) * 2));
		animTimer++; if (animTimer >= 6) { animTimer = 0; animFrame++; }
	}

	@Override
	protected void add(long gameTime) {
		if (!isLive() || --bombTimer > 0) return;
		List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
		if (players == null || players.isEmpty()) return;
		ElementObj player = players.get(0);

		if (Math.abs(getX() - player.getX()) < 350) {
			bombTimer = 40 + new java.util.Random().nextInt(30);
			Bullet bomb = new Bullet();
			bomb.createElement((getX() + getW() / 2) + "," + (getY() + getH()) + ",14,14,0,3,heavy,2,enemy");
			ElementManager.getManager().addElement(bomb, GameElement.PLAYFILE);
			try { SoundManager.getInstance().playSFX("bomb"); } catch (Exception e) {}
		}
	}

	@Override
	public void onHit(ElementObj attacker) {
		if ("enemy".equals(attacker.getFrom())) return;
		setHp(getHp() - (attacker instanceof Bullet ? ((Bullet) attacker).getDamage() : 1));
		if (getHp() <= 0) {
			setLive(false);
			List<ElementObj> players = ElementManager.getManager().getElementsByKey(GameElement.PLAY);
			if (!players.isEmpty()) players.get(0).setScore(players.get(0).getScore() + 300);
			Explosion exp = new Explosion();
			exp.createElement(getX() + "," + getY() + ",45");
			ElementManager.getManager().addElement(exp, GameElement.EFFECT);
			try { SoundManager.getInstance().playSFX("die"); } catch (Exception e) {}
		}
	}

	@Override
	public ElementObj createElement(String str) {
		String[] split = str.split(",");
		int offset = split[0].equalsIgnoreCase("AIRCRAFT") ? 1 : 0;
		setX(Integer.parseInt(split[offset])); setY(Integer.parseInt(split[offset + 1]));
		direction = Integer.parseInt(split[offset + 2]);
		setVx(direction * 3); return this;
	}
}
