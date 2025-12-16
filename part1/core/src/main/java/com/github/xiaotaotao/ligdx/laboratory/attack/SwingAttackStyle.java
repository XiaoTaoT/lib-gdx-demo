package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @Desciption:
 * @ClassName:SwingAttackStyle
 * @Author:TwT
 * @Date:2025/12/16 23:53
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * 刀挥攻击方式实现
 * 扩展：新增攻击方式只需新增此类
 */
public class SwingAttackStyle implements AttackStyle {
    // 绑定扇形范围策略（120°）
    private final AttackRangeStrategy rangeStrategy = new SectorAttackRange(120);
    // 冷却时间和硬直时间
    private static final long COOLDOWN_MS = 500;
    private static final long STIFFNESS_MS = 200;

    @Override
    public void execute(Character attacker, Weapon weapon, Vector2 dir) {
        // 1. 获取攻击范围
        List<Attackable> targets = rangeStrategy.detectTargets(
            attacker.getPixelPosition(),
            dir,
            weapon.getBasePixelUnit()
        );
        // 2. 计算伤害（人物属性+武器基础攻击）
        int damage = weapon.getBaseAttack() + attacker.getAttackAttr();
        // 3. 触发目标受击
        for (Attackable target : targets) {
            target.takeDamage(damage, attacker);
        }
        // 4. 触发人物硬直
        attacker.enterStiffness(STIFFNESS_MS);
    }

    @Override
    public long getCooldownMs() {
        return COOLDOWN_MS;
    }

    @Override
    public long getStiffnessMs() {
        return STIFFNESS_MS;
    }

    @Override
    public AttackRangeStrategy getRangeStrategy() {
        return rangeStrategy;
    }

    @Override
    public AttackStyleType getAttackStyleType() {
        return AttackStyleType.SWING;
    }
}
