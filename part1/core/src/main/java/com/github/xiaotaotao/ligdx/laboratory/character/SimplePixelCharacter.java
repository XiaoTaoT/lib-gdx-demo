package com.github.xiaotaotao.ligdx.laboratory.character;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * 一个聚合四大系统的简单像素角色实现：
 * - BasicAttributeSystem 管理属性与伤害
 * - GridMovementController 负责 8/16 像素对齐移动
 * - BasicMeleeAttackSystem 负责普攻时序与打击帧
 * - BasicSkillSystem 管理技能池与技能时序
 *
 * 渲染 / 粒子 / UI：
 * - 可通过监听 AttributeSystem.Listener / AttackSystem.BasicMeleeAttackSystem.Listener /
 *   SkillSystem.Listener 来触发飘血、命中特效、技能特效等。
 */
public class SimplePixelCharacter implements PixelCharacter {

    private final String id;
    private final AttributeSystem.BasicAttributeSystem attributeSystem;
    private final MovementController movementController;
    private final AttackSystem.BasicMeleeAttackSystem attackSystem;
    private final SkillSystem.BasicSkillSystem skillSystem;
    private final Vector2 sharedPosition = new Vector2();

    /**
     * 使用“按格子走路”的移动方式创建角色（栅格移动）。
     */
    public SimplePixelCharacter(String id,
                                AttributeSystem.BasicAttributeSystem attributeSystem,
                                float gridSize,
                                AttackSystem.BasicMeleeAttackSystem attackSystem,
                                SkillSystem.BasicSkillSystem skillSystem,
                                Rectangle moveBounds) {
        this(id,
                attributeSystem,
                new MovementController.GridMovementController(attributeSystem.getStats(), gridSize),
                attackSystem,
                skillSystem,
                moveBounds);
    }

    /**
     * 使用自定义 MovementController 创建角色，
     * 方便在 Demo 中切换不同移动实现（连续移动 / 栅格移动等）。
     */
    public SimplePixelCharacter(String id,
                                AttributeSystem.BasicAttributeSystem attributeSystem,
                                MovementController movementController,
                                AttackSystem.BasicMeleeAttackSystem attackSystem,
                                SkillSystem.BasicSkillSystem skillSystem,
                                Rectangle moveBounds) {
        this.id = id;
        this.attributeSystem = attributeSystem;
        this.movementController = movementController;
        this.movementController.setBounds(moveBounds);
        this.attackSystem = attackSystem;
        this.skillSystem = skillSystem;
    }

    public static SimplePixelCharacter create() {
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
                () -> {
                    /*if (isDead()) return;
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
                    }*/
                }
            );

        // 技能系统：一个简单的火球技能
        SkillSystem.BasicSkillSystem skillSystem =
            new SkillSystem.BasicSkillSystem(skill -> {
                if ("SKILL_FIREBALL".equals(skill.id)) {
//                    trySkillHit(skill);
                }
            });
        skillSystem.addSkill(new SkillSystem.Skill(
            "SKILL_FIREBALL",
            "火球术",
            0.3f,   // 前摇
            0.2f,   // 后摇
            2.0f,   // 冷却
            30      // 基础伤害
        ));

        // 创建两个聚合角色：一个使用连续移动，一个使用栅格移动
        MovementController freeMove = new MovementController.FreeMovementController(playerAttr.getStats());
        Rectangle bounds = new Rectangle(80, 80, 1920 - 160, 1080 - 160);

        return new SimplePixelCharacter(
            "free-player",
            playerAttr,
            freeMove,
            meleeAttack,
            skillSystem,
            bounds
        );
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AttributeSystem getAttributes() {
        return attributeSystem;
    }

    @Override
    public MovementController getMovementController() {
        return movementController;
    }

    @Override
    public AttackSystem getAttackSystem() {
        return attackSystem;
    }

    @Override
    public SkillSystem getSkillSystem() {
        return skillSystem;
    }

    @Override
    public Vector2 getPosition() {
        // 直接暴露 movementController 的位置引用，避免额外分配
        sharedPosition.set(movementController.getPosition());
        return sharedPosition;
    }

    public void draw(SpriteBatch batch, Texture texture) {
        float size = 32f;
        // 连续移动玩家（绿色）
        Vector2 fp = this.getPosition();
        float freeX = Math.round(fp.x - size / 2f);
        float freeY = Math.round(fp.y - size / 2f);
        batch.setColor(0.3f, 1f, 0.3f, 1f);
        batch.draw(texture, freeX, freeY, size, size);
    }

    @Override
    public void update(float delta) {
        // 死亡后可选择停止更新移动 / 攻击 / 技能
        if (attributeSystem.isDead()) return;

        movementController.update(delta);
        attackSystem.update(delta);
        skillSystem.update(delta);
    }
}


