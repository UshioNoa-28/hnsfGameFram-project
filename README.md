# 合金弹头 JavaSE 实训项目

一个基于 Java Swing 和课程游戏框架完成的横版射击游戏。项目包含三关地图、滚动镜头、平台跳跃、多种敌人、可破坏障碍、武器与补给道具，以及第三关 Boss 战。

## 启动方式

在 PowerShell 中进入项目目录：

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java | ForEach-Object FullName)
java -cp bin com.tedu.game.GameStart
```

## 操作说明

- `Enter`：开始游戏或返回主菜单
- `A / D` 或 `← / →`：左右移动
- `W` 或 `↑`：跳跃
- `S` 或 `↓`：蹲下，可躲避高位子弹
- `J` 或 `Space`：射击，支持按住连射

## 游戏目标

玩家需要消灭敌人、收集道具并到达每关最右侧。生命用尽则游戏失败。第三关需要击败装甲 Boss 后才能进入终点并完成游戏。

## 玩法特色

- 三种普通敌人：巡逻士兵、冲锋射手、狙击手
- 四种武器：普通枪、重机枪、散弹枪、火焰枪
- 五种道具：三种武器、生命恢复、额外生命
- 平台跳跃、蹲伏闪避和不同高度的射击判定
- 敌人和玩家都会受到重力影响，离开平台后正常下落
- 分数、生命、血量和武器状态会在关卡之间保留
- Boss 拥有两个攻击阶段，高位弹需要蹲伏、低位弹需要跳跃

## 代码结构

- `com.tedu.game`：项目入口和稳定的键盘输入分发
- `com.tedu.element`：玩家、敌人、Boss、子弹、道具和地形
- `com.tedu.text`：对象注册与三关地图配置
- `com.tedu.controller / manager / show`：课程框架提供的控制、管理和显示层
