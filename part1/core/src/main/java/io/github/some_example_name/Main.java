package io.github.some_example_name;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        // 默认显示 FirstScreen（游戏主界面）
        // 要切换到图层系统演示，请将下面的 FirstScreen() 改为 LayerSystemDemoScreen()
        // setScreen(new FirstScreen());

        // 取消注释下面这行来运行图层系统演示：
//        setScreen(new LayerSystemDemoScreen());
        setScreen(new P1Screen());
    }
}
