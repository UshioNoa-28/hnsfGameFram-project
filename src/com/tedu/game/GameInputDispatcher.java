package com.tedu.game;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tedu.controller.GameThread;
import com.tedu.element.ElementObj;
import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;

/**
 * 项目层的键盘输入分发器。
 *
 * 不依赖某个 Swing 组件拥有焦点，因此游戏面板重绘或窗口焦点变化后不会
 * 丢失移动按键；窗口失焦时由入口类调用 clearPressedKeys() 复位状态。
 */
public class GameInputDispatcher implements KeyEventDispatcher {
    private final ElementManager elementManager = ElementManager.getManager();
    private final Set<Integer> pressedKeys = new HashSet<Integer>();

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getID() == KeyEvent.KEY_PRESSED) {
            onKeyPressed(event.getKeyCode());
        } else if (event.getID() == KeyEvent.KEY_RELEASED) {
            onKeyReleased(event.getKeyCode());
        }
        return false;
    }

    private void onKeyPressed(int key) {
        if (key == KeyEvent.VK_ENTER) {
            if (GameThread.gameState == GameThread.STATE_MENU) {
                clearPressedKeys();
                GameThread.gameState = GameThread.STATE_PLAYING;
                return;
            }
            if (GameThread.gameState == GameThread.STATE_GAMEOVER
                    || GameThread.gameState == GameThread.STATE_WIN) {
                clearPressedKeys();
                elementManager.clearAll();
                GameThread.resetGame();
                return;
            }
        }

        if (GameThread.gameState != GameThread.STATE_PLAYING || !pressedKeys.add(key)) {
            return;
        }
        notifyPlayers(true, key);
    }

    private void onKeyReleased(int key) {
        if (!pressedKeys.remove(key)) {
            return;
        }
        notifyPlayers(false, key);
    }

    public void clearPressedKeys() {
        for (Integer key : pressedKeys) {
            notifyPlayers(false, key);
        }
        pressedKeys.clear();
    }

    private void notifyPlayers(boolean pressed, int key) {
        List<ElementObj> players = elementManager.getElementsByKey(GameElement.PLAY);
        for (ElementObj player : players) {
            player.keyClick(pressed, key);
        }
    }
}
