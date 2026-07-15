package com.tedu.element;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

final class SpritePainter {
    private SpritePainter() {}

    static void pixelMode(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
    }

    static void player(Graphics g, int x, int y, int w, int h, int facing, boolean crouch,
            boolean moving, boolean onGround, boolean shooting, String weapon, int frame, boolean hurt) {
        pixelMode(g);
        if (hurt && frame % 4 < 2) return;

        int dir = facing >= 0 ? 1 : -1;
        int bodyY = crouch ? y + 8 : y + 14;
        int bodyH = crouch ? h - 8 : h - 16;
        int legSwing = moving && onGround ? (frame / 5 % 2 == 0 ? 4 : -2) : 0;

        g.setColor(new Color(28, 32, 24));
        g.fillRect(x + 7, bodyY + 5, w - 8, bodyH);
        g.setColor(new Color(218, 174, 122));
        g.fillRect(x + 10, y + 2, 16, 14);
        g.setColor(new Color(248, 204, 148));
        g.fillRect(x + 14 + (dir > 0 ? 4 : -2), y + 7, 5, 4);

        g.setColor(new Color(68, 86, 42));
        g.fillRect(x + 7, y, 20, 7);
        g.setColor(new Color(98, 126, 48));
        g.fillRect(x + 9, y - 3, 16, 5);
        g.setColor(new Color(46, 58, 30));
        g.fillRect(x + 5, y + 5, 25, 4);

        g.setColor(new Color(76, 118, 54));
        g.fillRect(x + 8, bodyY, w - 14, bodyH - 4);
        g.setColor(new Color(116, 150, 70));
        g.fillRect(x + 12, bodyY + 3, w - 22, 6);
        g.setColor(new Color(48, 72, 40));
        g.fillRect(x + 11, bodyY + 12, 5, bodyH - 14);
        g.fillRect(x + w - 15, bodyY + 12, 5, bodyH - 14);

        if (!crouch) {
            g.setColor(new Color(42, 52, 38));
            g.fillRect(x + 8, y + h - 10, 8, 10);
            g.fillRect(x + w - 15 + legSwing, y + h - 10, 8, 10);
            g.setColor(new Color(28, 30, 26));
            g.fillRect(x + 6, y + h - 3, 12, 4);
            g.fillRect(x + w - 17 + legSwing, y + h - 3, 12, 4);
        }

        int gunY = crouch ? y + 18 : y + 24;
        int gunX = dir > 0 ? x + w - 2 : x - 18;
        g.setColor(weaponColor(weapon));
        g.fillRect(gunX, gunY, 20, 5);
        g.setColor(new Color(30, 30, 30));
        g.fillRect(gunX + (dir > 0 ? 15 : 0), gunY - 1, 7, 7);
        if (shooting && frame % 6 < 3) {
            int fx = dir > 0 ? gunX + 22 : gunX - 10;
            g.setColor(new Color(255, 238, 90));
            g.fillRect(fx, gunY - 2, 10, 9);
            g.setColor(new Color(255, 124, 28));
            g.fillRect(fx + (dir > 0 ? 5 : -1), gunY, 6, 5);
        }
    }

    static void enemy(Graphics g, String type, int x, int y, int w, int h, int dir, int frame) {
        pixelMode(g);
        Color coat = new Color(128, 130, 74);
        Color trim = new Color(88, 92, 54);
        if ("RUNNER".equals(type)) {
            coat = new Color(170, 58, 48);
            trim = new Color(118, 32, 30);
        } else if ("SNIPER".equals(type)) {
            coat = new Color(56, 92, 148);
            trim = new Color(32, 50, 92);
        }
        int swing = frame / 8 % 2 == 0 ? 2 : -2;
        g.setColor(new Color(38, 30, 24));
        g.fillRect(x + 6, y + 12, w - 9, h - 12);
        g.setColor(coat);
        g.fillRect(x + 5, y + 14, w - 10, h - 20);
        g.setColor(trim);
        g.fillRect(x + 8, y + 20, w - 16, 6);
        g.fillRect(x + 7, y + h - 9, 7, 9);
        g.fillRect(x + w - 13 + swing, y + h - 9, 7, 9);
        g.setColor(new Color(220, 166, 118));
        g.fillRect(x + 8, y + 3, w - 16, 12);
        g.setColor(trim);
        g.fillRect(x + 5, y, w - 10, 6);
        g.fillRect(x + 2, y + 4, w - 4, 3);
        g.setColor(Color.WHITE);
        g.fillRect(x + (dir > 0 ? w - 10 : 8), y + 8, 3, 3);
        g.setColor(Color.BLACK);
        g.fillRect(x + (dir > 0 ? w - 9 : 8), y + 9, 2, 2);
        int gunX = dir > 0 ? x + w - 1 : x - 15;
        g.setColor(new Color(45, 45, 45));
        g.fillRect(gunX, y + 21, 17, 4);
        if ("SNIPER".equals(type)) {
            g.setColor(new Color(190, 190, 90));
            g.drawRect(gunX + (dir > 0 ? 12 : 0), y + 17, 5, 5);
        }
    }

    static void boss(Graphics g, int x, int y, int w, int h, int dir, int hp, int maxHp, int frame) {
        pixelMode(g);
        g.setColor(new Color(33, 39, 42));
        g.fillRect(x + 5, y + 23, w - 10, h - 22);
        g.setColor(new Color(87, 101, 96));
        g.fillRect(x + 10, y + 30, w - 20, h - 38);
        g.setColor(new Color(124, 52, 38));
        g.fillRect(x + 16, y + 8, w - 32, 26);
        g.setColor(new Color(190, 74, 42));
        g.fillRect(x + 22, y + 12, w - 44, 8);
        g.setColor(new Color(22, 24, 22));
        g.fillRect(x + 12, y + h - 14, 20, 14);
        g.fillRect(x + w - 32, y + h - 14, 20, 14);
        g.setColor(new Color(250, 196, 70));
        g.fillRect(x + (dir > 0 ? w - 26 : 16), y + 17, 9, 7);
        int gunX = dir > 0 ? x + w - 2 : x - 35;
        g.setColor(new Color(42, 42, 45));
        g.fillRect(gunX, y + 48, 38, 11);
        g.setColor(new Color(95, 95, 102));
        g.fillRect(gunX + 5, y + 45, 25, 5);
        if (frame % 30 < 5) {
            int fx = dir > 0 ? gunX + 38 : gunX - 12;
            g.setColor(new Color(255, 224, 70));
            g.fillRect(fx, y + 46, 12, 12);
        }
        g.setColor(Color.RED);
        g.fillRect(x, y - 14, w, 7);
        g.setColor(new Color(70, 235, 85));
        g.fillRect(x, y - 14, Math.max(0, w * hp / maxHp), 7);
        g.setColor(Color.WHITE);
        g.drawRect(x, y - 14, w, 7);
    }

    static void crate(Graphics g, int x, int y, int w, int h) {
        pixelMode(g);
        g.setColor(new Color(84, 52, 24));
        g.fillRect(x, y, w, h);
        g.setColor(new Color(176, 112, 48));
        g.fillRect(x + 3, y + 3, w - 6, h - 6);
        g.setColor(new Color(112, 66, 28));
        g.drawRect(x, y, w - 1, h - 1);
        g.drawLine(x + 4, y + 4, x + w - 5, y + h - 5);
        g.drawLine(x + w - 5, y + 4, x + 4, y + h - 5);
        g.fillRect(x + w / 2 - 2, y + 2, 4, h - 4);
        g.fillRect(x + 2, y + h / 2 - 2, w - 4, 4);
    }

    static void barrel(Graphics g, int x, int y, int w, int h) {
        pixelMode(g);
        g.setColor(new Color(50, 52, 55));
        g.fillRoundRect(x + 4, y, w - 8, h, 8, 8);
        g.setColor(new Color(116, 118, 122));
        g.fillRoundRect(x + 7, y + 3, w - 14, h - 6, 6, 6);
        g.setColor(new Color(62, 64, 68));
        g.fillRect(x + 5, y + 8, w - 10, 4);
        g.fillRect(x + 5, y + h - 12, w - 10, 4);
        g.setColor(new Color(230, 70, 38));
        g.fillOval(x + w / 2 - 7, y + h / 2 - 7, 14, 14);
        g.setColor(new Color(255, 212, 68));
        g.drawString("!", x + w / 2 - 3, y + h / 2 + 5);
    }

    static void item(Graphics g, String type, int x, int y, int w, int h, boolean flash) {
        pixelMode(g);
        Color c = new Color(230, 190, 54);
        String label = "H";
        if ("WEAPON_S".equals(type)) { c = new Color(240, 126, 42); label = "S"; }
        else if ("WEAPON_F".equals(type)) { c = new Color(235, 62, 35); label = "F"; }
        else if ("HEALTH".equals(type)) { c = new Color(80, 220, 95); label = "+"; }
        else if ("LIFE".equals(type)) { c = new Color(250, 230, 80); label = "1"; }
        if (!flash) c = c.darker();
        g.setColor(new Color(30, 34, 36));
        g.fillRect(x - 2, y - 2, w + 4, h + 4);
        g.setColor(c);
        g.fillRect(x, y, w, h);
        g.setColor(Color.WHITE);
        g.fillRect(x + 3, y + 3, w - 6, 3);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 15));
        g.drawString(label, x + 6, y + 16);
    }

    static void platform(Graphics g, String type, int x, int y, int w, int h) {
        pixelMode(g);
        if ("GROUND".equals(type)) {
            g.setColor(new Color(74, 86, 52));
            g.fillRect(x, y, w, h);
            g.setColor(new Color(164, 132, 72));
            g.fillRect(x, y, w, 10);
            g.setColor(new Color(74, 122, 72));
            g.fillRect(x, y - 5, w, 7);
            g.setColor(new Color(44, 76, 46));
            for (int i = 0; i < w; i += 12) g.fillRect(x + i, y - 8 + i % 3, 7, 5);
            g.setColor(new Color(52, 58, 46));
            for (int i = 0; i < w; i += 32) g.fillRect(x + i, y + 25, 16, 6);
        } else {
            g.setColor(new Color(72, 78, 82));
            g.fillRect(x, y, w, h);
            g.setColor(new Color(148, 154, 150));
            g.fillRect(x, y, w, 4);
            g.setColor(new Color(44, 48, 50));
            g.drawRect(x, y, w - 1, h - 1);
            for (int i = 10; i < w; i += 30) g.drawLine(x + i, y + 4, x + i - 8, y + h - 2);
        }
    }

    static void bullet(Graphics g, String type, int x, int y, int w, int h, int frame) {
        pixelMode(g);
        if ("flame".equals(type)) {
            g.setColor(new Color(255, 68, 24));
            g.fillOval(x - 5, y - 3, w + 10, h + 6);
            g.setColor(new Color(255, 216, 64));
            g.fillOval(x, y - 1, w, h + 2);
        } else if ("heavy".equals(type)) {
            g.setColor(new Color(188, 194, 202));
            g.fillRect(x, y, w, h);
            g.setColor(Color.WHITE);
            g.fillRect(x + 2, y + 1, w - 4, 2);
        } else if ("spread".equals(type)) {
            g.setColor(new Color(255, 150, 44));
            g.fillOval(x, y, w, h);
            g.setColor(new Color(255, 238, 100));
            g.fillOval(x + 1, y + 1, Math.max(2, w - 2), Math.max(2, h - 2));
        } else {
            g.setColor(new Color(255, 238, 82));
            g.fillOval(x, y, w, h);
            g.setColor(Color.WHITE);
            g.fillOval(x + 1, y + 1, Math.max(2, w - 3), Math.max(2, h - 3));
        }
    }

    static void explosion(Graphics g, int cx, int cy, int radius, int frame) {
        pixelMode(g);
        int spikes = 8;
        Polygon p = new Polygon();
        for (int i = 0; i < spikes * 2; i++) {
            double a = Math.PI * i / spikes + frame * 0.12;
            int r = i % 2 == 0 ? radius : radius / 2;
            p.addPoint(cx + (int) (Math.cos(a) * r), cy + (int) (Math.sin(a) * r));
        }
        g.setColor(new Color(230, 55, 24));
        g.fillPolygon(p);
        g.setColor(new Color(255, 178, 50));
        g.fillOval(cx - radius / 2, cy - radius / 2, radius, radius);
        g.setColor(new Color(255, 246, 160));
        g.fillOval(cx - radius / 4, cy - radius / 4, Math.max(2, radius / 2), Math.max(2, radius / 2));
    }

    private static Color weaponColor(String weapon) {
        if ("HEAVY".equals(weapon)) return new Color(148, 154, 160);
        if ("SPREAD".equals(weapon)) return new Color(218, 112, 46);
        if ("FLAME".equals(weapon)) return new Color(210, 66, 28);
        return new Color(68, 68, 70);
    }
}
