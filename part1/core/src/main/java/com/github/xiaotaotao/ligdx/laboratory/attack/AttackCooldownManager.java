package com.github.xiaotaotao.ligdx.laboratory.attack;

import java.util.HashMap;
import java.util.Map;

/**
 * 攻击冷却管理器：管理不同攻击方式的冷却时间
 * 
 * 设计说明：
 * - 每个角色实例拥有一个冷却管理器
 * - 支持多种攻击方式独立冷却（如斩和挥可以分别冷却）
 * - 使用时间戳记录冷却结束时间
 * 
 * 工作原理：
 * - 攻击时记录：冷却结束时间 = 当前时间 + 冷却时长
 * - 下次攻击前检查：当前时间 >= 冷却结束时间 才能攻击
 */
public class AttackCooldownManager {
    
    /** 存储每种攻击方式的冷却结束时间（毫秒时间戳） */
    private final Map<AttackStyle.AttackStyleType, Long> cooldownEndTimes = new HashMap<>();
    
    /**
     * 检查指定攻击方式是否冷却完成（可以攻击）
     * 
     * @param attackStyleType 攻击方式类型
     * @return true=可以攻击，false=仍在冷却中
     */
    public boolean isReady(AttackStyle.AttackStyleType attackStyleType) {
        Long endTime = cooldownEndTimes.get(attackStyleType);
        if (endTime == null) {
            // 从未使用过此攻击方式，可以直接使用
            return true;
        }
        // 检查当前时间是否已超过冷却结束时间
        return System.currentTimeMillis() >= endTime;
    }
    
    /**
     * 开始指定攻击方式的冷却
     * 
     * @param attackStyleType 攻击方式类型
     * @param cooldownMs 冷却时长（毫秒）
     */
    public void startCooldown(AttackStyle.AttackStyleType attackStyleType, long cooldownMs) {
        long endTime = System.currentTimeMillis() + cooldownMs;
        cooldownEndTimes.put(attackStyleType, endTime);
    }
    
    /**
     * 获取指定攻击方式的剩余冷却时间（毫秒）
     * 
     * @param attackStyleType 攻击方式类型
     * @return 剩余冷却时间（毫秒），0 表示冷却完成
     */
    public long getRemainingCooldown(AttackStyle.AttackStyleType attackStyleType) {
        Long endTime = cooldownEndTimes.get(attackStyleType);
        if (endTime == null) {
            return 0;
        }
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * 清除所有冷却（通常在角色死亡/重置时调用）
     */
    public void clearAll() {
        cooldownEndTimes.clear();
    }
}

