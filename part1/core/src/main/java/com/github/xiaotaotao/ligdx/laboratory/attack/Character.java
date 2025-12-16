package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @ClassName:Character
 * @Author:TwT
 * @Date:2025/12/16 23:50
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

/**
 * 人物接口：定义人物类型和普攻核心逻辑
 * 扩展点：新增人物类型只需实现此接口
 */
public interface Character {
    /**
     * 执行普攻（核心入口：人物类型×普攻→攻击方式）
     * @param attackStyleType 选择的攻击方式（如斩/挥）
     * @param dir 攻击朝向
     */
    void performNormalAttack(AttackStyle.AttackStyleType attackStyleType, Vector2 dir);

    /**
     * 装备武器（人物可切换武器，影响可用攻击方式）
     */
    void equipWeapon(Weapon weapon);

    /**
     * 获取当前装备的武器
     */
    Weapon getEquippedWeapon();

    /**
     * 获取人物类型（决定默认可用的攻击方式）
     */
    CharacterType getCharacterType();

    /**
     * 判定该人物是否可被攻击（通用接口，供攻击范围检测）
     */
    boolean isAttackable();

    /**
     * 获取人物的像素坐标（供攻击范围检测）
     */
    Vector2 getPixelPosition();
    
    /**
     * 获取人物的攻击属性值（用于计算伤害）
     * 
     * @return 攻击属性值
     */
    int getAttackAttr();
    
    /**
     * 进入硬直状态（攻击后无法操作的时间）
     * 
     * @param stiffnessMs 硬直时长（毫秒）
     */
    void enterStiffness(long stiffnessMs);
    
    /**
     * 检查是否处于硬直状态
     * 
     * @return true=处于硬直状态，false=可以操作
     */
    boolean isInStiffness();

    // 人物类型枚举（扩展时新增）
    enum CharacterType {
        SWORDSMAN, // 剑士
        KNIFEMAN,  // 刀客
        SPEARMAN,  // 枪兵
        HAMMERMAN  // 锤兵
    }
}
