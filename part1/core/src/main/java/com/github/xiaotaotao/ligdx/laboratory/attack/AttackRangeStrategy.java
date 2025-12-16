package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @ClassName:AttackRangeStrategy
 * @Author:TwT
 * @Date:2025/12/16 23:49
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

import java.util.List;

/**
 * 攻击范围策略接口：定义不同形状攻击范围的判定逻辑
 * 扩展点：新增形状（如圆形、多边形）只需实现此接口
 */
public interface AttackRangeStrategy {
    /**
     * 判定范围内的所有目标
     * @param attackerPos 攻击者像素坐标（中心）
     * @param attackerDir 攻击者朝向（单位向量，如正前方Vector2(1,0)）
     * @param pixelUnit 像素单位（适配不同武器的基础范围，如刀=32像素，剑=48像素）
     * @return 范围内的所有可攻击目标
     */
    List<Attackable> detectTargets(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit);

    /**
     * （调试/渲染用）获取攻击范围的像素顶点（用于绘制范围轮廓，适配像素UI）
     * @param attackerPos 攻击者坐标
     * @param attackerDir 攻击者朝向
     * @param pixelUnit 像素单位
     * @return 范围顶点的像素坐标列表（如扇形返回3个顶点，矩形返回4个）
     */
    List<Vector2> getRangeVertices(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit);

    /**
     * 范围类型标识（用于配置/序列化）
     */
    RangeType getRangeType();

    // 范围形状枚举（扩展时新增枚举值即可）
    enum RangeType {
        SECTOR,    // 扇形（如刀挥120°）
        RECTANGLE, // 矩形（如刀斩正前方1/3）
        CIRCLE,    // 圆形（如锤子砸地）
        POLYGON    // 多边形（如长枪突刺）
    }
}
