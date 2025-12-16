package com.github.xiaotaotao.ligdx.laboratory.character;

/**
 * 攻击系统接口：
 * - 负责普攻 / 连击的时序与冷却
 * - 不直接做渲染，只给出“当前攻击帧是否需要判定”的信号
 *
 * 像素 RPG 约定：
 * - 普攻动画拆分为若干帧（frameCount），攻击判定只在指定帧生效（例如第 3 帧）
 * - 外层渲染层根据当前动画帧调用 {@link #shouldHitOnCurrentFrame()} 来决定是否检测碰撞
 */
public interface AttackSystem {

    /** 请求一次普攻（例如玩家按下攻击键时调用） */
    void requestAttack();

    /** 每帧更新攻击时序、冷却与当前攻击帧索引 */
    void update(float deltaSeconds);

    /** 当前是否处于攻击动画中 */
    boolean isAttacking();

    /** 当前攻击动画帧索引（0 ~ frameCount-1），供渲染层选择贴图 */
    int getCurrentFrameIndex();

    /** 在当前帧是否应该执行一次命中判定（“打击帧”） */
    boolean shouldHitOnCurrentFrame();

    /** 重置攻击状态（如被打断时） */
    void reset();

    /**
     * 一个简易的近战普攻实现：
     * - 单段普攻动画，共 frameCount 帧
     * - 指定 hitFrameIndex 为打击帧，只在该帧返回 shouldHitOnCurrentFrame = true
     * - attackInterval = 每次普攻总时长（秒）
     * - 使用 attackSystemListener 通知命中事件（供产生伤害 / 播放音效）
     */
    class BasicMeleeAttackSystem implements AttackSystem {

        public interface Listener {
            /** 当 shouldHitOnCurrentFrame() 第一次为 true 时回调一次，用于真正执行伤害判定 */
            void onHitWindow();
        }

        private final int frameCount;
        private final int hitFrameIndex;
        private final float attackInterval;
        private final Listener listener;

        private float elapsed;      // 当前攻击已经经过的时间
        private boolean attacking;
        private boolean hitEmitted; // 本次攻击是否已经触发过命中窗口

        public BasicMeleeAttackSystem(int frameCount,
                                      int hitFrameIndex,
                                      float attackInterval,
                                      Listener listener) {
            this.frameCount = frameCount;
            this.hitFrameIndex = hitFrameIndex;
            this.attackInterval = attackInterval;
            this.listener = listener;
        }

        @Override
        public void requestAttack() {
            if (attacking) {
                // 简单实现：攻击过程中忽略新的请求（可扩展为队列 / 连击系统）
                return;
            }
            attacking = true;
            elapsed = 0f;
            hitEmitted = false;
        }

        @Override
        public void update(float deltaSeconds) {
            if (!attacking) return;
            elapsed += deltaSeconds;
            if (elapsed >= attackInterval) {
                // 攻击结束
                attacking = false;
                elapsed = 0f;
                hitEmitted = false;
            } else if (!hitEmitted && shouldHitOnCurrentFrame() && listener != null) {
                hitEmitted = true;
                listener.onHitWindow();
            }
        }

        @Override
        public boolean isAttacking() {
            return attacking;
        }

        @Override
        public int getCurrentFrameIndex() {
            if (!attacking || attackInterval <= 0f || frameCount <= 0) return 0;
            float t = elapsed / attackInterval;
            int index = (int) (t * frameCount);
            if (index >= frameCount) index = frameCount - 1;
            return index;
        }

        @Override
        public boolean shouldHitOnCurrentFrame() {
            return isAttacking() && getCurrentFrameIndex() == hitFrameIndex;
        }

        @Override
        public void reset() {
            attacking = false;
            elapsed = 0f;
            hitEmitted = false;
        }
    }
}


