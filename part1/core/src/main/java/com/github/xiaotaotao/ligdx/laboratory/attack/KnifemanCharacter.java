package com.github.xiaotaotao.ligdx.laboratory.attack;

import com.badlogic.gdx.math.Vector2;

/**
 * 刀客人物实现
 * 
 * 设计说明：
 * - 实现 Character 接口，支持装备武器和执行攻击
 * - 拥有攻击冷却管理，避免高频攻击
 * - 支持硬直状态，攻击后短暂无法操作
 * 
 * 扩展：新增人物类型只需新增此类，实现 Character 接口
 */
public class KnifemanCharacter implements Character, Attackable {
    
    /** 当前装备的武器 */
    private Weapon equippedWeapon;
    
    /** 攻击冷却管理器（避免高频攻击） */
    private final AttackCooldownManager cooldownManager = new AttackCooldownManager();
    
    /** 人物当前位置（像素坐标） */
    private final Vector2 position = new Vector2();
    
    /** 人物当前 HP */
    private int hp = 100;
    
    /** 人物最大 HP */
    private int maxHp = 100;
    
    /** 人物攻击属性值 */
    private int attackAttr = 15;
    
    /** 硬直状态结束时间（毫秒时间戳） */
    private long stiffnessEndTime = 0;
    
    /** 是否处于无敌状态 */
    private boolean invincible = false;
    
    /**
     * 创建刀客角色
     * 
     * @param x 初始 X 坐标（像素）
     * @param y 初始 Y 坐标（像素）
     */
    public KnifemanCharacter(float x, float y) {
        // 初始装备刀
        this.equippedWeapon = new KnifeWeapon();
        this.position.set(x, y);
        
        // 注册到实体管理器
        EntityManager.getInstance().register(this);
    }

    @Override
    public void performNormalAttack(AttackStyle.AttackStyleType attackStyleType, Vector2 dir) {
        // 1. 检查是否装备武器
        if (equippedWeapon == null) return;
        // 2. 检查该武器是否支持此攻击方式
        AttackStyle attackStyle = equippedWeapon.getSupportedAttackStyles().get(attackStyleType);
        if (attackStyle == null) return;
        // 3. 检查冷却
        if (!cooldownManager.isReady(attackStyleType)) return;
        // 4. 执行攻击
        attackStyle.execute(this, equippedWeapon, dir);
        // 5. 记录冷却
        cooldownManager.startCooldown(attackStyleType, attackStyle.getCooldownMs());
    }

    @Override
    public void equipWeapon(Weapon weapon) {
        this.equippedWeapon = weapon;
    }

    @Override
    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    @Override
    public CharacterType getCharacterType() {
        return CharacterType.KNIFEMAN;
    }

    @Override
    public boolean isAttackable() {
        return !isDead() && !isInvincible();
    }

    @Override
    public Vector2 getPixelPosition() {
        // 返回人物的像素坐标（适配LibGDX）
        return this.position;
    }
    
    @Override
    public int getAttackAttr() {
        return attackAttr;
    }
    
    @Override
    public void enterStiffness(long stiffnessMs) {
        stiffnessEndTime = System.currentTimeMillis() + stiffnessMs;
    }
    
    @Override
    public boolean isInStiffness() {
        return System.currentTimeMillis() < stiffnessEndTime;
    }
    
    /**
     * 设置人物位置
     * 
     * @param x X 坐标（像素）
     * @param y Y 坐标（像素）
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
    
    /**
     * 获取当前 HP
     * 
     * @return 当前 HP
     */
    public int getHp() {
        return hp;
    }
    
    /**
     * 获取最大 HP
     * 
     * @return 最大 HP
     */
    public int getMaxHp() {
        return maxHp;
    }
    
    /**
     * 检查是否死亡
     * 
     * @return true=已死亡
     */
    public boolean isDead() {
        return hp <= 0;
    }
    
    /**
     * 检查是否处于无敌状态
     * 
     * @return true=无敌状态
     */
    public boolean isInvincible() {
        return invincible;
    }
    
    /**
     * 设置无敌状态
     * 
     * @param invincible true=无敌，false=可受伤
     */
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }
    
    // ========== Attackable 接口实现 ==========
    
    @Override
    public void takeDamage(int damage, Character attacker) {
        if (isInvincible() || isDead()) {
            return;
        }
        
        // 扣除 HP
        hp = Math.max(0, hp - damage);
        
        // 如果受到伤害，可以触发短暂无敌（避免连续受击）
        if (damage > 0) {
            setInvincible(true);
            // 0.2 秒后取消无敌
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    setInvincible(false);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    @Override
    public AttackCollider getAttackCollider() {
        // 简单的矩形碰撞盒（32x32 像素）
        AttackCollider collider = new AttackCollider();
        collider.offset.set(-16, -16); // 中心对齐
        collider.width = 32;
        collider.height = 32;
        return collider;
    }
    
    /**
     * 获取攻击冷却管理器（用于 UI 显示冷却时间）
     * 
     * @return 冷却管理器
     */
    public AttackCooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    /**
     * 清理资源（角色销毁时调用）
     */
    public void dispose() {
        EntityManager.getInstance().unregister(this);
    }
}
