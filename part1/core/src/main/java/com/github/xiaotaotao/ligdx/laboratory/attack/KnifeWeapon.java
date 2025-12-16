package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @Desciption:
 * @ClassName:KnifeWeapon
 * @Author:TwT
 * @Date:2025/12/16 23:53
 * @Version:1.0
 **/

import java.util.HashMap;
import java.util.Map;

/**
 * 刀武器实现
 * 扩展：新增武器只需新增此类
 */
public class KnifeWeapon implements Weapon {
    // 绑定刀支持的攻击方式（斩+挥）
    private final Map<AttackStyle.AttackStyleType, AttackStyle> supportedStyles;
    // 基础属性
    private static final float BASE_PIXEL_UNIT = 32; // 基础范围32像素
    private static final int BASE_ATTACK = 10;

    public KnifeWeapon() {
        supportedStyles = new HashMap<>();
        // 斩：矩形范围（正前方1/3）
        supportedStyles.put(AttackStyle.AttackStyleType.SLASH, new SlashAttackStyle());
        // 挥：扇形范围（120°）
        supportedStyles.put(AttackStyle.AttackStyleType.SWING, new SwingAttackStyle());
    }

    @Override
    public Map<AttackStyle.AttackStyleType, AttackStyle> getSupportedAttackStyles() {
        return supportedStyles;
    }

    @Override
    public float getBasePixelUnit() {
        return BASE_PIXEL_UNIT;
    }

    @Override
    public int getBaseAttack() {
        return BASE_ATTACK;
    }

    @Override
    public WeaponType getWeaponType() {
        return WeaponType.KNIFE;
    }
}
