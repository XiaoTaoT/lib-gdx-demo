package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * 碰撞检测系统：处理角色之间的碰撞检测和响应
 * 
 * 设计说明：
 * - 使用矩形碰撞盒进行碰撞检测
 * - 支持碰撞响应（推开或阻止移动）
 * - 像素级精确碰撞（基于碰撞盒）
 * 
 * 工作原理：
 * 1. 检测两个角色的碰撞盒是否重叠
 * 2. 如果重叠，计算推开向量
 * 3. 应用推开向量，使角色分离
 */
public class CollisionSystem {
    
    /** 碰撞盒大小（像素），所有角色使用相同的碰撞盒大小 */
    private static final float COLLIDER_SIZE = 32f;
    
    /** 碰撞盒偏移（相对于角色中心） */
    private static final float COLLIDER_OFFSET = -16f;
    
    /**
     * 检查两个角色是否发生碰撞
     * 
     * @param pos1 角色1的位置
     * @param pos2 角色2的位置
     * @return true=发生碰撞
     */
    public static boolean checkCollision(Vector2 pos1, Vector2 pos2) {
        Rectangle rect1 = getColliderRect(pos1);
        Rectangle rect2 = getColliderRect(pos2);
        return rect1.overlaps(rect2);
    }
    
    /**
     * 计算碰撞响应向量（将角色1从角色2推开）
     * 
     * @param pos1 角色1的位置
     * @param pos2 角色2的位置
     * @return 推开向量（角色1应该移动的方向和距离）
     */
    public static Vector2 calculatePushVector(Vector2 pos1, Vector2 pos2) {
        Vector2 push = new Vector2();
        
        // 计算两个角色中心之间的距离
        float dx = pos2.x - pos1.x;
        float dy = pos2.y - pos1.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance == 0) {
            // 如果完全重叠，随机推开方向
            push.set(1, 0);
        } else {
            // 归一化方向（从角色2指向角色1）
            push.set(-dx / distance, -dy / distance);
        }
        
        // 计算需要推开的距离（重叠深度）
        Rectangle rect1 = getColliderRect(pos1);
        Rectangle rect2 = getColliderRect(pos2);
        
        // 计算重叠区域
        float overlapX = Math.min(rect1.x + rect1.width, rect2.x + rect2.width) - 
                        Math.max(rect1.x, rect2.x);
        float overlapY = Math.min(rect1.y + rect1.height, rect2.y + rect2.height) - 
                        Math.max(rect1.y, rect2.y);
        
        // 选择较小的重叠方向（优先推开重叠较少的方向）
        float pushDistance;
        if (overlapX < overlapY) {
            pushDistance = overlapX;
            push.y = 0; // 只推开 X 方向
        } else {
            pushDistance = overlapY;
            push.x = 0; // 只推开 Y 方向
        }
        
        // 应用推开距离
        push.scl(pushDistance + 1f); // +1 确保完全分离
        
        return push;
    }
    
    /**
     * 解决碰撞（将角色1从角色2推开）
     * 
     * @param pos1 角色1的位置（会被修改）
     * @param pos2 角色2的位置
     * @return true=发生了碰撞并已解决
     */
    public static boolean resolveCollision(Vector2 pos1, Vector2 pos2) {
        if (!checkCollision(pos1, pos2)) {
            return false;
        }
        
        Vector2 push = calculatePushVector(pos1, pos2);
        pos1.add(push);
        
        return true;
    }
    
    /**
     * 检查移动后是否会碰撞，如果会碰撞则调整移动距离
     * 
     * @param currentPos 当前位置
     * @param targetPos 目标位置（会被修改，如果碰撞则调整为不碰撞的位置）
     * @param otherPos 其他角色的位置
     * @return true=发生了碰撞，目标位置已被调整
     */
    public static boolean checkAndAdjustMovement(Vector2 currentPos, Vector2 targetPos, Vector2 otherPos) {
        // 先检查目标位置是否会碰撞
        if (!checkCollision(targetPos, otherPos)) {
            return false;
        }
        
        // 如果会碰撞，计算推开向量并调整目标位置
        Vector2 push = calculatePushVector(targetPos, otherPos);
        targetPos.add(push);
        
        // 如果调整后仍然碰撞，则不允许移动（保持在当前位置）
        if (checkCollision(targetPos, otherPos)) {
            targetPos.set(currentPos);
            return true;
        }
        
        return true;
    }
    
    /**
     * 获取角色碰撞盒的矩形
     * 
     * @param position 角色位置（中心点）
     * @return 碰撞盒矩形
     */
    private static Rectangle getColliderRect(Vector2 position) {
        return new Rectangle(
            position.x + COLLIDER_OFFSET,
            position.y + COLLIDER_OFFSET,
            COLLIDER_SIZE,
            COLLIDER_SIZE
        );
    }
    
    /**
     * 检查角色移动时是否会与场景中所有其他角色碰撞
     * 
     * @param currentPos 当前位置
     * @param targetPos 目标位置（会被修改，如果碰撞则调整为不碰撞的位置）
     * @param exclude 要排除的角色（通常是移动者自己）
     * @return true=发生了碰撞，目标位置已被调整
     */
    public static boolean checkMovementAgainstAll(Vector2 currentPos, Vector2 targetPos, Attackable exclude) {
        List<Attackable> allAttackables = EntityManager.getInstance().getAllAttackables();
        boolean collided = false;
        
        for (Attackable other : allAttackables) {
            // 跳过自己和不可攻击的目标
            if (other == exclude || !other.isAttackable()) {
                continue;
            }
            
            Vector2 otherPos = other.getPixelPosition();
            if (checkAndAdjustMovement(currentPos, targetPos, otherPos)) {
                collided = true;
            }
        }
        
        return collided;
    }
}

