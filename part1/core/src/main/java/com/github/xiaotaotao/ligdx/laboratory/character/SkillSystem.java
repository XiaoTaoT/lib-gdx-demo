package com.github.xiaotaotao.ligdx.laboratory.character;

import com.badlogic.gdx.utils.Array;

/**
 * 技能系统接口：
 * - 管理技能池（普攻以外的主动 / 被动技能）
 * - 处理施法前摇 / 后摇 / 冷却时间
 * - 技能命中后通常会委托 AttributeSystem / 伤害系统计算数值
 *
 * 像素 RPG 建议：
 * - 技能特效使用粒子 / 像素精灵（由渲染层实现）
 * - 该系统只负责“什么时候技能生效、什么时候可以再次释放”这类时序逻辑
 */
public interface SkillSystem {

    /** 当前持有的技能列表（只读） */
    Array<Skill> getSkills();

    /** 根据 ID 获取技能 */
    Skill getSkill(String id);

    /** 尝试施放技能（按键触发时调用），返回是否成功进入施法状态 */
    boolean cast(String id);

    /** 每帧更新所有技能的冷却与施法状态 */
    void update(float deltaSeconds);

    /**
     * 技能数据与运行时状态。
     * 渲染层可以根据 {@link #state} 和 {@link #elapsedInState} 驱动技能动画 / 特效。
     */
    final class Skill {
        public final String id;
        public final String name;
        public final float castTime;   // 前摇（秒）
        public final float backSwing;  // 后摇（秒）
        public final float cooldown;   // 冷却（秒）
        public final int baseDamage;   // 技能基础伤害

        public State state = State.READY;
        public float elapsedInState = 0f;
        public float cdRemaining = 0f;

        public Skill(String id, String name, float castTime, float backSwing,
                     float cooldown, int baseDamage) {
            this.id = id;
            this.name = name;
            this.castTime = castTime;
            this.backSwing = backSwing;
            this.cooldown = cooldown;
            this.baseDamage = baseDamage;
        }
    }

    enum State {
        /** 冷却完毕，可以施放 */
        READY,
        /** 前摇中（此阶段不可移动或受限移动，通常不产生伤害） */
        CASTING,
        /** 技能已释放，处于后摇中（可以在进入该状态时发射弹道 / 生成粒子） */
        BACK_SWING,
        /** 冷却中 */
        COOLDOWN
    }

    /**
     * 技能事件监听，用于：
     * - 在技能真正生效时（通常从 CASTING -> BACK_SWING 的瞬间）生成弹道 / 粒子特效
     * - 与 AttributeSystem 协作应用伤害
     */
    interface Listener {
        /** 技能进入生效瞬间（通常用来生成弹道或执行范围伤害） */
        void onSkillImpact(Skill skill);
    }

    /**
     * 一个简单的技能系统实现：
     * - 单线程更新，所有技能独立管理自己的状态机
     * - 不考虑打断 / 位移施法等复杂逻辑
     */
    class BasicSkillSystem implements SkillSystem {

        private final Array<Skill> skills = new Array<>();
        private final Listener listener;

        public BasicSkillSystem(Listener listener) {
            this.listener = listener;
        }

        public void addSkill(Skill skill) {
            skills.add(skill);
        }

        @Override
        public Array<Skill> getSkills() {
            return skills;
        }

        @Override
        public Skill getSkill(String id) {
            for (Skill s : skills) {
                if (s.id.equals(id)) return s;
            }
            return null;
        }

        @Override
        public boolean cast(String id) {
            Skill s = getSkill(id);
            if (s == null) return false;
            if (s.state != State.READY) return false;

            s.state = State.CASTING;
            s.elapsedInState = 0f;
            return true;
        }

        @Override
        public void update(float deltaSeconds) {
            for (Skill s : skills) {
                switch (s.state) {
                    case READY:
                        // 无事可做
                        break;
                    case CASTING:
                        s.elapsedInState += deltaSeconds;
                        if (s.elapsedInState >= s.castTime) {
                            // 进入生效瞬间：切到后摇状态并触发 impact 回调
                            s.state = State.BACK_SWING;
                            s.elapsedInState = 0f;
                            if (listener != null) {
                                listener.onSkillImpact(s);
                            }
                        }
                        break;
                    case BACK_SWING:
                        s.elapsedInState += deltaSeconds;
                        if (s.elapsedInState >= s.backSwing) {
                            // 进入冷却
                            s.state = State.COOLDOWN;
                            s.cdRemaining = s.cooldown;
                            s.elapsedInState = 0f;
                        }
                        break;
                    case COOLDOWN:
                        s.cdRemaining -= deltaSeconds;
                        if (s.cdRemaining <= 0f) {
                            s.cdRemaining = 0f;
                            s.state = State.READY;
                            s.elapsedInState = 0f;
                        }
                        break;
                }
            }
        }
    }
}


