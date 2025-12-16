package com.github.xiaotaotao.ligdx.laboratory.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * RPG 风格背包 UI 演示：
 * - 左侧网格展示物品
 * - 右侧详情区域显示名称/稀有度/数量/描述
 * - 底部提供“使用”“丢弃”示例按钮
 */
public class InventoryScreen implements Screen {

    private Stage stage;
    private Skin skin;

    private final Array<Item> items = new Array<>();
    private Label nameLabel;
    private Label rarityLabel;
    private Label countLabel;
    private Label descLabel;

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        seedItems();
        buildUI();
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        root.pad(16f);
        root.defaults().space(10f);
        stage.addActor(root);

        // 顶部标题
        Label title = new Label("背包 / Inventory", skin);
        title.setFontScale(1.1f);
        root.add(title).left().colspan(2);
        TextButton close = new TextButton("关闭", skin);
        root.add(close).right();
        root.row();

        // 左侧物品网格（滚动）
        Table grid = new Table();
        grid.defaults().space(6f).size(64);
        int cols = 5;
        for (int i = 0; i < items.size; i++) {
            Item it = items.get(i);
            Image icon = new Image(skin.newDrawable("white", it.color));
            icon.setSize(64, 64);
            icon.setColor(it.color);
            Table slot = new Table(skin);
            slot.setBackground(skin.newDrawable("white", 0, 0, 0, 0.35f));
            slot.add(icon).grow();
            slot.row();
            Label qty = new Label("x" + it.count, skin);
            qty.setAlignment(Align.center);
            qty.setColor(Color.WHITE);
            slot.add(qty).growX();

            final int idx = i;
            slot.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showItemDetail(items.get(idx));
                }
            });

            grid.add(slot).size(72, 90);
            if ((i + 1) % cols == 0) {
                grid.row();
            }
        }
        ScrollPane gridScroll = new ScrollPane(grid, skin);
        gridScroll.setFadeScrollBars(false);
        root.add(gridScroll).grow().colspan(2).height(360);

        // 右侧详情
        Table detail = new Table(skin);
        detail.defaults().space(8f).left();
        detail.setBackground(skin.newDrawable("white", 0, 0, 0, 0.45f));
        nameLabel = new Label("名称：-", skin);
        rarityLabel = new Label("稀有度：-", skin);
        countLabel = new Label("数量：-", skin);
        descLabel = new Label("描述：-", skin);
        descLabel.setWrap(true);
        detail.add(nameLabel).left().row();
        detail.add(rarityLabel).left().row();
        detail.add(countLabel).left().row();
        detail.add(descLabel).width(280).left();

        root.add(detail).width(320).top().padLeft(10f);
        root.row();

        // 底部操作按钮
        TextButton useBtn = new TextButton("使用", skin);
        TextButton dropBtn = new TextButton("丢弃", skin);
        Table actions = new Table();
        actions.defaults().space(10f).height(40).width(120);
        actions.add(useBtn);
        actions.add(dropBtn);
        root.add(actions).left().colspan(3).padTop(10f);

        // 交互提示
        Label hint = new Label("提示：点击左侧物品查看详情。", skin);
        root.row();
        root.add(hint).left().colspan(3);

        // 初始化显示第一个物品
        if (items.size > 0) {
            showItemDetail(items.first());
        }

        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    private void showItemDetail(Item item) {
        nameLabel.setText("名称：" + item.name);
        rarityLabel.setText("稀有度：" + item.rarity);
        rarityLabel.setColor(item.color);
        countLabel.setText("数量：x" + item.count);
        descLabel.setText("描述：" + item.desc);
    }

    private void seedItems() {
        items.add(new Item("初级药水", "普通", 12, "恢复少量生命。", Color.LIGHT_GRAY));
        items.add(new Item("中级药水", "精良", 6, "恢复中量生命。", Color.SKY));
        items.add(new Item("高级药水", "稀有", 3, "恢复大量生命。", Color.ROYAL));
        items.add(new Item("力量卷轴", "精良", 2, "短时间提升攻击力。", Color.SKY));
        items.add(new Item("智慧卷轴", "稀有", 1, "短时间提升法术强度。", Color.ROYAL));
        items.add(new Item("史诗宝石", "史诗", 1, "可用于打造强力装备。", Color.PURPLE));
        items.add(new Item("传说之刃", "传说", 1, "英雄的象征，锋利无比。", Color.GOLD));
        items.add(new Item("木材", "普通", 24, "基础材料。", Color.LIGHT_GRAY));
        items.add(new Item("铁矿", "普通", 18, "冶炼金属的原料。", Color.LIGHT_GRAY));
        items.add(new Item("魔力晶体", "稀有", 4, "蕴含元素之力的结晶。", Color.ROYAL));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.07f, 0.07f, 0.09f, 1f);
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

    private static class Item {
        final String name;
        final String rarity;
        final int count;
        final String desc;
        final Color color;

        Item(String name, String rarity, int count, String desc, Color color) {
            this.name = name;
            this.rarity = rarity;
            this.count = count;
            this.desc = desc;
            this.color = color;
        }
    }
}

