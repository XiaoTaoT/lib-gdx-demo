package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @ClassName:AttackStyle
 * @Author:TwT
 * @Date:2025/12/16 23:50
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

/**
 * 攻击方式接口：定义普攻的具体行为（如斩、挥、刺）
 * 扩展点：新增攻击方式只需实现此接口
 */
public interface AttackStyle {
    /**
     * 执行攻击逻辑
     * @param attacker 攻击者（人物）
     * @param weapon 所用武器
     * @param dir 攻击朝向
     */
    void execute(Character attacker, Weapon weapon, Vector2 dir);

    /**
     * 获取该攻击方式的冷却时间（毫秒）
     */
    long getCooldownMs();

    /**
     * 获取该攻击方式的硬直时间（毫秒，攻击后无法操作的时间）
     */
    long getStiffnessMs();

    /**
     * 获取该攻击方式对应的范围策略（武器×攻击方式决定）
     */
    AttackRangeStrategy getRangeStrategy();

    /**
     * 攻击方式标识（用于配置/人物-攻击方式绑定）
     */
    AttackStyleType getAttackStyleType();

    // 攻击方式枚举（扩展时新增）
    enum AttackStyleType {
        SLASH,  // 斩（刀/剑）
        SWING,  // 挥（刀/斧）
        STAB,   // 刺（枪/剑）
        SMASH   // 砸（锤/棍）
    }
}
