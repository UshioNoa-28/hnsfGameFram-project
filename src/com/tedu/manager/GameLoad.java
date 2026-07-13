package com.tedu.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tedu.element.ElementObj;

/**
 * @说明 加载器：读取配置文件，加载图片、地图、对象
 * @author renjj
 */
public class GameLoad {
	private static ElementManager em = ElementManager.getManager();

	public static Map<String, ImageIcon> imgMap = new HashMap<>();

	private static Properties pro = new Properties();

	/**
	 * 加载关卡地图
	 * @param levelId 关卡编号
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

				// 特殊处理 END 标记 (关卡终点)
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
					// key作为类别标识, 传给对应工厂
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

	/**
	 * 根据配置类型名创建元素
	 */
	private static ElementObj createElementByType(String type, String data) {
		String fullStr = type + "," + data;
		// 通过obj.pro中注册的类名来反射创建
		ElementObj proto = getObj(type.toLowerCase());
		if (proto != null) {
			return proto.createElement(fullStr);
		}
		return null;
	}

	/**
	 * 将配置类型映射到GameElement枚举
	 */
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
			case "END":
				return GameElement.MAPS;
			default:
				return GameElement.MAPS;
		}
	}

	/**
	 * 加载图片资源
	 */
	public static void loadImg() {
		String texturl = "com/tedu/text/GameData.pro";
		ClassLoader classLoader = GameLoad.class.getClassLoader();
		InputStream texts = classLoader.getResourceAsStream(texturl);
		pro.clear();
		try {
			pro.load(texts);
			Set<Object> set = pro.keySet();
			for (Object o : set) {
				String url = pro.getProperty(o.toString());
				ImageIcon icon = new ImageIcon(url);
				if (icon.getIconWidth() > 0) {
					imgMap.put(o.toString(), icon);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载玩家
	 */
	public static void loadPlay() {
		loadObj();
		// 玩家初始位置在关卡左边
		String playStr = "80,400,player"; // x,y,type
		ElementObj obj = getObj("player");
		if (obj != null) {
			ElementObj play = obj.createElement(playStr);
			em.addElement(play, GameElement.PLAY);
		}
	}

	/**
	 * 通过obj.pro中注册的key获取对象实例(反射)
	 */
	private static Map<String, Class<?>> objMap = new HashMap<>();

	public static ElementObj getObj(String str) {
		try {
			Class<?> class1 = objMap.get(str);
			if (class1 == null) return null;
			Object newInstance = class1.newInstance();
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
