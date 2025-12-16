package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * 斩击攻击方式实现（矩形范围，正前方）
 * 
 * 设计说明：
 * - 使用矩形范围策略（正前方一定距离和宽度）
 * - 适合刀/剑的斩击动作
 * - 范围：正前方，宽度为攻击范围的 1/3
 * 
 * 扩展：新增攻击方式只需新增此类
 */
public class SlashAttackStyle implements AttackStyle {
    
    /** 绑定矩形范围策略（正前方，宽度为范围的 1/3） */
    private final AttackRangeStrategy rangeStrategy = new RectangleAttackRange();
    
    /** 冷却时间和硬直时间（毫秒） */
    private static final long COOLDOWN_MS = 400;
    private static final long STIFFNESS_MS = 150;
    
    @Override
    public void execute(Character attacker, Weapon weapon, Vector2 dir) {
        // 1. 获取攻击范围内的所有目标
        List<Attackable> targets = rangeStrategy.detectTargets(
            attacker.getPixelPosition(),
            dir,
            weapon.getBasePixelUnit()
        );
        
        // 2. 计算伤害（武器基础攻击 + 人物攻击属性）
        int damage = weapon.getBaseAttack() + attacker.getAttackAttr();
        
        // 3. 对范围内所有目标造成伤害
        for (Attackable target : targets) {
            if (target.isAttackable()) {
                target.takeDamage(damage, attacker);
            }
        }
        
        // 4. 触发攻击者硬直（攻击后无法操作的时间）
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
        return AttackStyleType.SLASH;
    }
}

