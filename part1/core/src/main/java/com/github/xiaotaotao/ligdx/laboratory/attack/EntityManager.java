package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体管理器：管理场景中所有可攻击目标
 * 
 * 设计说明：
 * - 单例模式，全局唯一实例
 * - 负责注册/注销可攻击目标
 * - 供攻击范围策略查询所有目标
 * 
 * 扩展点：
 * - 可以按类型分类管理（如玩家、敌人、中立单位）
 * - 可以添加空间分区优化（如四叉树）以提高查询效率
 */
public class EntityManager {
    
    /** 单例实例 */
    private static final EntityManager instance = new EntityManager();
    
    /** 所有可攻击目标列表 */
    private final Array<Attackable> allAttackables = new Array<>();
    
    /**
     * 获取单例实例
     * 
     * @return EntityManager 单例
     */
    public static EntityManager getInstance() {
        return instance;
    }
    
    /**
     * 注册一个可攻击目标（通常在目标创建时调用）
     * 
     * @param attackable 可攻击目标
     */
    public void register(Attackable attackable) {
        if (attackable != null && !allAttackables.contains(attackable, true)) {
            allAttackables.add(attackable);
        }
    }
    
    /**
     * 注销一个可攻击目标（通常在目标销毁时调用）
     * 
     * @param attackable 可攻击目标
     */
    public void unregister(Attackable attackable) {
        allAttackables.removeValue(attackable, true);
    }
    
    /**
     * 获取场景中所有可攻击目标
     * 
     * @return 所有可攻击目标列表（返回副本，避免外部修改内部列表）
     */
    public List<Attackable> getAllAttackables() {
        List<Attackable> result = new ArrayList<>();
        for (Attackable attackable : allAttackables) {
            if (attackable != null && attackable.isAttackable()) {
                result.add(attackable);
            }
        }
        return result;
    }
    
    /**
     * 清空所有注册的目标（通常在场景切换时调用）
     */
    public void clear() {
        allAttackables.clear();
    }
    
    /**
     * 获取当前注册的目标数量（用于调试）
     * 
     * @return 目标数量
     */
    public int getCount() {
        return allAttackables.size;
    }
}

