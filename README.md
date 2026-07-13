# 合金弹头 · JavaSE 实训项目

基于课程游戏框架开发的横版动作射击游戏，灵感来自经典《合金弹头》。

## 运行方式

```powershell
cd hnsfGameFram
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp bin com.tedu.game.GameStart
```

## 操作

| 按键 | 功能 |
|------|------|
| `Enter` | 开始 / 重来 |
| `A/D` 或 `←/→` | 移动 |
| `W` 或 `↑` | 跳跃 |
| `S` 或 `↓` | 蹲下 |
| `J` 或 `Space` | 射击 |

## 提交历史

- `init` — 课程框架基础
- `组长` — 框架核心 + 中文UI + 增强输入
- `组员A` — 场景系统（地形/障碍物/道具/爆炸/三关地图）
- `组员B` — 角色系统（玩家/子弹/3种敌人/Boss）

## 技术栈

JavaSE + Swing/AWT，无外部依赖。
