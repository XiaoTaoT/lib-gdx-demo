package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * 矩形攻击范围实现（正前方矩形区域）
 * 
 * 设计说明：
 * - 范围形状：矩形，正前方延伸
 * - 宽度：攻击范围的 1/3（可配置）
 * - 长度：由武器的 pixelUnit 决定
 * 
 * 用途：
 * - 斩击攻击（刀/剑的正前方斩击）
 * - 突刺攻击（枪的正前方突刺）
 */
public class RectangleAttackRange implements AttackRangeStrategy {
    
    /** 矩形宽度相对于长度的比例（例如 0.33 表示宽度为长度的 1/3） */
    private static final float WIDTH_RATIO = 0.33f;
    
    @Override
    public List<Attackable> detectTargets(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit) {
        List<Attackable> targets = new ArrayList<>();
        
        // 1. 获取场景内所有可攻击目标
        List<Attackable> allAttackables = EntityManager.getInstance().getAllAttackables();
        
        // 2. 计算矩形范围参数
        float length = pixelUnit; // 矩形长度（正前方延伸距离）
        float width = length * WIDTH_RATIO; // 矩形宽度
        
        // 3. 计算矩形的四个顶点（相对于攻击者位置）
        // 矩形中心在攻击者正前方 length/2 的位置
        Vector2 center = attackerPos.cpy().add(attackerDir.cpy().scl(length / 2f));
        
        // 计算垂直于攻击方向的向量（用于计算宽度）
        Vector2 perpendicular = new Vector2(-attackerDir.y, attackerDir.x).nor();
        
        // 矩形的四个顶点
        Vector2 halfWidth = perpendicular.cpy().scl(width / 2f);
        Vector2 halfLength = attackerDir.cpy().scl(length / 2f);
        
        Vector2 topLeft = center.cpy().add(halfWidth).sub(halfLength);
        Vector2 topRight = center.cpy().sub(halfWidth).sub(halfLength);
        Vector2 bottomLeft = center.cpy().add(halfWidth).add(halfLength);
        Vector2 bottomRight = center.cpy().sub(halfWidth).add(halfLength);
        
        // 4. 遍历所有目标，检查是否在矩形范围内
        for (Attackable target : allAttackables) {
            Vector2 targetPos = target.getPixelPosition();
            
            // 使用点是否在矩形内的判定（简化版：检查点是否在四个顶点围成的矩形内）
            if (isPointInRectangle(targetPos, topLeft, topRight, bottomLeft, bottomRight)) {
                targets.add(target);
            }
        }
        
        return targets;
    }
    
    /**
     * 判断点是否在矩形内（使用向量叉积判定）
     * 
     * @param point 待判定点
     * @param topLeft 矩形左上角
     * @param topRight 矩形右上角
     * @param bottomLeft 矩形左下角
     * @param bottomRight 矩形右下角
     * @return true=点在矩形内
     */
    private boolean isPointInRectangle(Vector2 point, Vector2 topLeft, Vector2 topRight, 
                                       Vector2 bottomLeft, Vector2 bottomRight) {
        // 简化判定：使用 LibGDX 的 Rectangle 类
        // 计算矩形的边界
        float minX = Math.min(Math.min(topLeft.x, topRight.x), Math.min(bottomLeft.x, bottomRight.x));
        float maxX = Math.max(Math.max(topLeft.x, topRight.x), Math.max(bottomLeft.x, bottomRight.x));
        float minY = Math.min(Math.min(topLeft.y, topRight.y), Math.min(bottomLeft.y, bottomRight.y));
        float maxY = Math.max(Math.max(topLeft.y, topRight.y), Math.max(bottomLeft.y, bottomRight.y));
        
        return point.x >= minX && point.x <= maxX && point.y >= minY && point.y <= maxY;
    }
    
    @Override
    public List<Vector2> getRangeVertices(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit) {
        List<Vector2> vertices = new ArrayList<>();
        
        // 计算矩形参数
        float length = pixelUnit;
        float width = length * WIDTH_RATIO;
        
        // 矩形中心
        Vector2 center = attackerPos.cpy().add(attackerDir.cpy().scl(length / 2f));
        Vector2 perpendicular = new Vector2(-attackerDir.y, attackerDir.x).nor();
        
        Vector2 halfWidth = perpendicular.cpy().scl(width / 2f);
        Vector2 halfLength = attackerDir.cpy().scl(length / 2f);
        
        // 返回矩形的四个顶点（按顺序：左上、右上、右下、左下）
        vertices.add(center.cpy().add(halfWidth).sub(halfLength)); // 左上
        vertices.add(center.cpy().sub(halfWidth).sub(halfLength)); // 右上
        vertices.add(center.cpy().sub(halfWidth).add(halfLength)); // 右下
        vertices.add(center.cpy().add(halfWidth).add(halfLength)); // 左下
        
        return vertices;
    }
    
    @Override
    public RangeType getRangeType() {
        return RangeType.RECTANGLE;
    }
}

