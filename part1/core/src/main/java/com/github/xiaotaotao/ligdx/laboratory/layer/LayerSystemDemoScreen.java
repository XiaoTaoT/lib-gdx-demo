package com.github.xiaotaotao.ligdx.laboratory.layer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;

/**
 * 图层系统演示 Screen
 * 展示如何使用7层图层系统进行分层渲染
 */
public class LayerSystemDemoScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private LayerSystemImpl layerSystem;

    // 演示用的纹理
    private Texture backgroundTexture;
    private Texture characterTexture;
    private Texture effectTexture;

    // 动画时间
    private float time = 0f;
    private float backgroundOffset = 0f;
    private float characterX = 400f;
    private float characterY = 240f;
    private float effectScale = 1.0f;

    // 控制变量
    private boolean showLayerInfo = true;
    private int currentLayerIndex = 0;

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;

    @Override
    public void show() {
        // 初始化相机和视口
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        // 初始化渲染对象
        batch = new SpriteBatch();

        // 生成字体
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("ui/simsun.ttc"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 24;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "图层系统演示按数字键切换图层可见性按+/-调整图层透明度";
        font = generator.generateFont(parameter);
        generator.dispose();

        // 创建简单的纹理用于演示
        createDemoTextures();

        // 创建图层系统
        layerSystem = new LayerSystemImpl();

        // 初始化所有图层
        setupLayers();
    }

    /**
     * 创建演示用的纹理
     */
    private void createDemoTextures() {
        // 创建背景纹理（渐变蓝色）
        com.badlogic.gdx.graphics.Pixmap bgPixmap = new com.badlogic.gdx.graphics.Pixmap(800, 480, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        for (int y = 0; y < 480; y++) {
            float ratio = y / 480f;
            int color = com.badlogic.gdx.graphics.Color.rgba8888(0.2f, 0.4f + ratio * 0.3f, 0.8f, 1.0f);
            for (int x = 0; x < 800; x++) {
                bgPixmap.drawPixel(x, y, color);
            }
        }
        backgroundTexture = new Texture(bgPixmap);
        bgPixmap.dispose();

        // 创建角色纹理（简单方块）
        com.badlogic.gdx.graphics.Pixmap charPixmap = new com.badlogic.gdx.graphics.Pixmap(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        charPixmap.setColor(0.2f, 0.8f, 0.2f, 1.0f);
        charPixmap.fillRectangle(16, 16, 32, 32);
        characterTexture = new Texture(charPixmap);
        charPixmap.dispose();

        // 创建特效纹理（星星）
        com.badlogic.gdx.graphics.Pixmap effectPixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        effectPixmap.setColor(1.0f, 1.0f, 0.0f, 1.0f);
        effectPixmap.fillCircle(16, 16, 16);
        effectTexture = new Texture(effectPixmap);
        effectPixmap.dispose();
    }

    /**
     * 设置所有图层
     */
    private void setupLayers() {
        // 1. 背景层 - 渐变背景
        layerSystem.addRenderer(LayerSystem.Layer.BACKGROUND, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制滚动背景
                float offset = backgroundOffset % 800;
                batch.draw(backgroundTexture, -offset, 0, 800, 480);
                batch.draw(backgroundTexture, 800 - offset, 0, 800, 480);
            }

            @Override
            public void update(float delta) {
                // 背景滚动
                backgroundOffset += 20f * delta;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 2. 地图层 - 网格地面
        layerSystem.addRenderer(LayerSystem.Layer.MAP, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制网格
                batch.setColor(0.3f, 0.3f, 0.3f, 0.5f);
                for (int x = 0; x < 800; x += 50) {
                    batch.draw(backgroundTexture, x, 0, 1, 480);
                }
                for (int y = 0; y < 480; y += 50) {
                    batch.draw(backgroundTexture, 0, y, 800, 1);
                }
                batch.setColor(1, 1, 1, 1);
            }

            @Override
            public void update(float delta) {
                // 地图层不需要更新
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 3. 装饰层 - 地面装饰物
        layerSystem.addRenderer(LayerSystem.Layer.DECORATION, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制一些装饰物（小方块）
                batch.setColor(0.8f, 0.6f, 0.4f, 1.0f);
                for (int i = 0; i < 5; i++) {
                    float x = 100 + i * 150;
                    float y = 50 + MathUtils.sin(time + i) * 20;
                    batch.draw(characterTexture, x, y, 32, 32);
                }
                batch.setColor(1, 1, 1, 1);
            }

            @Override
            public void update(float delta) {
                // 装饰物动画
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 4. 角色层 - 可移动的角色
        layerSystem.addRenderer(LayerSystem.Layer.CHARACTER, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制角色
                batch.draw(characterTexture, characterX - 32, characterY - 32, 64, 64);
            }

            @Override
            public void update(float delta) {
                // 角色移动（WASD控制）
                float speed = 100f * delta;
                if (Gdx.input.isKeyPressed(Input.Keys.W)) characterY += speed;
                if (Gdx.input.isKeyPressed(Input.Keys.S)) characterY -= speed;
                if (Gdx.input.isKeyPressed(Input.Keys.A)) characterX -= speed;
                if (Gdx.input.isKeyPressed(Input.Keys.D)) characterX += speed;

                // 限制在屏幕内
                characterX = MathUtils.clamp(characterX, 32, VIRTUAL_WIDTH - 32);
                characterY = MathUtils.clamp(characterY, 32, VIRTUAL_HEIGHT - 32);
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 5. 特效层 - 闪烁特效
        layerSystem.addRenderer(LayerSystem.Layer.EFFECT, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制闪烁特效
                float alpha = (MathUtils.sin(time * 3f) + 1f) / 2f;
                batch.setColor(1, 1, 0, alpha * 0.8f);
                float scale = effectScale;
                for (int i = 0; i < 3; i++) {
                    float x = 200 + i * 200;
                    float y = 300;
                    float size = 32 * scale;
                    batch.draw(effectTexture, x - size / 2, y - size / 2, size, size);
                }
                batch.setColor(1, 1, 1, 1);
            }

            @Override
            public void update(float delta) {
                // 特效缩放动画
                effectScale = 1.0f + MathUtils.sin(time * 2f) * 0.3f;
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 6. UI层 - 游戏UI
        layerSystem.addRenderer(LayerSystem.Layer.UI, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 绘制UI背景（半透明）
                batch.setColor(0, 0, 0, 0.5f);
                batch.draw(backgroundTexture, 10, 10, 200, 100);
                batch.setColor(1, 1, 1, 1);

                // 绘制UI文字
                font.draw(batch, "图层系统演示", 20, 90);
                font.draw(batch, "WASD: 移动角色", 20, 60);
                font.draw(batch, "1-7: 切换图层", 20, 30);
            }

            @Override
            public void update(float delta) {
                // UI层不需要更新
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });

        // 7. 顶层UI - 图层信息显示
        layerSystem.addRenderer(LayerSystem.Layer.TOP_UI, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;

            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                if (showLayerInfo) {
                    // 绘制图层信息
                    float y = VIRTUAL_HEIGHT - 30;
                    LayerSystem.Layer[] layers = LayerSystem.Layer.values();
                    for (int i = 0; i < layers.length; i++) {
                        LayerSystem.Layer layer = layers[i];
                        boolean visible = layerSystem.isLayerVisible(layer);
                        float alpha = layerSystem.getLayerAlpha(layer);
                        String status = visible ? (alpha < 1.0f ? "半透明" : "显示") : "隐藏";
                        String text = (i + 1) + ". " + layer.name() + ": " + status;
                        if (i == currentLayerIndex) {
                            batch.setColor(1, 1, 0, 1); // 高亮当前图层
                        } else {
                            batch.setColor(1, 1, 1, 1);
                        }
                        font.draw(batch, text, VIRTUAL_WIDTH - 300, y);
                        y -= 25;
                    }
                    batch.setColor(1, 1, 1, 1);
                }
            }

            @Override
            public void update(float delta) {
                // 处理键盘输入
                handleLayerControls();
            }

            @Override
            public boolean isEnabled() {
                return enabled;
            }

            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });
    }

    /**
     * 处理图层控制输入
     */
    private void handleLayerControls() {
        LayerSystem.Layer[] layers = LayerSystem.Layer.values();

        // 数字键1-7切换图层可见性
        for (int i = 0; i < layers.length && i < 7; i++) {
            int key = Input.Keys.NUM_1 + i;
            if (Gdx.input.isKeyJustPressed(key)) {
                currentLayerIndex = i;
                boolean visible = layerSystem.isLayerVisible(layers[i]);
                layerSystem.setLayerVisible(layers[i], !visible);
            }
        }

        // +/- 调整当前图层透明度
        if (Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            float alpha = layerSystem.getLayerAlpha(layers[currentLayerIndex]);
            layerSystem.setLayerAlpha(layers[currentLayerIndex], Math.min(1.0f, alpha + 0.01f));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            float alpha = layerSystem.getLayerAlpha(layers[currentLayerIndex]);
            layerSystem.setLayerAlpha(layers[currentLayerIndex], Math.max(0.0f, alpha - 0.01f));
        }

        // H键切换信息显示
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showLayerInfo = !showLayerInfo;
        }
    }

    @Override
    public void render(float delta) {
        time += delta;

        // 更新所有图层
        layerSystem.updateAll(delta);

        // 清屏
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新相机
        camera.update();

        // 使用图层系统渲染所有图层
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        layerSystem.renderAll(batch, camera);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        viewport.update(width, height, true);
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
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (characterTexture != null) {
            characterTexture.dispose();
        }
        if (effectTexture != null) {
            effectTexture.dispose();
        }
        if (layerSystem != null) {
            layerSystem.dispose();
        }
    }
}

