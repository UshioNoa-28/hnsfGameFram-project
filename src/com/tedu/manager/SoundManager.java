package com.tedu.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * 音效管理器 — 背景音乐循环 + 音效播放
 */
public class SoundManager {
	private static SoundManager instance;
	private List<File> bgmFiles = new ArrayList<>();
	private List<File> sfxFiles = new ArrayList<>();
	private Clip bgmClip;
	private Sequencer midiSequencer;
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
		if (muted) return;
		// Standard Java Sound has no guaranteed MP3 decoder. Prefer WAV, otherwise use the built-in arcade score.
		boolean hasWav = false;
		for (File f : bgmFiles) if (f.getName().toLowerCase().endsWith(".wav")) hasWav = true;
		if (hasWav) playNextBGM(); else startArcadeBGM();
	}

	private void playNextBGM() {
		if (bgmFiles.isEmpty() || muted) return;
		try {
			if (bgmClip != null) {
				bgmClip.stop();
				bgmClip.close();
			}
			bgmIndex = rand.nextInt(bgmFiles.size());
			List<File> playable = new ArrayList<>();
			for (File f : bgmFiles) if (f.getName().toLowerCase().endsWith(".wav")) playable.add(f);
			if (playable.isEmpty()) { startArcadeBGM(); return; }
			File bgmFile = playable.get(rand.nextInt(playable.size()));
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
		if (muted) return;
		try {
			File sfxFile = null;
			for (File f : sfxFiles) {
				if (f.getName().toLowerCase().endsWith(".wav") && f.getName().toLowerCase().contains(name.toLowerCase())) {
					sfxFile = f;
					break;
				}
			}
			if (sfxFile == null) { synthesizeSFX(name); return; }
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
		if (midiSequencer != null) { midiSequencer.stop(); midiSequencer.close(); midiSequencer = null; }
	}

	private void startArcadeBGM() {
		try {
			if (midiSequencer != null && midiSequencer.isRunning()) return;
			Sequence seq = new Sequence(Sequence.PPQ, 24);
			Track track = seq.createTrack();
			addMidi(track, ShortMessage.PROGRAM_CHANGE, 0, 81, 0, 0);
			int[] riff = {45,45,48,52,45,45,53,52,43,43,47,50,43,43,52,50};
			for (int bar = 0; bar < 8; bar++) for (int i = 0; i < riff.length; i++) {
				long tick = (bar * riff.length + i) * 6L;
				addMidi(track, ShortMessage.NOTE_ON, 0, riff[i], 72, tick);
				addMidi(track, ShortMessage.NOTE_OFF, 0, riff[i], 0, tick + 5);
				if (i % 4 == 0) { addMidi(track, ShortMessage.NOTE_ON, 9, 36, 90, tick); addMidi(track, ShortMessage.NOTE_OFF, 9, 36, 0, tick + 2); }
				if (i % 4 == 2) { addMidi(track, ShortMessage.NOTE_ON, 9, 38, 75, tick); addMidi(track, ShortMessage.NOTE_OFF, 9, 38, 0, tick + 2); }
			}
			midiSequencer = MidiSystem.getSequencer(); midiSequencer.open(); midiSequencer.setSequence(seq);
			midiSequencer.setTempoInBPM(148); midiSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY); midiSequencer.start();
			bgmPlaying = true;
		} catch (Exception e) { System.out.println("[SoundManager] Arcade BGM unavailable: " + e.getMessage()); }
	}

	private void addMidi(Track track, int command, int channel, int note, int velocity, long tick) throws Exception {
		ShortMessage message = new ShortMessage(); message.setMessage(command, channel, note, velocity);
		track.add(new MidiEvent(message, tick));
	}

	private void synthesizeSFX(String name) {
		new Thread(() -> {
			try {
				int rate = 11025;
				double seconds = name.contains("bomb") || name.contains("die") ? .28 : name.contains("boss") ? .18 : .055;
				byte[] data = new byte[(int)(rate * seconds)];
				for (int i = 0; i < data.length; i++) {
					double fade = 1.0 - i / (double)data.length;
					double hz = name.contains("fire") ? 180 - i * .06 : name.contains("boss") ? 85 : 55;
					double tone = Math.sin(2 * Math.PI * hz * i / rate);
					double noise = rand.nextDouble() * 2 - 1;
					data[i] = (byte)(Math.max(-1, Math.min(1, tone * .35 + noise * .65)) * 90 * fade);
				}
				AudioFormat format = new AudioFormat(rate, 8, 1, true, false);
				SourceDataLine line = AudioSystem.getSourceDataLine(format); line.open(format); line.start();
				line.write(data, 0, data.length); line.drain(); line.close();
			} catch (Exception ignored) { }
		}, "arcade-sfx").start();
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
