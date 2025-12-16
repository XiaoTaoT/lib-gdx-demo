package com.github.xiaotaotao.ligdx.laboratory.character;

import com.badlogic.gdx.math.MathUtils;

/**
 * 属性系统接口：
 * - 管理 HP/MP/攻击/防御/攻速/移速 等基础属性
 * - 提供伤害/治疗等基础计算
 * - 通过监听器暴露属性变化（供 UI 飘字等使用）
 */
public interface AttributeSystem {

    /** 当前属性快照（线程不安全，仅供游戏主线程读取） */
    Stats getStats();

    /** 设置监听器，用于 UI、飘血数字等 */
    void setListener(Listener listener);

    /** 造成一次伤害，内部会根据攻击方/受击方属性计算真实伤害并扣血 */
    DamageResult applyDamage(DamageRequest request);

    /** 直接恢复生命（已做上限约束） */
    void heal(int amount);

    /** 是否已经死亡（HP <= 0） */
    boolean isDead();

    /**
     * 属性变化 / 伤害事件监听器。
     * 实现里应保证：在属性变化后同步回调（同一帧），方便 UI 做飘血、数值更新。
     */
    interface Listener {
        /** 任意基础属性变化回调（例如 HP/攻击力等） */
        void onStatsChanged(Stats newStats);

        /** 一次伤害结算完成（可在此触发飘字 UI：-20 / +10 等） */
        void onDamageResolved(DamageResult result);
    }

    /**
     * HP/MP/攻击/防御/攻速/移速 等基础属性结构体。
     * 简化起见，这里全部用 int/float，可根据需要扩展。
     */
    final class Stats {
        public int maxHp;
        public int hp;
        public int maxMp;
        public int mp;

        public int attack;
        public int defense;

        /** 攻击间隔（秒）= 1 / 攻速，可按需要自己定义 */
        public float attackSpeed;

        /** 每秒基础移动距离（像素/秒），在像素移动中会被量化到 8/16 像素 */
        public float moveSpeed;

        public Stats cpy() {
            Stats s = new Stats();
            s.maxHp = maxHp;
            s.hp = hp;
            s.maxMp = maxMp;
            s.mp = mp;
            s.attack = attack;
            s.defense = defense;
            s.attackSpeed = attackSpeed;
            s.moveSpeed = moveSpeed;
            return s;
        }
    }

    /**
     * 伤害请求：包含攻击方属性、防御方当前属性、基础伤害、是否暴击等信息。
     * 这里保留一定弹性，方便扩展技能/元素/暴击等。
     */
    final class DamageRequest {
        public Stats attackerStats;
        public Stats defenderStats;
        /** 技能或普攻的基础伤害（未计算防御） */
        public int baseDamage;
        /** 是否必定命中（忽略闪避等） */
        public boolean guaranteedHit = true;
        /** 是否暴击（可在外层先行判定暴击概率） */
        public boolean critical;
        /** 暴击伤害倍数（例如 1.5f/2.0f） */
        public float criticalMul = 1.5f;
    }

    /**
     * 伤害结果：实际伤害值 / 是否暴击 / 扣血后剩余 HP。
     * UI 飘字、战斗日志可以直接使用这个结构。
     */
    final class DamageResult {
        public int finalDamage;
        public boolean critical;
        public int hpBefore;
        public int hpAfter;

        @Override
        public String toString() {
            return "DamageResult{damage=" + finalDamage +
                    ", critical=" + critical +
                    ", hpBefore=" + hpBefore +
                    ", hpAfter=" + hpAfter + '}';
        }
    }

    /**
     * 一个简单的属性系统实现：
     * - 线性伤害公式：damage = max(1, baseDamage + atk - def)
     * - 防御减伤等可后续扩展（百分比减伤、护盾等）
     */
    class BasicAttributeSystem implements AttributeSystem {
        private final Stats stats = new Stats();
        private Listener listener;

        public BasicAttributeSystem(int maxHp, int maxMp, int attack, int defense,
                                    float attackSpeed, float moveSpeed) {
            stats.maxHp = maxHp;
            stats.hp = maxHp;
            stats.maxMp = maxMp;
            stats.mp = maxMp;
            stats.attack = attack;
            stats.defense = defense;
            stats.attackSpeed = attackSpeed;
            stats.moveSpeed = moveSpeed;
        }

        @Override
        public Stats getStats() {
            return stats;
        }

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
        }

        @Override
        public DamageResult applyDamage(DamageRequest request) {
            DamageResult result = new DamageResult();
            result.hpBefore = stats.hp;

            // 简单的线性伤害：基础伤害 + 攻方攻击 - 守方防御，下限 1 点
            int damage = request.baseDamage;
            if (request.attackerStats != null) {
                damage += request.attackerStats.attack;
            }
            if (request.defenderStats != null) {
                damage -= request.defenderStats.defense;
            }
            damage = Math.max(1, damage);

            if (request.critical) {
                damage = Math.round(damage * request.criticalMul);
            }

            stats.hp = MathUtils.clamp(stats.hp - damage, 0, stats.maxHp);

            result.finalDamage = damage;
            result.critical = request.critical;
            result.hpAfter = stats.hp;

            if (listener != null) {
                listener.onStatsChanged(stats.cpy());
                listener.onDamageResolved(result);
            }
            return result;
        }

        @Override
        public void heal(int amount) {
            if (amount <= 0 || isDead()) return;
            stats.hp = MathUtils.clamp(stats.hp + amount, 0, stats.maxHp);
            if (listener != null) {
                listener.onStatsChanged(stats.cpy());
            }
        }

        @Override
        public boolean isDead() {
            return stats.hp <= 0;
        }
    }
}


