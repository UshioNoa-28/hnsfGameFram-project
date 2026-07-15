package com.tedu.manager;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;

/**
 * 资源加载器 — 加载合金弹头精灵、音效、地图
 */
public class GameLoad {
	private static ElementManager em = ElementManager.getManager();

	public static Map<String, ImageIcon> imgMap = new HashMap<>();

	/**
	 * 精灵分组缓存: 外层key=类别(player/enemy/boss...), 内层key=动画名(run/attack/jump...)
	 */
	public static Map<String, Map<String, List<ImageIcon>>> spriteGroups = new LinkedHashMap<>();

	private static Properties pro = new Properties();

	/** 合金弹头资源根目录 */
	private static final String MS_ASSET_ROOT = "../合金弹头/合金弹头/images";

	/** 背景图片 */
	public static final List<Image> bgImages = new ArrayList<>();

	// 方向处理模式
	private static final int DIR_NORMAL = 0;       // 保留所有帧，按名称分组
	private static final int DIR_FILTER_RIGHT = 1; // 仅保留方向=1(右)的帧
	private static final int DIR_SPLIT = 2;        // 按方向拆分为 anim_0 / anim_1

	/**
	 * 加载关卡地图
	 */
	public static void MapLoad(int levelId) {
		String mapName = "com/tedu/text/lev" + levelId + ".map";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream maps = classLoader.getResourceAsStream(mapName);
		if (maps == null) {
			System.out.println("关卡配置文件读取异常: " + mapName);
			return;
		}
		try {
			pro.clear();
			pro.load(maps);
			Enumeration<?> names = pro.propertyNames();
			while (names.hasMoreElements()) {
				String key = names.nextElement().toString();
				String value = pro.getProperty(key);
				String[] entries = value.split(";");

				if ("END".equalsIgnoreCase(key)) {
					if (entries.length > 0) {
						String[] coord = entries[0].split(",");
						int endX = Integer.parseInt(coord[0].trim());
						com.tedu.show.GameMainJPanel.setLevelEndX(endX);
					}
					continue;
				}

				for (int i = 0; i < entries.length; i++) {
					String entry = entries[i].trim();
					if (entry.isEmpty()) continue;
					ElementObj obj = createElementByType(key, entry);
					if (obj != null) {
						GameElement ge = mapTypeToGameElement(key);
						em.addElement(obj, ge);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static ElementObj createElementByType(String type, String data) {
		String fullStr = type + "," + data;
		ElementObj proto = getObj(type.toLowerCase());
		if (proto != null) {
			return proto.createElement(fullStr);
		}
		return null;
	}

	private static GameElement mapTypeToGameElement(String type) {
		switch (type.toUpperCase()) {
			case "GROUND":
			case "PLATFORM":
			case "BARRIER":
			case "BACKGROUND":
				return GameElement.MAPS;
			case "ENEMY":
				return GameElement.ENEMY;
			case "BOSS":
				return GameElement.BOSS;
			case "ITEM":
				return GameElement.ITEM;
			case "HOSTAGE":
				return GameElement.ITEM;
			case "AIRCRAFT":
				return GameElement.ENEMY;
			default:
				return GameElement.MAPS;
		}
	}

	/**
	 * 加载所有图片资源
	 */
	public static void loadImg() {
		String texturl = "com/tedu/text/GameData.pro";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream texts = classLoader.getResourceAsStream(texturl);
		spriteGroups.clear();
		pro.clear();
		if (texts != null) try {
			pro.load(texts);
			Set<Object> set = pro.keySet();
			for (Object o : set) {
				String key = o.toString();
				String url = pro.getProperty(key);
				if (url.endsWith(".png") || url.endsWith(".ico")) {
					ImageIcon icon = new ImageIcon(url);
					if (icon.getIconWidth() > 0) {
						imgMap.put(key, icon);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 从合金弹头资源目录加载精灵
		String root = resolveAssetRoot();
		System.out.println("[GameLoad] 合金弹头资源路径: " + new File(root).getAbsolutePath());

		loadSpriteDir("player", root + "/plays", DIR_FILTER_RIGHT);
		loadSpriteDir("enemy", root + "/Enemy");
		// 敌人子分组: 不同行为使用不同帧
		{ String ed = root + "/Enemy";
		addSpriteFile("enemy", "stand_shoot", ed + "/20070130041731OFQDOJuj-103.png");
		addSpriteFile("enemy", "stand_shoot2", ed + "/20070130041731OFQDOJuj-227.png");
		for (int i = 174; i <= 178; i++) addSpriteFile("enemy", "walk_shoot", ed + "/20070130041731OFQDOJuj-" + i + ".png");
		addSpriteFile("enemy", "walk_shoot_idle", ed + "/20070130041731OFQDOJuj-176.png");
		for (int i = 179; i <= 183; i++) addSpriteFile("enemy", "walk_shoot2", ed + "/20070130041731OFQDOJuj-" + i + ".png");
		addSpriteFile("enemy", "walk_shoot2_idle", ed + "/20070130041731OFQDOJuj-181.png");
		addSpriteFile("enemy", "static_guard", ed + "/20070130041731OFQDOJuj-202.png");
		for (int i = 117; i <= 120; i++) addSpriteFile("enemy", "death", ed + "/20070130041731OFQDOJuj-" + i + ".png"); }
		loadSpriteDir("boss", root + "/boss");
		loadSpriteDir("explosion", root + "/爆炸");
		loadSpriteDir("bullet", root + "/子弹");
		loadSpriteDir("hostage", root + "/人质");
		loadSpriteDir("aircraft", root + "/飞机");
	addSpriteFile("aircraft", "fly_right", root + "/飞机/plane_fly1.png");
	addSpriteFile("aircraft", "fly_left", root + "/飞机/plane_fly0.png");
	// 加载背景图
	loadBackgrounds(root + "/背景");

		System.out.println("[GameLoad] 精灵加载完成: " + spriteGroups.size() + " 类, 背景: " + bgImages.size() + " 张");
		for (String cat : spriteGroups.keySet()) {
			Map<String, List<ImageIcon>> groups = spriteGroups.get(cat);
			System.out.println("  [" + cat + "] " + groups.size() + " 动画组:");
			for (String anim : groups.keySet()) {
				System.out.println("    - " + anim + ": " + groups.get(anim).size() + " 帧");
			}
		}
	}

	/**
	 * 解析资源根目录
	 */
	private static String resolveAssetRoot() {
		// 先尝试直接路径
		File dir = new File(MS_ASSET_ROOT);
		if (dir.exists() && dir.isDirectory()) return MS_ASSET_ROOT;

		// 尝试从当前目录
		dir = new File("合金弹头/合金弹头/images");
		if (dir.exists() && dir.isDirectory()) return "合金弹头/合金弹头/images";

		// 尝试从user.dir
		String userDir = System.getProperty("user.dir");
		dir = new File(userDir, MS_ASSET_ROOT);
		if (dir.exists() && dir.isDirectory()) return dir.getAbsolutePath();

		dir = new File(userDir, "合金弹头/合金弹头/images");
		if (dir.exists() && dir.isDirectory()) return dir.getAbsolutePath();

		// 最后尝试上级目录
		dir = new File(userDir);
		while (dir.getParent() != null) {
			File msDir = new File(dir, "合金弹头/合金弹头/images");
			if (msDir.exists() && msDir.isDirectory()) return msDir.getAbsolutePath();
			dir = dir.getParentFile();
		}

		System.err.println("[GameLoad] 找不到合金弹头资源目录!");
		return MS_ASSET_ROOT;
	}

	/**
	 * 加载背景图片
	 */
	private static void loadBackgrounds(String dirPath) {
		bgImages.clear();
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			// 尝试从resolveAssetRoot的上级目录查找
			String root = resolveAssetRoot();
			dir = new File(root, "背景");
		}
		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println("[GameLoad] 背景目录不存在: " + dirPath);
			return;
		}
		File[] files = dir.listFiles((d, name) -> {
			String low = name.toLowerCase();
			return low.endsWith(".png") || low.endsWith(".jpg") || low.endsWith(".gif");
		});
		if (files == null) return;
		java.util.Arrays.sort(files, (a, b) -> a.getName().compareTo(b.getName()));
		for (File f : files) {
			try {
				Image img = ImageIO.read(f);
				if (img != null) bgImages.add(img);
			} catch (Exception e) {
				System.out.println("[GameLoad] 背景加载失败: " + f.getName());
			}
		}
	}

	/**
	 * 手动添加单张精灵到指定组
	 */
	private static void addSpriteFile(String category, String groupName, String filePath) {
		File f = new File(filePath);
		if (!f.exists()) { System.out.println("[GameLoad] 文件不存在: " + filePath); return; }
		ImageIcon icon = new ImageIcon(f.getAbsolutePath());
		if (icon.getIconWidth() <= 0) return;
		Map<String, List<ImageIcon>> groups = spriteGroups.get(category);
		if (groups == null) {
			groups = new LinkedHashMap<>();
			spriteGroups.put(category, groups);
		}
		groups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(icon);
	}

	/**
	 * 从目录加载PNG，按文件名前缀自动分组为动画序列
	 */
	private static void loadSpriteDir(String category, String dirPath) {
		loadSpriteDir(category, dirPath, DIR_NORMAL);
	}

	/**
	 * 从目录加载PNG，按文件名前缀自动分组为动画序列
	 * @param dirMode DIR_NORMAL=保留所有 / DIR_FILTER_RIGHT=仅保留方向1 / DIR_SPLIT=按方向拆分为 _0/_1 组
	 */
	private static void loadSpriteDir(String category, String dirPath, int dirMode) {
		File dir = new File(dirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println("[GameLoad] 目录不存在: " + dirPath);
			return;
		}

		File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
		if (files == null || files.length == 0) {
			System.out.println("[GameLoad] 空目录: " + dirPath);
			return;
		}

		// 自然排序
		java.util.Arrays.sort(files, (a, b) -> naturalCompare(a.getName(), b.getName()));

		// 按文件名前缀分组
		Map<String, List<ImageIcon>> groups = new LinkedHashMap<>();

		for (File f : files) {
			String name = f.getName();
			String nameForGroup = name;
			String dirSuffix = ""; // DIR_SPLIT模式下的方向后缀

			if (dirMode != DIR_NORMAL) {
				String noExt = name;
				if (noExt.toLowerCase().endsWith(".png"))
					noExt = noExt.substring(0, noExt.length() - 4);
				if (!noExt.isEmpty()) {
					char last = noExt.charAt(noExt.length() - 1);
					if (last == '0' || last == '1') {
						if (dirMode == DIR_FILTER_RIGHT) {
							if (last == '0') continue; // 跳过左向帧
							nameForGroup = noExt.substring(0, noExt.length() - 1) + ".png";
						} else if (dirMode == DIR_SPLIT) {
							dirSuffix = "_" + last;
							nameForGroup = noExt.substring(0, noExt.length() - 1) + ".png";
						}
					}
				}
			}

			ImageIcon icon = new ImageIcon(f.getAbsolutePath());
			if (icon.getIconWidth() <= 0) continue;

			String animName = extractAnimName(nameForGroup) + dirSuffix;
			groups.computeIfAbsent(animName, k -> new ArrayList<>()).add(icon);
		}

		if (!groups.isEmpty()) {
			groups = mergeSmallGroups(groups);
			spriteGroups.put(category, groups);
		}
	}

	/**
	 * 从文件名提取动画组名
	 */
	private static String extractAnimName(String filename) {
		// 去掉扩展名
		String name = filename;
		if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
		String original = name;

		// enemy文件检测(在剥离数字之前): 长数字+字母+短横线+数字
		if (original.matches("^\\d{10,}[A-Za-z]+\\d*-\\d+$")) return "walk";
		// 另一种enemy命名: enemy_attackNNN
		if (original.toLowerCase().startsWith("enemy_")) {
			return original.replaceAll("\\d+$", "").toLowerCase();
		}

		// 移除末尾的 (数字) 如 "D_boss (1)" -> "D_boss"
		name = name.replaceAll("\\s*\\(\\d+\\)\\s*$", "");
		// 移除末尾分隔符+数字 如 "attack000"->"attack", "run01"->"run"
		name = name.replaceAll("[_\\-]?\\d+$", "");

		if (name.isEmpty()) return "default";

		// boss
		if (name.equalsIgnoreCase("D_boss")) return "idle";

		// hostage: oder -> idle
		if (name.equalsIgnoreCase("oder")) return "idle";

		// aircraft
		if (name.equalsIgnoreCase("plane_fly")) return "fly";
		if (name.equalsIgnoreCase("plane")) return "plane";

		return name.toLowerCase();
	}

	/**
	 * 合并过小的动画组(少于2帧合并到同名基础组)
	 */
	private static Map<String, List<ImageIcon>> mergeSmallGroups(Map<String, List<ImageIcon>> groups) {
		if (groups.size() <= 1) return groups;

		Map<String, List<ImageIcon>> result = new LinkedHashMap<>();
		List<ImageIcon> orphans = new ArrayList<>();

		for (Map.Entry<String, List<ImageIcon>> entry : groups.entrySet()) {
			if (entry.getValue().size() >= 1) {
				result.put(entry.getKey(), entry.getValue());
			} else {
				orphans.addAll(entry.getValue());
			}
		}

		if (result.isEmpty() && !orphans.isEmpty()) {
			result.put("default", orphans);
		} else if (!orphans.isEmpty()) {
			String firstKey = result.keySet().iterator().next();
			result.get(firstKey).addAll(orphans);
		}

		return result;
	}

	/**
	 * 自然排序比较（处理超长数字）
	 */
	private static int naturalCompare(String a, String b) {
		int ia = 0, ib = 0;
		while (ia < a.length() && ib < b.length()) {
			char ca = a.charAt(ia), cb = b.charAt(ib);
			if (Character.isDigit(ca) && Character.isDigit(cb)) {
				// 跳过前导零
				int startA = ia, startB = ib;
				while (ia < a.length() && a.charAt(ia) == '0') ia++;
				while (ib < b.length() && b.charAt(ib) == '0') ib++;
				int endA = ia, endB = ib;
				while (endA < a.length() && Character.isDigit(a.charAt(endA))) endA++;
				while (endB < b.length() && Character.isDigit(b.charAt(endB))) endB++;
				int lenA = endA - ia, lenB = endB - ib;
				if (lenA != lenB) return Integer.compare(lenA, lenB);
				// 长度相同，逐位比较
				for (int k = 0; k < lenA; k++) {
					char da = a.charAt(ia + k), db = b.charAt(ib + k);
					if (da != db) return Character.compare(da, db);
				}
				// 数字相同，比较前导零数量
				int zerosA = ia - startA, zerosB = ib - startB;
				if (zerosA != zerosB) return Integer.compare(zerosA, zerosB);
				ia = endA; ib = endB;
			} else {
				if (ca != cb) return Character.compare(ca, cb);
				ia++; ib++;
			}
		}
		return Integer.compare(a.length() - ia, b.length() - ib);
	}

	// ===== 精灵获取API =====

	/**
	 * 获取某个类别下所有帧(合并所有动画组)
	 */
	public static List<ImageIcon> getSprites(String category) {
		Map<String, List<ImageIcon>> groups = spriteGroups.get(category);
		if (groups == null) return null;
		List<ImageIcon> all = new ArrayList<>();
		for (List<ImageIcon> list : groups.values()) all.addAll(list);
		return all.isEmpty() ? null : all;
	}

	/**
	 * 获取指定动画组的精灵帧
	 * @param category 类别 (player/enemy/boss/...)
	 * @param animName 动画名 (run/attack/jump/idle/...)
	 */
	public static List<ImageIcon> getSprites(String category, String animName) {
		Map<String, List<ImageIcon>> groups = spriteGroups.get(category);
		if (groups == null) return null;

		// 精确匹配
		List<ImageIcon> result = groups.get(animName);
		if (result != null && !result.isEmpty()) return result;

		// 前缀匹配
		for (String key : groups.keySet()) {
			if (key.startsWith(animName) || animName.startsWith(key)) {
				return groups.get(key);
			}
		}

		// fallback: 返回第一个组
		if (!groups.isEmpty()) return groups.values().iterator().next();
		return null;
	}

	/**
	 * 加载玩家
	 */
	public static void loadPlay() {
		loadObj();
		String playStr = "80,400,player";
		ElementObj obj = getObj("player");
		if (obj != null) {
			ElementObj play = obj.createElement(playStr);
			em.addElement(play, GameElement.PLAY);
		}
	}

	// ===== 对象工厂(反射) =====

	private static Map<String, Class<?>> objMap = new HashMap<>();

	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			if (class1 == null) return null;
			Object newInstance = class1.getDeclaredConstructor().newInstance();
			if (newInstance instanceof ElementObj) {
				return (ElementObj) newInstance;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void loadObj() {
		String texturl = "com/tedu/text/obj.pro";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream texts = classLoader.getResourceAsStream(texturl);
		pro.clear();
		try {
			pro.load(texts);
			Set<Object> set = pro.keySet();
			for (Object o : set) {
				String classUrl = pro.getProperty(o.toString());
				Class<?> forName = Class.forName(classUrl);
				objMap.put(o.toString(), forName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
