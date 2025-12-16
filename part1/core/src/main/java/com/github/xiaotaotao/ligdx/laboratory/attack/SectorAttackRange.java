package com.github.xiaotaotao.ligdx.laboratory.attack;

/**
 * @Desciption:
 * @ClassName:SectorAttackRange
 * @Author:TwT
 * @Date:2025/12/16 23:53
 * @Version:1.0
 **/

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * 扇形攻击范围实现（如刀挥120°）
 * 扩展：新增形状只需新增此类，无需修改其他代码
 */
public class SectorAttackRange implements AttackRangeStrategy {
    // 扇形角度（可配置，如刀挥=120°，剑挥=90°）
    private final float angleDeg;
    // 扇形半径（由武器pixelUnit决定）

    public SectorAttackRange(float angleDeg) {
        this.angleDeg = angleDeg;
    }

    @Override
    public List<Attackable> detectTargets(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit) {
        List<Attackable> targets = new ArrayList<>();
        // 1. 获取场景内所有可攻击目标
        List<Attackable> allAttackables = EntityManager.getInstance().getAllAttackables();
        // 2. 遍历判定是否在扇形范围内（像素级）
        for (Attackable target : allAttackables) {
            if (isInSector(attackerPos, attackerDir, target.getPixelPosition(), angleDeg, pixelUnit)) {
                targets.add(target);
            }
        }
        return targets;
    }

    // 扇形判定核心逻辑（基于向量夹角+距离）
    private boolean isInSector(Vector2 center, Vector2 dir, Vector2 target, float angleDeg, float radius) {
        // 1. 计算目标到中心的距离（像素）
        float distance = center.dst(target);
        if (distance > radius) return false;
        // 2. 计算目标方向与攻击方向的夹角
        Vector2 targetDir = target.cpy().sub(center).nor();
        float dot = dir.dot(targetDir);
        float angleRad = (float) Math.acos(Math.max(-1, Math.min(1, dot)));
        float angle = (float) Math.toDegrees(angleRad);
        // 3. 判定是否在扇形角度内（120°则判定angle <= 60°）
        return angle <= angleDeg / 2;
    }

    @Override
    public List<Vector2> getRangeVertices(Vector2 attackerPos, Vector2 attackerDir, float pixelUnit) {
        // 计算扇形的三个顶点（中心、左边界、右边界），用于像素渲染
        List<Vector2> vertices = new ArrayList<>();
        vertices.add(attackerPos.cpy()); // 中心
        // 左边界
        Vector2 leftDir = attackerDir.cpy().rotate(angleDeg / 2f);
        vertices.add(attackerPos.cpy().add(leftDir.scl(pixelUnit)));
        // 右边界
        Vector2 rightDir = attackerDir.cpy().rotate(-angleDeg / 2f);
        vertices.add(attackerPos.cpy().add(rightDir.scl(pixelUnit)));
        return vertices;
    }

    @Override
    public RangeType getRangeType() {
        return RangeType.SECTOR;
    }
}
