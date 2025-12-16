package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @ClassName:Attackable
 * @Author:TwT
 * @Date:2025/12/16 23:51
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

/**
 * 可攻击目标接口：所有能被攻击的实体都需实现此接口
 * 扩展点：新增可攻击目标（如召唤物、陷阱）只需实现此接口
 */
public interface Attackable {
    /**
     * 受到攻击
     * @param damage 伤害值
     * @param attacker 攻击者
     */
    void takeDamage(int damage, Character attacker);

    /**
     * 获取目标的像素坐标（供碰撞检测）
     */
    Vector2 getPixelPosition();

    /**
     * 获取目标的碰撞盒（像素级，适配精准判定）
     */
    AttackCollider getAttackCollider();

    /**
     * 是否可被攻击（如霸体、无敌时返回false）
     */
    boolean isAttackable();

    /**
     * 碰撞盒（适配像素级碰撞）
     * 
     * 设计说明：
     * - 用于精确的碰撞检测
     * - 支持相对于目标坐标的偏移
     * - 可以扩展为像素级精确碰撞（基于纹理数据）
     */
    class AttackCollider {
        /** 相对于目标坐标的偏移（像素） */
        public Vector2 offset = new Vector2();
        
        /** 碰撞盒宽度（像素） */
        public float width;
        
        /** 碰撞盒高度（像素） */
        public float height;
        
        /**
         * 获取碰撞盒在世界坐标系中的矩形
         * 
         * @param targetPos 目标位置
         * @return 碰撞盒矩形
         */
        public com.badlogic.gdx.math.Rectangle getWorldBounds(Vector2 targetPos) {
            return new com.badlogic.gdx.math.Rectangle(
                targetPos.x + offset.x,
                targetPos.y + offset.y,
                width,
                height
            );
        }
        
        /**
         * 像素级精准碰撞判定（适配不规则精灵）
         * 
         * @param thisPos 当前目标位置
         * @param other 另一个可攻击目标
         * @return true=发生碰撞
         * 
         * 注意：当前为简化实现，实际可以基于纹理数据做逐像素判定
         */
        public boolean checkPixelCollision(Vector2 thisPos, Attackable other) {
            // 简化实现：使用矩形碰撞
            Vector2 otherPos = other.getPixelPosition();
            AttackCollider otherCollider = other.getAttackCollider();
            
            com.badlogic.gdx.math.Rectangle thisRect = getWorldBounds(thisPos);
            com.badlogic.gdx.math.Rectangle otherRect = otherCollider.getWorldBounds(otherPos);
            
            return thisRect.overlaps(otherRect);
        }
    }
}
