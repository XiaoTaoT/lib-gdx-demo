package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * @Desciption:
 * @ClassName:P1Screen
 * @Author:TwT
 * @Date:2025/12/6 19:59
 * @Version:1.0
 **/
public class P1Screen implements Screen {
    // 核心组件
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture playerTexture;       // 加载4x4精灵图
    private Animation<TextureRegion> upAnim;    // 向上走动画
    private Animation<TextureRegion> downAnim;  // 向下走动画
    private Animation<TextureRegion> leftAnim;  // 向左走动画
    private Animation<TextureRegion> rightAnim; // 向右走动画

    // 分辨率适配
    private static final float VIRTUAL_WIDTH = 1920;  // 虚拟宽度
    private static final float VIRTUAL_HEIGHT = 1080; // 虚拟高度

    // 状态变量
    private float stateTime;             // 动画播放累计时间（用于计算当前帧）
    private Animation<TextureRegion> currentAnim; // 当前播放的动画
    private float playerX = 200;         // 人物世界X坐标
    private float playerY = 200;         // 人物世界Y坐标
    private final float MOVE_SPEED = 150; // 移动速度（像素/秒）
    private int tileSize;

    @Override
    public void show() {
        // 1. 初始化相机和视口（用于分辨率适配）
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        // 2. 初始化渲染批次
        batch = new SpriteBatch();

        // 3. 加载精灵图并拆分瓦片（假设p.png是4x4等宽高瓦片，如256x256 → 单瓦片64x64）
        FileHandle p41 = Gdx.files.internal("person/p_4_1.png");

        playerTexture = new Texture(p41);
        // 设置纹理过滤模式，避免黑线和锯齿
        playerTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        int frame4Width = playerTexture.getWidth() / 4;
        int frame4Height = playerTexture.getHeight() / 4;
        tileSize = frame4Width; // 初始化瓦片大小（假设是正方形瓦片）
        TextureRegion[][] tiles = TextureRegion.split(playerTexture, frame4Width, frame4Height);

        // 按方向分组帧
        TextureRegion[] downFrames = tiles[0];    // 第0行=向上走的4帧
        TextureRegion[] leftFrames = tiles[1];  // 第1行=向下走的4帧
        TextureRegion[] rightFrames = tiles[2];  // 第2行=向左走的4帧
        TextureRegion[] upFrames = tiles[3]; // 第3行=向右走的4帧


        // 4. 创建Animation对象（参数1：帧间隔时间，参数2：帧序列；0.1f=每100ms切换一帧）
        float frameDuration = 0.1f;
        upAnim = new Animation<>(frameDuration, upFrames);
        downAnim = new Animation<>(frameDuration, downFrames);
        leftAnim = new Animation<>(frameDuration, leftFrames);
        rightAnim = new Animation<>(frameDuration, rightFrames);

        // 5. 默认动画：向下走
        currentAnim = downAnim;
    }

    @Override
    public void render(float delta) {


        // 1. 清屏（黑色背景）
        ScreenUtils.clear(0, 0, 0, 1);

        // 2. 处理输入（方向键控制移动+切换动画）
        handleInput();

        // 3. 更新动画时间（Gdx.graphics.getDeltaTime()=上一帧到当前帧的时间差）
        stateTime += Gdx.graphics.getDeltaTime();

        // 4. 获取当前动画帧
        TextureRegion currentFrame = currentAnim.getKeyFrame(stateTime, true); // true=循环播放

        // 5. 渲染当前帧
        batch.setProjectionMatrix(camera.combined); // 使用相机的投影矩阵
        batch.begin(); // 开始渲染批次
        // 绘制：世界坐标(x,y) + 帧宽高（tileSize）
        batch.draw(currentFrame, playerX, playerY, tileSize, tileSize);
        batch.end(); // 结束渲染批次
    }

    // 处理输入：方向键控制移动和动画切换
    private void handleInput() {
        // 重置移动标记（避免多方向同时移动）
        boolean isMoving = false;
        float deltaTime = Gdx.graphics.getDeltaTime();

        // 向上
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerY += MOVE_SPEED * deltaTime;
            currentAnim = upAnim;
            isMoving = true;
        }
        // 向下
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            playerY -= MOVE_SPEED * deltaTime;
            currentAnim = downAnim;
            isMoving = true;
        }
        // 向左
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerX -= MOVE_SPEED * deltaTime;
            currentAnim = leftAnim;
            isMoving = true;
        }
        // 向右
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerX += MOVE_SPEED * deltaTime;
            currentAnim = rightAnim;
            isMoving = true;
        }

        // 边界检查，防止角色移出视口范围
        playerX = Math.max(0, Math.min(playerX, VIRTUAL_WIDTH - tileSize));
        playerY = Math.max(0, Math.min(playerY, VIRTUAL_HEIGHT - tileSize));

        // 若未移动，重置动画时间（停止时固定显示第一帧）
        if (!isMoving) {
            stateTime = 0;
        }
    }

    @Override
    public void resize(int width, int height) {
        // 窗口大小改变时，更新视口以保持宽高比
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
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
        batch.dispose();
        playerTexture.dispose(); // 仅释放Texture，Region/Animation无需单独释放
    }
}
