package com.github.xiaotaotao.ligdx.laboratory.character;

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

    @Override
    public void update(float delta) {
        // 死亡后可选择停止更新移动 / 攻击 / 技能
        if (attributeSystem.isDead()) return;

        movementController.update(delta);
        attackSystem.update(delta);
        skillSystem.update(delta);
    }
}


