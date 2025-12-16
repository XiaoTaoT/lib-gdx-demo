package com.github.xiaotaotao.ligdx.laboratory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import java.util.ArrayList;
import java.util.Iterator;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private SpriteBatch batch;
    private BitmapFont font;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float time;

    // 角色相关
    private enum CharacterType {
        FOUR_DIRECTION,  // 4向行走
        EIGHT_DIRECTION   // 8向行走
    }

    private CharacterType currentCharacterType = CharacterType.FOUR_DIRECTION;
    private Texture person4Texture;  // 4向行走精灵图
    private Texture person8Texture;  // 8向行走精灵图
    private TextureRegion[][] person4Frames; // 4行 x 3列的动画帧
    private TextureRegion[][] person8Frames; // 8行 x 8列的动画帧
    private int frameWidth, frameHeight; // 每帧的宽高
    private float playerX, playerY; // 角色在世界坐标中的位置
    private int currentDirection; // 当前方向
    private float animationTime; // 动画时间累积
    private float playerSpeed = 100f; // 角色移动速度（像素/秒）
    private static final float ANIMATION_FRAME_DURATION = 0.2f; // 每帧动画持续时间（秒）

    // 用于切换人物的按键状态（避免连续触发）
    private boolean switchKeyPressed = false;

    // Box2D 物理世界相关
    private World box2DWorld;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera box2DCamera;
    private Body playerBody; // 玩家物理身体
    private Body targetBody; // 目标物理身体
    private ArrayList<Body> bullets; // 子弹列表
    private float targetX, targetY; // 目标位置（像素坐标）
    private static final float PIXELS_PER_METER = 32f; // Box2D 单位转换：32像素 = 1米
    private static final float BULLET_SPEED = 10f; // 子弹速度（米/秒）
    private boolean attackKeyPressed = false; // 攻击按键状态（避免连续触发）
    private Texture targetTexture; // 目标纹理（简单用颜色块代替）

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

        // 加载4向行走角色精灵图并分割成动画帧
        person4Texture = new Texture(Gdx.files.internal("person/p_4.png"));
        // 设置纹理过滤模式，避免黑线和锯齿
        person4Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        int frame4Width = person4Texture.getWidth() / 3; // 每行3帧
        int frame4Height = person4Texture.getHeight() / 4; // 共4行
        // 分割纹理：4行（向下、向左、向右、向上），每行3帧
        person4Frames = TextureRegion.split(person4Texture, frame4Width, frame4Height);

        // 加载8向行走角色精灵图并分割成动画帧
        person8Texture = new Texture(Gdx.files.internal("person/p_8.png"));
        // 设置纹理过滤模式，避免黑线和锯齿
        person8Texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        int frame8Width = person8Texture.getWidth() / 8; // 每行8帧
        int frame8Height = person8Texture.getHeight() / 8; // 共8行
        // 分割纹理：8行（下、左、右、上、左下、右下、左上、右上），每行8帧
        person8Frames = TextureRegion.split(person8Texture, frame8Width, frame8Height);

        // 使用当前人物类型的帧尺寸
        updateFrameSize();

        // 初始化角色位置（地图中心）
        float mapWidth = MAP_WIDTH_TILES * TILE_SIZE;
        float mapHeight = MAP_HEIGHT_TILES * TILE_SIZE;
        playerX = mapWidth / 2f;
        playerY = mapHeight / 2f;
        currentDirection = 0; // 初始方向向下
        animationTime = 0f;

        // 初始化 Box2D 物理世界
        box2DWorld = new World(new Vector2(0, 0), true); // 无重力
        debugRenderer = new Box2DDebugRenderer();

        // 创建 Box2D 相机（用于调试渲染）
        box2DCamera = new OrthographicCamera();
        box2DCamera.setToOrtho(false, VIRTUAL_WIDTH / PIXELS_PER_METER, VIRTUAL_HEIGHT / PIXELS_PER_METER);

        // 创建玩家物理身体
        BodyDef playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyType.DynamicBody;
        playerBodyDef.position.set(playerX / PIXELS_PER_METER, playerY / PIXELS_PER_METER);
        playerBody = box2DWorld.createBody(playerBodyDef);

        // 创建玩家碰撞形状（圆形）
        CircleShape playerShape = new CircleShape();
        playerShape.setRadius(frameWidth / 2f / PIXELS_PER_METER);
        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.shape = playerShape;
        playerFixtureDef.density = 1f;
        playerFixtureDef.friction = 0.3f;
        playerBody.createFixture(playerFixtureDef);
        playerShape.dispose();

        // 创建目标（预定义位置：地图右上角附近）
        targetX = mapWidth * 0.75f;
        targetY = mapHeight * 0.75f;

        BodyDef targetBodyDef = new BodyDef();
        targetBodyDef.type = BodyType.StaticBody;
        targetBodyDef.position.set(targetX / PIXELS_PER_METER, targetY / PIXELS_PER_METER);
        targetBody = box2DWorld.createBody(targetBodyDef);

        // 创建目标碰撞形状（圆形）
        CircleShape targetShape = new CircleShape();
        targetShape.setRadius(20f / PIXELS_PER_METER); // 目标半径20像素
        FixtureDef targetFixtureDef = new FixtureDef();
        targetFixtureDef.shape = targetShape;
        targetFixtureDef.isSensor = true; // 设为传感器，不产生物理碰撞
        targetBody.createFixture(targetFixtureDef);
        targetShape.dispose();

        // 初始化子弹列表
        bullets = new ArrayList<>();

        // 创建简单的目标纹理（红色方块）
        targetTexture = new Texture(40, 40, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(40, 40, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0, 0, 1); // 红色
        pixmap.fillCircle(20, 20, 20);
        targetTexture.draw(pixmap, 0, 0);
        pixmap.dispose();

        time = 0f;
    }

    /**
     * 根据当前人物类型更新帧尺寸
     */
    private void updateFrameSize() {
        if (currentCharacterType == CharacterType.FOUR_DIRECTION) {
            frameWidth = person4Texture.getWidth() / 3;
            frameHeight = person4Texture.getHeight() / 4;
        } else {
            frameWidth = person8Texture.getWidth() / 8;
            frameHeight = person8Texture.getHeight() / 8;
        }
    }

    /**
     * 切换人物类型（按T键）
     */
    private void handleCharacterSwitch() {
        if (Gdx.input.isKeyPressed(Input.Keys.T)) {
            if (!switchKeyPressed) {
                // 切换人物类型
                if (currentCharacterType == CharacterType.FOUR_DIRECTION) {
                    currentCharacterType = CharacterType.EIGHT_DIRECTION;
                } else {
                    currentCharacterType = CharacterType.FOUR_DIRECTION;
                }
                updateFrameSize();
                // 重置方向，避免索引越界
                currentDirection = 0;
                switchKeyPressed = true;
            }
        } else {
            switchKeyPressed = false;
        }
    }

    /**
     * 根据输入计算8向移动的方向
     * 返回方向索引：0=下, 1=左, 2=右, 3=上, 4=左下, 5=右下, 6=左上, 7=右上
     */
    private int calculate8Direction(boolean up, boolean down, boolean left, boolean right) {
        if (down && !up) {
            if (left && !right) return 4; // 左下
            if (right && !left) return 5; // 右下
            return 0; // 下
        }
        if (up && !down) {
            if (left && !right) return 6; // 左上
            if (right && !left) return 7; // 右上
            return 3; // 上
        }
        if (left && !right) return 1; // 左
        if (right && !left) return 2; // 右
        return currentDirection; // 保持当前方向
    }

    /**
     * 处理攻击功能（按空格键发射子弹）
     */
    private void handleAttack() {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (!attackKeyPressed) {
                // 计算子弹方向（朝向目标）
                float dx = targetX - playerX;
                float dy = targetY - playerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance > 0) {
                    // 归一化方向向量
                    float dirX = dx / distance;
                    float dirY = dy / distance;

                    // 创建子弹身体
                    BodyDef bulletDef = new BodyDef();
                    bulletDef.type = BodyType.DynamicBody;
                    bulletDef.position.set(playerX / PIXELS_PER_METER, playerY / PIXELS_PER_METER);
                    Body bullet = box2DWorld.createBody(bulletDef);

                    // 创建子弹碰撞形状（小圆形）
                    CircleShape bulletShape = new CircleShape();
                    bulletShape.setRadius(5f / PIXELS_PER_METER);
                    FixtureDef bulletFixtureDef = new FixtureDef();
                    bulletFixtureDef.shape = bulletShape;
                    bulletFixtureDef.density = 0.1f;
                    bulletFixtureDef.isSensor = true; // 设为传感器，用于检测碰撞
                    bullet.createFixture(bulletFixtureDef);
                    bulletShape.dispose();

                    // 设置子弹速度
                    bullet.setLinearVelocity(dirX * BULLET_SPEED, dirY * BULLET_SPEED);

                    // 添加到子弹列表
                    bullets.add(bullet);
                }

                attackKeyPressed = true;
            }
        } else {
            attackKeyPressed = false;
        }
    }

    /**
     * 更新子弹物理和碰撞检测
     */
    private void updateBullets(float delta) {
        Iterator<Body> iterator = bullets.iterator();
        float mapWidth = MAP_WIDTH_TILES * TILE_SIZE;
        float mapHeight = MAP_HEIGHT_TILES * TILE_SIZE;

        while (iterator.hasNext()) {
            Body bullet = iterator.next();
            Vector2 bulletPos = bullet.getPosition();
            float bulletX = bulletPos.x * PIXELS_PER_METER;
            float bulletY = bulletPos.y * PIXELS_PER_METER;

            // 检查子弹是否击中目标（简单距离检测）
            float dx = bulletX - targetX;
            float dy = bulletY - targetY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 30f) {
                // 击中目标，移除子弹
                box2DWorld.destroyBody(bullet);
                iterator.remove();
                // 可以在这里添加击中效果
            } else if (bulletX < 0 || bulletX > mapWidth || bulletY < 0 || bulletY > mapHeight) {
                // 子弹飞出地图，移除
                box2DWorld.destroyBody(bullet);
                iterator.remove();
            }
        }
    }

    @Override
    public void render(float delta) {
        // 让时间累积，用来做简单动画
        time += delta;

        // 处理人物切换（按T键）
        handleCharacterSwitch();

        // 处理攻击（按空格键发射子弹）
        handleAttack();

        // 键盘输入控制角色移动（WASD + 方向键）
        boolean isMoving = false;
        float moveSpeed = playerSpeed * delta;

        // 检测按键状态
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        if (currentCharacterType == CharacterType.FOUR_DIRECTION) {
            // 4向行走：只处理上下左右，不支持对角线
            if (up) {
                playerY += moveSpeed;
                currentDirection = 3; // 向上
                isMoving = true;
            }
            if (down) {
                playerY -= moveSpeed;
                currentDirection = 0; // 向下
                isMoving = true;
            }
            if (left) {
                playerX -= moveSpeed;
                currentDirection = 1; // 向左
                isMoving = true;
            }
            if (right) {
                playerX += moveSpeed;
                currentDirection = 2; // 向右
                isMoving = true;
            }
        } else {
            // 8向行走：支持对角线移动
            if (up || down || left || right) {
                // 计算8向方向
                currentDirection = calculate8Direction(up, down, left, right);

                // 对角线移动时速度需要除以根号2，保持总速度一致
                float diagonalSpeed = moveSpeed / 1.414f;

                if (up) playerY += (left || right) ? diagonalSpeed : moveSpeed;
                if (down) playerY -= (left || right) ? diagonalSpeed : moveSpeed;
                if (left) playerX -= (up || down) ? diagonalSpeed : moveSpeed;
                if (right) playerX += (up || down) ? diagonalSpeed : moveSpeed;

                isMoving = true;
            }
        }

        // 限制角色位置在地图内部
        float mapWidth = MAP_WIDTH_TILES * TILE_SIZE;
        float mapHeight = MAP_HEIGHT_TILES * TILE_SIZE;
        playerX = MathUtils.clamp(playerX, frameWidth / 2f, mapWidth - frameWidth / 2f);
        playerY = MathUtils.clamp(playerY, frameHeight / 2f, mapHeight - frameHeight / 2f);

        // 同步玩家位置到 Box2D 身体
        playerBody.setTransform(playerX / PIXELS_PER_METER, playerY / PIXELS_PER_METER, 0);

        // 更新子弹物理
        updateBullets(delta);

        // 更新 Box2D 世界
        box2DWorld.step(delta, 6, 2);

        // 更新动画时间（只在移动时更新）
        if (isMoving) {
            animationTime += delta;
        } else {
            animationTime = 0f; // 停止时重置到第一帧
        }

        // 让相机跟随角色
        camera.position.set(playerX, playerY, 0);
        // 限制相机范围在地图内部（避免移出地图外面全是空）
        float halfViewWidth = VIRTUAL_WIDTH / 2f;
        float halfViewHeight = VIRTUAL_HEIGHT / 2f;
        camera.position.x = MathUtils.clamp(camera.position.x, halfViewWidth, mapWidth - halfViewWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfViewHeight, mapHeight - halfViewHeight);

        // 对相机位置取整，避免亚像素渲染导致的黑线
        camera.position.x = MathUtils.floor(camera.position.x);
        camera.position.y = MathUtils.floor(camera.position.y);

        // 清屏为黑色，地图会覆盖背景
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新摄像机（在取整之后）
        camera.update();

        // 先绘制 Tiled 地图
        if (mapRenderer != null) {
            mapRenderer.setView(camera);
            mapRenderer.render();
        }

        // 绘制角色
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 计算当前应该显示的动画帧索引
        TextureRegion currentFrame;
        if (currentCharacterType == CharacterType.FOUR_DIRECTION) {
            // 4向：每行3帧
            int frameIndex = (int) (animationTime / ANIMATION_FRAME_DURATION) % 3;
            currentFrame = person4Frames[currentDirection][frameIndex];
        } else {
            // 8向：每行8帧
            int frameIndex = (int) (animationTime / ANIMATION_FRAME_DURATION) % 8;
            currentFrame = person8Frames[currentDirection][frameIndex];
        }

        // 计算角色绘制位置（角色中心在世界坐标中）
        // 对绘制位置取整，避免亚像素渲染导致的黑线
        float drawX = MathUtils.floor(playerX - frameWidth / 2f);
        float drawY = MathUtils.floor(playerY - frameHeight / 2f);

        // 绘制角色
        batch.draw(currentFrame, drawX, drawY);

        // 绘制目标（对位置取整）
        float targetDrawX = MathUtils.floor(targetX - 20f);
        float targetDrawY = MathUtils.floor(targetY - 20f);
        batch.draw(targetTexture, targetDrawX, targetDrawY, 40, 40);

        // 绘制子弹（对位置取整）
        for (Body bullet : bullets) {
            Vector2 bulletPos = bullet.getPosition();
            float bulletX = bulletPos.x * PIXELS_PER_METER;
            float bulletY = bulletPos.y * PIXELS_PER_METER;
            // 简单绘制一个小圆点表示子弹
            batch.setColor(1, 1, 0, 1); // 黄色
            batch.draw(targetTexture, MathUtils.floor(bulletX - 5), MathUtils.floor(bulletY - 5), 10, 10);
            batch.setColor(1, 1, 1, 1); // 恢复白色
        }

        // 再绘制文本（叠加在地图之上）
        // 注意：文本坐标需要转换为世界坐标，这里简单使用屏幕坐标
        float textX = camera.position.x - VIRTUAL_WIDTH / 2f + 50;
        float textY = camera.position.y + VIRTUAL_HEIGHT / 2f - 50;
        font.draw(batch, "WASD/方向键控制角色", textX, textY);
        font.draw(batch, "按T键切换人物", textX, textY - 40);
        font.draw(batch, "按空格键攻击", textX, textY - 80);
        font.draw(batch, "当前: " + (currentCharacterType == CharacterType.FOUR_DIRECTION ? "4向" : "8向"), textX, textY - 120);
        font.draw(batch, "Time: " + String.format("%.2f s", time), textX, textY - 160);
        batch.end();

        // 可选：绘制 Box2D 调试视图（按F1键切换）
        if (Gdx.input.isKeyPressed(Input.Keys.F1)) {
            box2DCamera.position.set(camera.position.x / PIXELS_PER_METER, camera.position.y / PIXELS_PER_METER, 0);
            box2DCamera.update();
            debugRenderer.render(box2DWorld, box2DCamera.combined);
        }
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
        if (person4Texture != null) {
            person4Texture.dispose();
        }
        if (person8Texture != null) {
            person8Texture.dispose();
        }
        if (targetTexture != null) {
            targetTexture.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
        if (box2DWorld != null) {
            // 清理所有子弹
            for (Body bullet : bullets) {
                box2DWorld.destroyBody(bullet);
            }
            bullets.clear();
            box2DWorld.dispose();
        }
    }
}
