package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

/**
 * 攻击系统演示 Screen
 * 
 * 功能展示：
 * - 角色攻击系统（武器、攻击方式、攻击范围）
 * - 扇形攻击范围（挥击）
 * - 矩形攻击范围（斩击）
 * - 攻击冷却管理
 * - 伤害计算和受击反馈
 * 
 * 操作说明：
 * - WASD：移动角色
 * - J：斩击（矩形范围，正前方）
 * - K：挥击（扇形范围，120度）
 * - R：重置场景
 */
public class AttackDemoScreen implements Screen {
    
    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;
    
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    
    /** 简单的纹理（用于绘制角色和敌人） */
    private Texture playerTexture;
    private Texture enemyTexture;
    private Texture rangeTexture;
    
    /** 玩家角色 */
    private KnifemanCharacter player;
    
    /** 敌人列表 */
    private final Array<SimpleEnemy> enemies = new Array<>();
    
    /** 当前攻击范围顶点（用于绘制攻击范围） */
    private List<Vector2> currentRangeVertices;
    
    /** 攻击范围显示时间（攻击后显示一段时间） */
    private float rangeDisplayTime = 0f;
    private static final float RANGE_DISPLAY_DURATION = 0.3f;
    
    /** 玩家移动速度（像素/秒） */
    private static final float PLAYER_MOVE_SPEED = 200f;
    
    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        
        batch = new SpriteBatch();
        font = new BitmapFont();
        
        createTextures();
        createCharacters();
    }
    
    /**
     * 创建简单的纹理（颜色块）
     */
    private void createTextures() {
        // 玩家纹理（绿色）
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 1f, 0.2f, 1f);
        pixmap.fill();
        playerTexture = new Texture(pixmap);
        
        // 敌人纹理（红色）
        pixmap.setColor(1f, 0.2f, 0.2f, 1f);
        pixmap.fill();
        enemyTexture = new Texture(pixmap);
        
        // 攻击范围纹理（半透明黄色）
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 0f, 0.5f);
        pixmap.fill();
        rangeTexture = new Texture(pixmap);
        
        pixmap.dispose();
    }
    
    /**
     * 创建角色和敌人
     */
    private void createCharacters() {
        // 创建玩家（屏幕左侧中间）
        player = new KnifemanCharacter(200, VIRTUAL_HEIGHT / 2f);
        
        // 创建几个敌人（分散在屏幕右侧，确保不重叠）
        enemies.clear();
        
        // 敌人初始位置（确保不重叠）
        float[] enemyPositions = {
            1400, VIRTUAL_HEIGHT / 2f,
            1500, VIRTUAL_HEIGHT / 2f + 100,
            1500, VIRTUAL_HEIGHT / 2f - 100,
            1600, VIRTUAL_HEIGHT / 2f
        };
        
        for (int i = 0; i < enemyPositions.length; i += 2) {
            SimpleEnemy enemy = new SimpleEnemy(enemyPositions[i], enemyPositions[i + 1]);
            
            // 检查是否与已存在的敌人重叠，如果重叠则调整位置
            Vector2 enemyPos = enemy.getPixelPosition();
            for (int attempts = 0; attempts < 10; attempts++) {
                boolean overlaps = false;
                for (SimpleEnemy existing : enemies) {
                    if (CollisionSystem.checkCollision(enemyPos, existing.getPixelPosition())) {
                        overlaps = true;
                        break;
                    }
                }
                
                // 检查是否与玩家重叠
                if (!overlaps && CollisionSystem.checkCollision(enemyPos, player.getPixelPosition())) {
                    overlaps = true;
                }
                
                if (!overlaps) {
                    break;
                }
                
                // 如果重叠，尝试新位置（随机偏移）
                enemyPos.x += MathUtils.random(-50, 50);
                enemyPos.y += MathUtils.random(-50, 50);
                enemyPos.x = MathUtils.clamp(enemyPos.x, 1000, VIRTUAL_WIDTH - 100);
                enemyPos.y = MathUtils.clamp(enemyPos.y, 100, VIRTUAL_HEIGHT - 100);
                enemy.setPosition(enemyPos.x, enemyPos.y);
            }
            
            enemies.add(enemy);
        }
    }
    
    @Override
    public void render(float delta) {
        handleInput(delta);
        update(delta);
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        drawCharacters();
        drawAttackRange();
        drawUI();
        batch.end();
    }
    
    /**
     * 处理输入
     */
    private void handleInput(float delta) {
        // 移动输入
        float moveX = 0, moveY = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) moveX -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) moveX += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) moveY += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) moveY -= 1;
        
        // 更新玩家位置（带碰撞检测）
        if (moveX != 0 || moveY != 0) {
            Vector2 currentPos = player.getPixelPosition();
            Vector2 dir = new Vector2(moveX, moveY).nor();
            
            // 计算目标位置
            Vector2 targetPos = currentPos.cpy();
            targetPos.add(dir.x * PLAYER_MOVE_SPEED * delta, dir.y * PLAYER_MOVE_SPEED * delta);
            
            // 检查与所有敌人的碰撞，并调整目标位置
            CollisionSystem.checkMovementAgainstAll(currentPos, targetPos, player);
            
            // 限制在屏幕内
            targetPos.x = MathUtils.clamp(targetPos.x, 0, VIRTUAL_WIDTH);
            targetPos.y = MathUtils.clamp(targetPos.y, 0, VIRTUAL_HEIGHT);
            
            // 更新玩家位置
            player.setPosition(targetPos.x, targetPos.y);
        }
        
        // 攻击输入
        Vector2 attackDir = new Vector2(1, 0); // 默认向右攻击
        if (moveX != 0 || moveY != 0) {
            attackDir.set(moveX, moveY).nor();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            // 斩击（矩形范围）
            performAttack(AttackStyle.AttackStyleType.SLASH, attackDir);
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            // 挥击（扇形范围）
            performAttack(AttackStyle.AttackStyleType.SWING, attackDir);
        }
        
        // 重置场景
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetScene();
        }
    }
    
    /**
     * 执行攻击
     */
    private void performAttack(AttackStyle.AttackStyleType styleType, Vector2 dir) {
        // 检查是否在硬直状态
        if (player.isInStiffness()) {
            return;
        }
        
        // 执行攻击
        player.performNormalAttack(styleType, dir);
        
        // 获取攻击范围并显示
        Weapon weapon = player.getEquippedWeapon();
        if (weapon != null) {
            AttackStyle style = weapon.getSupportedAttackStyles().get(styleType);
            if (style != null) {
                currentRangeVertices = style.getRangeStrategy().getRangeVertices(
                    player.getPixelPosition(),
                    dir,
                    weapon.getBasePixelUnit()
                );
                rangeDisplayTime = RANGE_DISPLAY_DURATION;
            }
        }
    }
    
    /**
     * 更新逻辑
     */
    private void update(float delta) {
        // 更新攻击范围显示时间
        if (rangeDisplayTime > 0) {
            rangeDisplayTime -= delta;
            if (rangeDisplayTime <= 0) {
                currentRangeVertices = null;
            }
        }
        
        // 移除死亡的敌人
        for (int i = enemies.size - 1; i >= 0; i--) {
            if (enemies.get(i).isDead()) {
                enemies.get(i).dispose();
                enemies.removeIndex(i);
            }
        }
    }
    
    /**
     * 绘制角色和敌人
     */
    private void drawCharacters() {
        // 绘制玩家
        Vector2 playerPos = player.getPixelPosition();
        batch.setColor(0.2f, 1f, 0.2f, 1f);
        batch.draw(playerTexture, playerPos.x - 16, playerPos.y - 16, 32, 32);
        
        // 绘制敌人
        batch.setColor(1f, 0.2f, 0.2f, 1f);
        for (SimpleEnemy enemy : enemies) {
            if (!enemy.isDead()) {
                Vector2 enemyPos = enemy.getPixelPosition();
                batch.draw(enemyTexture, enemyPos.x - 16, enemyPos.y - 16, 32, 32);
            }
        }
        
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    /**
     * 绘制攻击范围
     */
    private void drawAttackRange() {
        if (currentRangeVertices == null || currentRangeVertices.isEmpty()) {
            return;
        }
        
        float alpha = 0.3f * (rangeDisplayTime / RANGE_DISPLAY_DURATION);
        batch.setColor(1f, 1f, 0f, alpha);
        
        // 绘制范围轮廓（简化版：绘制从中心到各个顶点的线，用多个小点模拟）
        Vector2 center = currentRangeVertices.get(0);
        
        // 绘制从中心到各个顶点的线
        for (int i = 1; i < currentRangeVertices.size(); i++) {
            Vector2 vertex = currentRangeVertices.get(i);
            float dx = vertex.x - center.x;
            float dy = vertex.y - center.y;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (length > 0) {
                // 用多个小点绘制线段
                int pointCount = (int) (length / 4); // 每4像素一个点
                for (int j = 0; j <= pointCount; j++) {
                    float t = j / (float) pointCount;
                    float x = center.x + dx * t;
                    float y = center.y + dy * t;
                    batch.draw(rangeTexture, x - 2, y - 2, 4, 4);
                }
            }
        }
        
        // 如果是扇形（3个顶点），绘制弧线
        if (currentRangeVertices.size() == 3) {
            Vector2 left = currentRangeVertices.get(1);
            Vector2 right = currentRangeVertices.get(2);
            // 在左右边界之间绘制一些点（模拟弧线）
            for (int i = 0; i < 30; i++) {
                float t = i / 29f;
                // 简单的线性插值（实际应该是圆弧，这里简化）
                float x = center.x + (left.x - center.x) * (1 - t) + (right.x - center.x) * t;
                float y = center.y + (left.y - center.y) * (1 - t) + (right.y - center.y) * t;
                batch.draw(rangeTexture, x - 2, y - 2, 4, 4);
            }
        }
        
        batch.setColor(1f, 1f, 1f, 1f);
    }
    
    /**
     * 绘制 UI
     */
    private void drawUI() {
        font.setColor(1f, 1f, 1f, 1f);
        
        // 玩家信息
        font.draw(batch, "Player HP: " + player.getHp() + "/" + player.getMaxHp(), 20, VIRTUAL_HEIGHT - 20);
        font.draw(batch, "Attack: " + player.getAttackAttr(), 20, VIRTUAL_HEIGHT - 40);
        
        // 冷却时间
        long slashCD = player.getCooldownManager().getRemainingCooldown(AttackStyle.AttackStyleType.SLASH);
        long swingCD = player.getCooldownManager().getRemainingCooldown(AttackStyle.AttackStyleType.SWING);
        font.draw(batch, "Slash CD: " + (slashCD > 0 ? (slashCD / 100f) + "s" : "Ready"), 20, VIRTUAL_HEIGHT - 60);
        font.draw(batch, "Swing CD: " + (swingCD > 0 ? (swingCD / 100f) + "s" : "Ready"), 20, VIRTUAL_HEIGHT - 80);
        
        // 硬直状态
        if (player.isInStiffness()) {
            font.setColor(1f, 0.5f, 0.5f, 1f);
            font.draw(batch, "STIFFNESS", 20, VIRTUAL_HEIGHT - 100);
            font.setColor(1f, 1f, 1f, 1f);
        }
        
        // 操作提示
        font.draw(batch, "WASD: Move  J: Slash  K: Swing  R: Reset", 20, 40);
        
        // 敌人数量
        int aliveEnemies = 0;
        for (SimpleEnemy enemy : enemies) {
            if (!enemy.isDead()) aliveEnemies++;
        }
        font.draw(batch, "Enemies: " + aliveEnemies, 20, 20);
    }
    
    /**
     * 重置场景
     */
    private void resetScene() {
        // 清理所有敌人
        for (SimpleEnemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
        
        // 重新创建
        createCharacters();
    }
    
    @Override
    public void resize(int width, int height) {
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
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (rangeTexture != null) rangeTexture.dispose();
        if (player != null) player.dispose();
        for (SimpleEnemy enemy : enemies) {
            enemy.dispose();
        }
    }
    
    /**
     * 简单的敌人实现（用于演示）
     */
    private static class SimpleEnemy implements Attackable {
        private final Vector2 position = new Vector2();
        private int hp = 50;
        
        public SimpleEnemy(float x, float y) {
            position.set(x, y);
            EntityManager.getInstance().register(this);
        }
        
        /**
         * 设置敌人位置（用于碰撞调整）
         * 
         * @param x X 坐标
         * @param y Y 坐标
         */
        public void setPosition(float x, float y) {
            position.set(x, y);
        }
        
        @Override
        public void takeDamage(int damage, Character attacker) {
            hp = Math.max(0, hp - damage);
        }
        
        @Override
        public Vector2 getPixelPosition() {
            return position;
        }
        
        @Override
        public AttackCollider getAttackCollider() {
            AttackCollider collider = new AttackCollider();
            collider.offset.set(-16, -16);
            collider.width = 32;
            collider.height = 32;
            return collider;
        }
        
        @Override
        public boolean isAttackable() {
            return !isDead();
        }
        
        public boolean isDead() {
            return hp <= 0;
        }
        
        public void dispose() {
            EntityManager.getInstance().unregister(this);
        }
    }
}

