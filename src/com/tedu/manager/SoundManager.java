package com.tedu.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * 音效管理器 — 背景音乐循环 + 音效播放
 */
public class SoundManager {
	private static SoundManager instance;
	private List<File> bgmFiles = new ArrayList<>();
	private List<File> sfxFiles = new ArrayList<>();
	private Clip bgmClip;
	private int bgmIndex = 0;
	private boolean bgmPlaying = false;
	private boolean muted = false;
	private static final Random rand = new Random();

	private SoundManager() {
		loadSounds();
	}

	public static SoundManager getInstance() {
		if (instance == null) {
			instance = new SoundManager();
		}
		return instance;
	}

	private void loadSounds() {
		// 尝试多个可能的音效目录
		String[] paths = {
			"../合金弹头/合金弹头/music",
			"合金弹头/合金弹头/music",
			"image/sound"
		};
		File soundDir = null;
		for (String p : paths) {
			File f = new File(p);
			if (f.exists() && f.isDirectory()) { soundDir = f; break; }
		}
		// 也尝试从user.dir搜索
		if (soundDir == null) {
			String ud = System.getProperty("user.dir");
			for (String p : paths) {
				File f = new File(ud, p);
				if (f.exists() && f.isDirectory()) { soundDir = f; break; }
			}
		}

		if (soundDir != null) {
			File[] files = soundDir.listFiles();
			if (files != null) {
				for (File f : files) {
					String name = f.getName().toLowerCase();
					if (name.endsWith(".mp3") || name.endsWith(".wav")) {
						if (name.startsWith("music") || name.startsWith("bgm")) {
							bgmFiles.add(f);
						} else {
							sfxFiles.add(f);
						}
					}
				}
			}
		}
		Collections.sort(bgmFiles, (a, b) -> a.getName().compareTo(b.getName()));
		System.out.println("[SoundManager] BGM: " + bgmFiles.size() + " tracks, SFX: " + sfxFiles.size() + " files");
	}

	/**
	 * 开始播放背景音乐（随机顺序循环）
	 */
	public void startBGM() {
		if (bgmFiles.isEmpty() || muted) return;
		playNextBGM();
	}

	private void playNextBGM() {
		if (bgmFiles.isEmpty() || muted) return;
		try {
			if (bgmClip != null) {
				bgmClip.stop();
				bgmClip.close();
			}
			bgmIndex = rand.nextInt(bgmFiles.size());
			File bgmFile = bgmFiles.get(bgmIndex);
			AudioInputStream ais = AudioSystem.getAudioInputStream(bgmFile);
			bgmClip = AudioSystem.getClip();
			bgmClip.open(ais);
			bgmClip.addLineListener(event -> {
				if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
					// 播放下一首
					new Thread(() -> {
						try { Thread.sleep(1000); } catch (Exception e) {}
						playNextBGM();
					}).start();
				}
			});
			bgmClip.start();
			bgmPlaying = true;
		} catch (Exception e) {
			System.out.println("[SoundManager] BGM播放失败: " + e.getMessage());
		}
	}

	/**
	 * 播放音效（fire, die, explosion等）
	 */
	public void playSFX(String name) {
		if (muted || sfxFiles.isEmpty()) return;
		try {
			File sfxFile = null;
			for (File f : sfxFiles) {
				if (f.getName().toLowerCase().contains(name.toLowerCase())) {
					sfxFile = f;
					break;
				}
			}
			if (sfxFile == null) {
				// 随机播放一个
				sfxFile = sfxFiles.get(rand.nextInt(sfxFiles.size()));
			}
			AudioInputStream ais = AudioSystem.getAudioInputStream(sfxFile);
			Clip clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			clip.addLineListener(event -> {
				if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
					clip.close();
				}
			});
		} catch (Exception e) {
			// 静默失败
		}
	}

	public void stopBGM() {
		if (bgmClip != null) {
			bgmClip.stop();
			bgmClip.close();
			bgmPlaying = false;
		}
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
		if (muted) {
			stopBGM();
		} else {
			startBGM();
		}
	}

	public boolean isMuted() {
		return muted;
	}
}
