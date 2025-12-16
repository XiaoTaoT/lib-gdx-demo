package com.github.xiaotaotao.ligdx.laboratory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * 一个简单的 UI 演示 Screen，展示 Scene2D UI 基本控件用法：
 * - Label / TextButton / CheckBox / TextField / Slider / ProgressBar / SelectBox
 * - Table 布局
 * - 使用默认 uiskin 皮肤
 */
public class UIDemoScreen implements Screen {

    private Stage stage;
    private Skin skin;

    // 状态标签，动态显示当前 UI 交互结果
    private Label statusLabel;
    private ProgressBar progressBar;

    @Override
    public void show() {
        // 使用 ScreenViewport 使 UI 在窗口缩放时自动适配
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // 加载默认皮肤（项目 assets/ui/uiskin.json）
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        buildUI();
    }

    /**
     * 构建 UI 布局与控件
     */
    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(20f);
        root.defaults().space(10f);
        stage.addActor(root);
        // 半透明背景，增强对比度（使用 uiskin 内置的 "white" drawable 着色）
        root.setBackground(skin.newDrawable("white", 0f, 0f, 0f, 0.35f));

        // 使用默认样式，避免皮肤中未注册的 "title" 样式导致崩溃
        Label title = new Label("Scene2D UI 演示", skin);
        root.add(title).colspan(2).center();
        root.row();

        // 文本按钮
        TextButton clickMe = new TextButton("点我", skin);
        clickMe.getLabel().setColor(Color.WHITE); // 按钮文字设为白色
        clickMe.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                statusLabel.setText("状态：点击了按钮");
            }
        });
        root.add(clickMe).width(180).height(50);

        // 复选框
        CheckBox musicToggle = new CheckBox(" 开启音乐", skin);
        musicToggle.setChecked(true);
        musicToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                statusLabel.setText("状态：音乐 " + (musicToggle.isChecked() ? "已开启" : "已关闭"));
            }
        });
        root.add(musicToggle).left();
        root.row();

        // 文本输入框
        TextField nameField = new TextField("", skin);
        nameField.setMessageText("输入玩家昵称");
        root.add(nameField).width(180).height(40);

        // 下拉框
        SelectBox<String> qualityBox = new SelectBox<>(skin);
        qualityBox.setItems("低", "中", "高", "超高");
        qualityBox.setSelected("高");
        qualityBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                statusLabel.setText("状态：画质 " + qualityBox.getSelected());
            }
        });
        root.add(qualityBox).width(180);
        root.row();

        // 滑块与进度条
        Label volumeLabel = new Label("音量", skin);
        Slider volumeSlider = new Slider(0f, 100f, 1f, false, skin);
        volumeSlider.setValue(60f);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float value = volumeSlider.getValue();
                progressBar.setValue(value);
                statusLabel.setText("状态：音量 " + (int) value + "%");
            }
        });
        root.add(volumeLabel).left();
        root.add(volumeSlider).width(220);
        root.row();

        progressBar = new ProgressBar(0f, 100f, 1f, false, skin);
        progressBar.setValue(60f);
        root.add(new Label("进度/加载示例", skin)).left();
        root.add(progressBar).width(220);
        root.row();

        // 状态显示
        statusLabel = new Label("状态：等待交互", skin);
        statusLabel.setColor(Color.YELLOW);
        // 为状态区域添加背景，提高可读性
        Table statusBox = new Table();
        statusBox.setBackground(skin.newDrawable("white", 0f, 0f, 0f, 0.55f));
        statusBox.add(statusLabel).left().pad(8f);
        root.add(statusBox).colspan(2).fillX();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}

