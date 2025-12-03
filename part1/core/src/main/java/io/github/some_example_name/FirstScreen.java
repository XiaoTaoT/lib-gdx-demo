package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float time;

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;

    @Override
    public void show() {
        // 初始化摄像机和视口
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        // 初始化渲染相关对象
        batch = new SpriteBatch();
        // 使用 FreeType 从 TTF 生成支持中文的 BitmapFont（只生成一次，然后复用）
        //
        // 注意：请将一个包含中文的 TTF 字体文件（例如思源黑体、微软雅黑等）
        // 放到 assets/ui 目录下，并命名为 "msyh.ttf" 或相应名称，
        // 同时把下面的路径改成实际文件名。
        FreeTypeFontGenerator generator =
                new FreeTypeFontGenerator(Gdx.files.internal("ui/simsun.ttc"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 28; // 字号
        // 只生成会用到的字符，可以按需扩展，避免一次生成整个中文字符集导致体积过大
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "背景颜色會隨时间變化Hello LibGDX!Time:. s";
        font = generator.generateFont(parameter);
        generator.dispose(); // 生成后就可以释放生成器

        time = 0f;
    }

    @Override
    public void render(float delta) {
        // 让时间累积，用来做简单动画
        time += delta;

        // 背景颜色在 0~1 之间循环：R 和 B 分量随时间变化
        float r = (float) ((Math.sin(time) + 1) / 2);      // 0~1 之间
        float g = 0.1f;
        float b = (float) ((Math.cos(time) + 1) / 2);      // 0~1 之间

        Gdx.gl.glClearColor(r, g, b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新摄像机
        camera.update();

        // 绘制文本
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, "Hello LibGDX!", 50, VIRTUAL_HEIGHT - 50);
        font.draw(batch, "Time: " + String.format("%.2f s", time), 50, VIRTUAL_HEIGHT - 90);
        font.draw(batch, "背景颜色會隨時間變化", 50, VIRTUAL_HEIGHT - 130);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) return;

        // 更新视口尺寸
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        if (batch != null) {
            batch.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}