package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float time;

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;
    // 地图信息：40x40 个 32x32 的 tile（来自 desert.tmx 描述）
    private static final int MAP_WIDTH_TILES = 40;
    private static final int MAP_HEIGHT_TILES = 40;
    private static final int TILE_SIZE = 32;

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

        // 加载 Tiled 地图（assets/tmx/desert.tmx）
        // 注意：lwjgl3 的 run 任务 workingDir 已指向 assets 目录，
        // 这里使用内部文件路径 "tmx/desert.tmx"
        tiledMap = new TmxMapLoader().load("tmx/desert.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        time = 0f;
    }

    @Override
    public void render(float delta) {
        // 让时间累积，用来做简单动画
        time += delta;

        // 键盘输入控制相机移动（WASD + 方向键）
        float moveSpeed = 200f * delta; // 每秒 200 像素
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.position.y += moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.position.y -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.position.x -= moveSpeed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.position.x += moveSpeed;
        }

        // 限制相机范围在地图内部（避免移出地图外面全是空）
        float mapWidth = MAP_WIDTH_TILES * TILE_SIZE;
        float mapHeight = MAP_HEIGHT_TILES * TILE_SIZE;
        float halfViewWidth = VIRTUAL_WIDTH / 2f;
        float halfViewHeight = VIRTUAL_HEIGHT / 2f;
        camera.position.x = MathUtils.clamp(camera.position.x, halfViewWidth, mapWidth - halfViewWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfViewHeight, mapHeight - halfViewHeight);

        // 清屏为黑色，地图会覆盖背景
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新摄像机
        camera.update();

        // 先绘制 Tiled 地图
        if (mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        // 再绘制文本（叠加在地图之上）
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
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
    }
}