package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @ClassName:Weapon
 * @Author:TwT
 * @Date:2025/12/16 23:50
 * @Version:1.0
 **/

import java.util.Map;

/**
 * 武器接口：定义武器的基础属性和可用攻击方式
 * 扩展点：新增武器只需实现此接口
 */
public interface Weapon {
    /**
     * 获取武器支持的攻击方式（武器×攻击方式的映射）
     * 例：刀支持{SLASH: 斩逻辑, SWING: 挥逻辑}，剑支持{SLASH: 斩, STAB: 刺}
     */
    Map<AttackStyle.AttackStyleType, AttackStyle> getSupportedAttackStyles();

    /**
     * 获取武器的基础攻击范围像素单位（如刀=32像素，枪=64像素）
     */
    float getBasePixelUnit();

    /**
     * 获取武器的基础攻击力
     */
    int getBaseAttack();

    /**
     * 武器类型标识
     */
    WeaponType getWeaponType();

    // 武器类型枚举（扩展时新增）
    enum WeaponType {
        SWORD,  // 剑
        KNIFE,  // 刀
        SPEAR,  // 枪
        HAMMER  // 锤
    }
}
