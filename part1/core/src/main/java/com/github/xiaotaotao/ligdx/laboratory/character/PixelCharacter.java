package com.github.xiaotaotao.ligdx.laboratory.character;

import com.badlogic.gdx.math.Vector2;

/**
 * 像素 RPG 角色接口，聚合属性 / 移动 / 攻击 / 技能四大系统。
 * 该接口只关心“逻辑层”，渲染层（精灵、动画、UI）通过监听或轮询这些系统的数据自行完成。
 */
public interface PixelCharacter {

    /** 角色唯一 ID（用于管理/日志/网络同步） */
    String getId();

    /** 核心属性系统（血量、攻击、防御等） */
    AttributeSystem getAttributes();

    /** 移动控制器（玩家/AI 共用） */
    MovementController getMovementController();

    /** 攻击系统（普攻/连击） */
    AttackSystem getAttackSystem();

    /** 技能系统（主动/被动技能池） */
    SkillSystem getSkillSystem();

    /** 角色在世界中的当前位置（像素坐标，基于像素网格） */
    Vector2 getPosition();

    /** 每帧更新（由外部 Game/Screen 驱动） */
    void update(float delta);
}


