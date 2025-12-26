package com.github.xiaotaotao.ligdx.laboratory;

import com.badlogic.gdx.Game;
import com.github.xiaotaotao.ligdx.laboratory.attack.AttackDemoScreen;
import com.github.xiaotaotao.ligdx.laboratory.shape.ShapeDemoScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        // 默认显示 FirstScreen（游戏主界面）
        // 要切换到图层系统演示，请将下面的 FirstScreen() 改为 LayerSystemDemoScreen()
        // setScreen(new FirstScreen());

        // 取消注释下面这行来运行图层系统演示：
        // setScreen(new LayerSystemDemoScreen());

        // UI 演示 Screen
        // setScreen(new UIDemoScreen());

        // 像素角色/战斗系统演示 Screen
//        setScreen(new CharacterDemoScreen());
//        setScreen(new AttackDemoScreen());
//        setScreen(new ShapeDemoScreen());
        
        // 虚拟线程演示 - 1000个对象无规则运动
        // setScreen(new VirtualThreadDemoScreen());
        
        // setScreen(new P2Screen());
        setScreen(new VirtualThreadDemoScreen());
    }
}
