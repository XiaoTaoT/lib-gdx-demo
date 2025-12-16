package com.github.xiaotaotao.ligdx.laboratory;

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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.xiaotaotao.ligdx.laboratory.character.AttackSystem;
import com.github.xiaotaotao.ligdx.laboratory.character.AttributeSystem;
import com.github.xiaotaotao.ligdx.laboratory.character.MovementController;
import com.github.xiaotaotao.ligdx.laboratory.character.PixelCharacter;
import com.github.xiaotaotao.ligdx.laboratory.character.SimplePixelCharacter;
import com.github.xiaotaotao.ligdx.laboratory.character.SkillSystem;

/**
 * 像素角色系统演示 Screen：
 *
 * - 使用颜色块代替角色/敌人
 * - 展示：
 *   - 属性系统：HP / 攻击 / 防御 等，以及简单伤害公式
 *   - 移动控制器：基于 16 像素网格的移动，限制在战斗区域内
 *   - 攻击系统：普攻在指定动画帧（第 3 帧）触发命中判定
 *   - 技能系统：简单火球技能，带施法前摇 / 后摇 / 冷却
 *   - 像素飘血：伤害结算时生成小号像素数字
 *
 * 操作说明：
 * - 方向键 / WASD：移动玩家
 * - SPACE：普攻（近战，靠近敌人时造成伤害）
 * - Q：释放火球技能（远程伤害，带冷却）
 */
public class CharacterDemoScreen implements Screen {

    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;

    private SpriteBatch batch;
    private BitmapFont fontSmall;
    private OrthographicCamera camera;
    private Viewport viewport;

    // 以颜色块代替的“贴图”
    private Texture playerTexture;
    private Texture enemyTexture;

    // 系统化的像素角色（使用两种不同移动实现做对比）
    private PixelCharacter freePlayer;   // 连续移动
    private PixelCharacter gridPlayer;   // 栅格移动
    private AttributeSystem.BasicAttributeSystem enemyAttributes;
    private MovementController.GridMovementController enemyMovement;

    // 飘血数字
    private final Array<FloatingText> floatingTexts = new Array<>();

    // 技能 ID 常量
    private static final String SKILL_FIREBALL = "fireball";

    // 调试用：记录输入和位置等信息
    private float debugInputX, debugInputY;
    private float debugDelta;

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);

        batch = new SpriteBatch();

        // 暂时使用 LibGDX 默认字体，避免依赖外部字体资源
        fontSmall = new BitmapFont();

        createDebugTextures();
        createCharacters();
    }

    private void createDebugTextures() {
        // 简单的 1x1 像素纹理，用于拉伸绘制
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 1, 0, 1);
        pixmap.fill();
        playerTexture = new Texture(pixmap);

        pixmap.setColor(1, 0, 0, 1);
        pixmap.fill();
        enemyTexture = new Texture(pixmap);

        pixmap.dispose();
    }

    private void createCharacters() {
        // 公共移动边界（战斗区域）
        Rectangle bounds = new Rectangle(80, 80, VIRTUAL_WIDTH - 160, VIRTUAL_HEIGHT - 160);

        // ===== 玩家角色：属性 + 移动 + 攻击 + 技能 =====
        AttributeSystem.BasicAttributeSystem playerAttr =
                new AttributeSystem.BasicAttributeSystem(
                        100, // maxHp
                        50,  // maxMp
                        20,  // attack
                        5,   // defense
                        2.0f, // attackSpeed（这里暂未直接使用）
                        120f  // moveSpeed 像素/秒
                );

        // 玩家飘血监听（主要演示：受到伤害时生成红色/-值；治疗时可以扩展为绿色/+值）
        playerAttr.setListener(new AttributeSystem.Listener() {
            @Override
            public void onStatsChanged(AttributeSystem.Stats newStats) {
                // 本 Demo 中玩家暂不被敌人攻击，此处保留以便扩展
            }

            @Override
            public void onDamageResolved(AttributeSystem.DamageResult result) {
                // 可以在这里生成玩家受到伤害的飘血
            }
        });

        // 近战普攻：6 帧动画，第 3 帧为打击帧，总时长 0.4 秒
        AttackSystem.BasicMeleeAttackSystem meleeAttack =
                new AttackSystem.BasicMeleeAttackSystem(
                        6,
                        2,        // 第 3 帧（索引从 0 开始）
                        0.4f,
                        () -> tryMeleeHit()
                );

        // 技能系统：一个简单的火球技能
        SkillSystem.BasicSkillSystem skillSystem =
                new SkillSystem.BasicSkillSystem(skill -> {
                    if (SKILL_FIREBALL.equals(skill.id)) {
                        trySkillHit(skill);
                    }
                });
        skillSystem.addSkill(new SkillSystem.Skill(
                SKILL_FIREBALL,
                "火球术",
                0.3f,   // 前摇
                0.2f,   // 后摇
                2.0f,   // 冷却
                30      // 基础伤害
        ));

        // 创建两个聚合角色：一个使用连续移动，一个使用栅格移动
        MovementController freeMove = new MovementController.FreeMovementController(playerAttr.getStats());
        MovementController gridMove = new MovementController.GridMovementController(playerAttr.getStats(), 16f);

        freePlayer = new SimplePixelCharacter(
                "free-player",
                playerAttr,
                freeMove,
                meleeAttack,
                skillSystem,
                bounds
        );
        gridPlayer = new SimplePixelCharacter(
                "grid-player",
                playerAttr,
                gridMove,
                meleeAttack,
                skillSystem,
                bounds
        );

        // 初始位置：左侧中间上/下各放一个，便于观察差异
        freePlayer.getMovementController().setPosition(bounds.x + 40, bounds.y + bounds.height * 0.65f);
        gridPlayer.getMovementController().setPosition(bounds.x + 40, bounds.y + bounds.height * 0.35f);

        // ===== 敌人：只用属性 + 简单位置（暂不实现 AI 寻路） =====
        enemyAttributes = new AttributeSystem.BasicAttributeSystem(
                150, // maxHp
                0,
                10,
                2,
                1.0f,
                0f
        );
        enemyAttributes.setListener(new AttributeSystem.Listener() {
            @Override
            public void onStatsChanged(AttributeSystem.Stats newStats) {
                // 属性变化时可以刷新 UI（如血条），这里 Demo 直接用文本显示
            }

            @Override
            public void onDamageResolved(AttributeSystem.DamageResult result) {
                // 敌人头顶飘血（红色数字）
                FloatingText ft = new FloatingText();
                ft.text = "-" + result.finalDamage;
                ft.position.set(enemyPos);
                ft.position.y += 40; // 在敌人上方
                ft.color.set(1f, result.critical ? 0.8f : 0.3f, 0.3f, 1f);
                ft.lifetime = 0.6f;
                ft.velocity.set(0, 30); // 向上飘
                floatingTexts.add(ft);
            }
        });
        enemyPos.set(bounds.x + bounds.width - 80, bounds.y + bounds.height / 2f);
        enemyMovement = new MovementController.GridMovementController(
                enemyAttributes.getStats(),
                16f
        );
        enemyMovement.setPosition(enemyPos.x, enemyPos.y);
        enemyMovement.setBounds(bounds);
    }

    // 敌人当前位置（无需复杂 AI，此处固定站桩）
    private final Vector2 enemyPos = new Vector2();

    private void tryMeleeHit() {
        if (enemyAttributes.isDead()) return;
        // 简单近战判定：玩家与敌人的距离 < 64 像素即视为命中（此处使用连续移动玩家做示例）
        Vector2 p = freePlayer.getPosition();
        enemyPos.set(enemyMovement.getPosition());
        float dst2 = p.dst2(enemyPos);
        if (dst2 <= 64 * 64) {
            AttributeSystem.DamageRequest req = new AttributeSystem.DamageRequest();
            req.attackerStats = freePlayer.getAttributes().getStats();
            req.defenderStats = enemyAttributes.getStats();
            req.baseDamage = 5;
            req.critical = MathUtils.randomBoolean(0.2f);
            req.criticalMul = 1.5f;
            enemyAttributes.applyDamage(req);
        }
    }

    private void trySkillHit(SkillSystem.Skill skill) {
        if (enemyAttributes.isDead()) return;
        // 火球示例：只要敌人在屏幕内，就直接命中（可扩展为弹道/范围判定）
        AttributeSystem.DamageRequest req = new AttributeSystem.DamageRequest();
        req.attackerStats = freePlayer.getAttributes().getStats();
        req.defenderStats = enemyAttributes.getStats();
        req.baseDamage = skill.baseDamage;
        req.critical = MathUtils.randomBoolean(0.3f);
        req.criticalMul = 1.8f;
        enemyAttributes.applyDamage(req);
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        // 更新角色 & 飘血
        freePlayer.update(delta);
        gridPlayer.update(delta);
        enemyMovement.update(delta); // 目前敌人不动，但保留逻辑
        updateFloatingText(delta);

        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawBattleField();
        drawCharacters();
        drawUI();
        drawFloatingText();
        batch.end();
    }

    private void handleInput(float delta) {
        // 连续移动玩家：WASD 控制
        MovementController freeMove = freePlayer.getMovementController();
        float fx = 0, fy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) fx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) fx += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) fy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) fy -= 1;
        freeMove.setInputDirection(fx, fy);

        // 栅格移动玩家：方向键控制
        MovementController gridMove = gridPlayer.getMovementController();
        float gx = 0, gy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) gx -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) gx += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) gy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) gy -= 1;
        gridMove.setInputDirection(gx, gy);

        // 记录调试信息（以连续移动玩家为主）
        debugInputX = fx;
        debugInputY = fy;
        debugDelta = delta;

        // 普攻（用连续移动玩家演示）
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            freePlayer.getAttackSystem().requestAttack();
        }

        // 火球术
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            freePlayer.getSkillSystem().cast(SKILL_FIREBALL);
        }
    }

    private void drawBattleField() {
        // 简单画出战斗边界矩形（用深色块表示）
        float margin = 80f;
        float w = VIRTUAL_WIDTH - margin * 2;
        float h = VIRTUAL_HEIGHT - margin * 2;
        batch.setColor(0.1f, 0.15f, 0.2f, 1f);
        batch.draw(playerTexture, margin, margin, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawCharacters() {
        float size = 32f;

        // 连续移动玩家（绿色）
        Vector2 fp = freePlayer.getPosition();
        float freeX = Math.round(fp.x - size / 2f);
        float freeY = Math.round(fp.y - size / 2f);
        batch.setColor(0.3f, 1f, 0.3f, 1f);
        batch.draw(playerTexture, freeX, freeY, size, size);

        // 栅格移动玩家（蓝色）
        Vector2 gp = gridPlayer.getPosition();
        float gridX = Math.round(gp.x - size / 2f);
        float gridY = Math.round(gp.y - size / 2f);
        batch.setColor(0.3f, 0.6f, 1f, 1f);
        batch.draw(playerTexture, gridX, gridY, size, size);

        // 连续移动玩家的攻击效果圈
        if (freePlayer.getAttackSystem().isAttacking()) {
            int frameIndex = freePlayer.getAttackSystem().getCurrentFrameIndex();
            float alpha = 0.2f + 0.1f * frameIndex;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(playerTexture, freeX - size / 2f, freeY - size / 2f, size * 2, size * 2);
        }
        batch.setColor(1f, 1f, 1f, 1f);

        // 敌人
        enemyPos.set(enemyMovement.getPosition());
        float eSize = 32f;
        if (!enemyAttributes.isDead()) {
            batch.draw(enemyTexture, enemyPos.x - eSize / 2f, enemyPos.y - eSize / 2f, eSize, eSize);
        } else {
            // 死亡后画成灰色小块
            batch.setColor(0.4f, 0.4f, 0.4f, 1f);
            batch.draw(enemyTexture, enemyPos.x - eSize / 2f, enemyPos.y - eSize / 2f, eSize, eSize);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawUI() {
        // 左上角：玩家属性
        AttributeSystem.Stats ps = freePlayer.getAttributes().getStats();
        fontSmall.setColor(1f, 1f, 1f, 1f);
        fontSmall.draw(batch, "Player HP: " + ps.hp + "/" + ps.maxHp, 20, VIRTUAL_HEIGHT - 20);
        fontSmall.draw(batch, "ATK: " + ps.attack + "  DEF: " + ps.defense, 20, VIRTUAL_HEIGHT - 40);

        // 右上角：敌人 HP
        AttributeSystem.Stats es = enemyAttributes.getStats();
        fontSmall.setColor(1f, 0.6f, 0.6f, 1f);
        fontSmall.draw(batch, "Enemy HP: " + es.hp + "/" + es.maxHp, VIRTUAL_WIDTH - 200, VIRTUAL_HEIGHT - 20);

        // 底部：操作提示 & 技能状态
        fontSmall.setColor(1f, 1f, 1f, 1f);
        fontSmall.draw(batch, "Move: WASD / Arrow  Attack: SPACE  Skill[Q]: Fireball", 20, 40);

         SkillSystem.Skill fireball = freePlayer.getSkillSystem().getSkill(SKILL_FIREBALL);
        if (fireball != null) {
            String stateText = "READY";
            if (fireball.state == SkillSystem.State.CASTING) stateText = "CASTING";
            else if (fireball.state == SkillSystem.State.BACK_SWING) stateText = "BACK_SWING";
            else if (fireball.state == SkillSystem.State.COOLDOWN) {
                stateText = String.format("CD: %.1fs", fireball.cdRemaining);
            }
            fontSmall.draw(batch, "[Q] Fireball - " + stateText, 20, 20);
        }

         // 调试信息：输入方向 / 位置 / 速度（以连续移动玩家为主）
         Vector2 p = freePlayer.getPosition();
        fontSmall.setColor(0.6f, 0.9f, 1f, 1f);
        fontSmall.draw(batch,
                String.format("Debug Input: (%.0f, %.0f)  delta=%.3f", debugInputX, debugInputY, debugDelta),
                20, VIRTUAL_HEIGHT - 70);
        fontSmall.draw(batch,
                String.format("Debug Pos: (%.1f, %.1f)  moveSpeed=%.1f",
                        p.x, p.y, ps.moveSpeed),
                20, VIRTUAL_HEIGHT - 90);
    }

    private void updateFloatingText(float delta) {
        for (int i = floatingTexts.size - 1; i >= 0; i--) {
            FloatingText ft = floatingTexts.get(i);
            ft.lifetime -= delta;
            if (ft.lifetime <= 0) {
                floatingTexts.removeIndex(i);
            } else {
                ft.position.mulAdd(ft.velocity, delta);
                ft.color.a = MathUtils.clamp(ft.lifetime / 0.6f, 0f, 1f);
            }
        }
    }

    private void drawFloatingText() {
        for (FloatingText ft : floatingTexts) {
            fontSmall.setColor(ft.color);
            fontSmall.draw(batch, ft.text, ft.position.x, ft.position.y);
        }
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
        if (fontSmall != null) fontSmall.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
    }

    /** 简单的飘字结构体 */
    private static class FloatingText {
        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        final com.badlogic.gdx.graphics.Color color = new com.badlogic.gdx.graphics.Color();
        String text;
        float lifetime;
    }
}


