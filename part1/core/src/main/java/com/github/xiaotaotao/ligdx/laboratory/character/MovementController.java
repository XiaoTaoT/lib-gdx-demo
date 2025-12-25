package com.github.xiaotaotao.ligdx.laboratory.character;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

/**
 * 移动控制器接口：
 * - 玩家：响应键盘 / 手柄输入
 * - 敌人：根据 AI 计算目标方向
 *
 * 像素 RPG 重点：
 * - 所有位移必须「格子对齐」（如 8/16 像素步长），避免子像素导致的糊边
 * - 可配置移动边界矩形，用于战斗场景边缘限制
 */
public interface MovementController {

    /**
     * 获取角色的当前世界坐标位置
     *
     * @return 位置向量（像素单位，左下角为原点）
     *
     * 注意：
     * - 返回的是 Vector2 引用，可以直接修改，但建议通过 setPosition 设置
     * - 对于 GridMovementController，位置会自动对齐到格子中心
     * - 对于 FreeMovementController，位置可以是任意浮点数
     */
    Vector2 getPosition();

    /**
     * 设置角色的初始位置（通常用于初始化或瞬移）
     *
     * @param x 世界坐标 X（像素）
     * @param y 世界坐标 Y（像素）
     *
     * 注意：
     * - 对于 GridMovementController，如果 gridAligned=true，会自动对齐到最近的格子中心
     * - 对于 FreeMovementController，位置可以是任意浮点数
     */
    void setPosition(float x, float y);

    /**
     * 设置移动输入方向（由外部每帧调用，例如根据键盘输入）
     *
     * @param x 水平方向（-1=左, 0=无, 1=右）
     * @param y 垂直方向（-1=下, 0=无, 1=上）
     *
     * 示例：
     * - 按 W 键：setInputDirection(0, 1)
     * - 按 D 键：setInputDirection(1, 0)
     * - 同时按 W+D：setInputDirection(1, 1)（会归一化为对角线方向）
     * - 松开所有键：setInputDirection(0, 0)（角色停止移动）
     *
     * 注意：
     * - 输入方向会被归一化，所以对角线移动速度不会更快
     * - 必须在每帧的 update 之前调用，否则角色不会移动
     */
    void setInputDirection(float x, float y);

    /**
     * 每帧更新移动逻辑（由外部每帧调用，通常在 render 方法中）
     *
     * @param deltaSeconds 上一帧到当前帧的时间间隔（秒），例如 0.016（60 FPS）
     *
     * 工作流程：
     * 1. 检查是否有输入方向和移动速度
     * 2. 根据输入方向计算面朝方向（用于动画）
     * 3. 根据 moveSpeed 和 deltaSeconds 计算移动距离
     * 4. 更新位置（GridMovementController 会做格子对齐，FreeMovementController 直接移动）
     * 5. 应用边界限制
     *
     * 注意：
     * - 必须在 setInputDirection 之后调用
     * - 建议在每帧的 render 方法中调用，保证移动平滑
     */
    void update(float deltaSeconds);

    /**
     * 设置移动边界矩形（限制角色不能超出这个区域）
     *
     * @param bounds 边界矩形（像素单位，null = 无边界限制）
     *
     * 用途：
     * - 战斗场景：限制角色在战斗区域内移动
     * - 地图边界：防止角色走出地图
     * - 房间限制：限制角色在特定房间内
     *
     * 注意：
     * - 边界检查在 update 方法中自动应用
     * - 如果角色试图移动到边界外，会被限制在边界内
     */
    void setBounds(Rectangle bounds);

    /**
     * 启用/禁用网格对齐
     *
     * @param enabled true=强制对齐到格子中心，false=允许任意位置
     *
     * 注意：
     * - 对于 GridMovementController，启用时会立即将当前位置对齐到最近的格子
     * - 对于 FreeMovementController，此方法无效果（保持接口兼容）
     * - 一般像素 RPG 建议永远开启，避免像素模糊
     */
    void setGridAligned(boolean enabled);

    /**
     * 获取当前面朝方向（用于选择行走动画帧）
     *
     * @return 当前面朝方向（DOWN/LEFT/RIGHT/UP）
     *
     * 用途：
     * - 选择行走动画：根据 facing 选择对应的动画帧（例如：Facing.UP 使用向上走的动画）
     * - 攻击判定：根据 facing 决定攻击范围的方向
     * - UI 显示：显示角色朝向箭头等
     *
     * 注意：
     * - 面朝方向在 update 方法中根据输入方向自动计算
     * - 优先判断水平方向，如果水平分量更大，则面向左/右；否则面向上下
     */
    Facing getFacing();

    default void move(float x, float y){};

    /**
     * 角色面朝方向枚举：
     * - 用于选择行走动画的朝向（例如：向下走用 down 动画帧）
     * - 在攻击/技能判定时，也可以根据 facing 决定攻击范围的方向
     */
    enum Facing {
        /** 向下（屏幕下方） */
        DOWN,
        /** 向左（屏幕左侧） */
        LEFT,
        /** 向右（屏幕右侧） */
        RIGHT,
        /** 向上（屏幕上方） */
        UP
    }

    /**
     * 简单的栅格移动实现（真正“按格子走路”的感觉）：
     * - 使用 AttributeSystem.Stats.moveSpeed 作为基础速度
     * - 内部以 gridSize 为步长累积距离：distance = moveSpeed * delta
     *   当累计距离超过一格时，走 N 格（N * gridSize），多余的部分留到下一帧
     * - 适合战棋 / 严格 tile-based 的移动演示
     */
    class GridMovementController implements MovementController {

        /** 角色的当前世界坐标位置（像素单位，左下角为原点） */
        private final Vector2 position = new Vector2();

        /** 输入方向向量（由外部通过 setInputDirection 设置，例如 WASD 按键映射） */
        private final Vector2 inputDir = new Vector2();

        /** 角色属性系统的引用（用于读取 moveSpeed：移动速度，单位：像素/秒） */
        private final AttributeSystem.Stats stats;

        /** 像素网格大小（例如 8 或 16），角色移动时会以这个值为步长"跳格子" */
        private final float gridSize;

        /** 移动边界矩形（限制角色不能超出这个区域，例如战斗场景的边界） */
        private Rectangle bounds;

        /** 是否启用网格对齐（true=强制对齐到格子中心，false=允许任意位置） */
        private boolean gridAligned = true;

        /** 当前面朝方向（根据输入方向自动计算，用于选择动画帧） */
        private Facing facing = Facing.DOWN;

        /**
         * 累积的"未满一格"的距离余量（用于精确的格子移动计算）
         *
         * 工作原理：
         * - 假设 gridSize = 16，moveSpeed = 120 像素/秒，delta = 0.016 秒
         * - 本帧移动距离 = 120 * 0.016 = 1.92 像素（不足一格）
         * - 此时 steps = 0，distanceRemainder = 1.92
         * - 下一帧再累积：distance = 1.92 + 1.92 = 3.84（仍不足一格）
         * - 继续累积，直到 distance >= 16 时，steps = 1，角色移动一格
         *
         * 这样设计的好处：
         * - 避免低速时"完全不动"的问题（小距离会被累积）
         * - 保持严格的格子对齐（不会出现停在半格上的情况）
         */
        private float distanceRemainder = 0f;

        /**
         * @param stats    属性引用，用于读取 moveSpeed（像素/秒）
         * @param gridSize 像素网格大小（例如 8 / 16）
         */
        public GridMovementController(AttributeSystem.Stats stats, float gridSize) {
            this.stats = stats;
            this.gridSize = gridSize;
        }

        @Override
        public Vector2 getPosition() {
            return position;
        }

        /**
         * 设置角色的初始位置（通常用于初始化或瞬移）
         *
         * @param x 世界坐标 X（像素）
         * @param y 世界坐标 Y（像素）
         *
         * 注意：如果 gridAligned = true，会自动将坐标对齐到最近的格子中心
         */
        @Override
        public void setPosition(float x, float y) {
            position.set(x, y);
            if (gridAligned) {
                snapToGrid();
            }
        }

        @Override
        public void setInputDirection(float x, float y) {
            inputDir.set(x, y);
        }

        /**
         * 每帧更新移动逻辑（由外部每帧调用，通常在 render 方法中）
         *
         * @param deltaSeconds 上一帧到当前帧的时间间隔（秒），例如 0.016（60 FPS）
         *
         * 核心算法：格子累积移动
         * 1. 计算本帧应该移动的距离：moveSpeed * deltaSeconds
         * 2. 加上上一帧的余量：distance = moveSpeed * deltaSeconds + distanceRemainder
         * 3. 计算能走多少格：steps = distance / gridSize（向下取整）
         * 4. 保存余量：distanceRemainder = distance - steps * gridSize
         * 5. 如果 steps > 0，移动 steps 格；否则不移动，但余量会累积到下一帧
         *
         * 这样设计的好处：
         * - 低速时不会"完全不动"（小距离会累积）
         * - 高速时不会"瞬移"（每帧最多移动 N 格，N 由速度决定）
         * - 严格对齐格子，适合战棋/走格子玩法
         */
        @Override
        public void update(float deltaSeconds) {
            // 如果没有输入方向或移动速度为 0，直接返回（不移动）
            if (inputDir.isZero() || stats.moveSpeed <= 0f) {
                return;
            }

            // 计算面朝方向（用于选择动画帧）
            // 优先判断水平方向，如果水平分量更大，则面向左/右；否则面向上下
            if (Math.abs(inputDir.x) > Math.abs(inputDir.y)) {
                facing = inputDir.x > 0 ? Facing.RIGHT : Facing.LEFT;
            } else {
                facing = inputDir.y > 0 ? Facing.UP : Facing.DOWN;
            }

            // 归一化输入方向（将任意向量转换为单位向量，保证对角线移动速度不会更快）
            // 例如：(1, 1) 归一化后变成 (0.707, 0.707)，长度 = 1
            Vector2 dir = new Vector2(inputDir).nor();

            // 计算本帧累积的移动距离（加上上一帧的余量）
            float distance = stats.moveSpeed * deltaSeconds + distanceRemainder;

            // 计算需要跨过多少个格子（向下取整）
            int steps = (int) (distance / gridSize);

            // 保存余量（不足一格的剩余距离，留到下一帧继续累积）
            distanceRemainder = distance - steps * gridSize;

            // 如果本帧累积距离不足一格，不移动（但余量会累积，直到 >= 一格）
            if (steps == 0) {
                return;
            }

            // 计算实际移动的像素距离（必须是 gridSize 的整数倍）
            float stepDistance = steps * gridSize;
            float dx = dir.x * stepDistance;
            float dy = dir.y * stepDistance;

            // 计算新位置
            float newX = position.x + dx;
            float newY = position.y + dy;

            // 边界限制：确保角色不会超出 bounds 矩形范围
            if (bounds != null) {
                newX = MathUtils.clamp(newX, bounds.x, bounds.x + bounds.width);
                newY = MathUtils.clamp(newY, bounds.y, bounds.y + bounds.height);
            }

            // 更新位置
            position.set(newX, newY);
        }

        /**
         * 将当前位置对齐到最近的格子中心
         *
         * 算法：
         * - 将坐标除以 gridSize，四舍五入，再乘以 gridSize
         * - 例如：gridSize=16, position.x=25.3 → 25.3/16=1.58 → round=2 → 2*16=32
         *
         * 用途：
         * - 初始化时确保角色在格子中心
         * - 瞬移后自动对齐
         */
        private void snapToGrid() {
            position.x = Math.round(position.x / gridSize) * gridSize;
            position.y = Math.round(position.y / gridSize) * gridSize;
        }

        /**
         * 设置移动边界矩形（限制角色不能超出这个区域）
         *
         * @param bounds 边界矩形（null = 无边界限制）
         *
         * 用途：
         * - 战斗场景：限制角色在战斗区域内移动
         * - 地图边界：防止角色走出地图
         */
        @Override
        public void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }

        /**
         * 启用/禁用网格对齐
         *
         * @param enabled true=强制对齐到格子中心，false=允许任意位置
         *
         * 注意：启用时会立即将当前位置对齐到最近的格子
         */
        @Override
        public void setGridAligned(boolean enabled) {
            this.gridAligned = enabled;
            if (enabled) {
                snapToGrid();
            }
        }

        /**
         * 获取当前面朝方向（用于选择行走动画帧）
         *
         * @return 当前面朝方向（DOWN/LEFT/RIGHT/UP）
         */
        @Override
        public Facing getFacing() {
            return facing;
        }
    }

    /**
     * 连续坐标移动实现：
     * - 使用 AttributeSystem.Stats.moveSpeed 作为基础速度（像素/秒）
     * - 每帧直接按速度 * deltaSeconds 平滑移动
     * - 渲染时建议对坐标取整，以避免像素模糊
     */
    class FreeMovementController implements MovementController {


        /** 角色的当前世界坐标位置（像素单位，左下角为原点，可以是任意浮点数） */
        private final Vector2 position = new Vector2();

        /** 输入方向向量（由外部通过 setInputDirection 设置，例如 WASD 按键映射） */
        private final Vector2 inputDir = new Vector2();

        /** 角色属性系统的引用（用于读取 moveSpeed：移动速度，单位：像素/秒） */
        private final AttributeSystem.Stats stats;

        /** 移动边界矩形（限制角色不能超出这个区域，例如战斗场景的边界） */
        private Rectangle bounds;

        /** 当前面朝方向（根据输入方向自动计算，用于选择动画帧） */
        private Facing facing = Facing.DOWN;

        /**
         * 创建连续移动控制器
         *
         * @param stats 角色属性系统引用（用于读取 moveSpeed）
         *
         * 与 GridMovementController 的区别：
         * - 不使用 gridSize，位置可以是任意浮点数
         * - 每帧直接按速度移动，不做格子对齐
         * - 渲染时建议对坐标取整，避免像素模糊
         */
        public FreeMovementController(AttributeSystem.Stats stats) {
            this.stats = stats;
        }

        /**
         * 获取当前世界坐标位置
         *
         * @return 位置向量（可以直接修改，但建议通过 setPosition 设置）
         */
        @Override
        public Vector2 getPosition() {
            return position;
        }

        /**
         * 设置角色的初始位置（通常用于初始化或瞬移）
         *
         * @param x 世界坐标 X（像素，可以是任意浮点数）
         * @param y 世界坐标 Y（像素，可以是任意浮点数）
         *
         * 注意：不会自动对齐到格子，位置可以是任意值
         */
        @Override
        public void setPosition(float x, float y) {
            position.set(x, y);
        }

        /**
         * 设置移动输入方向（由外部每帧调用，例如根据键盘输入）
         *
         * @param x 水平方向（-1=左, 0=无, 1=右）
         * @param y 垂直方向（-1=下, 0=无, 1=上）
         *
         * 与 GridMovementController 的 setInputDirection 用法完全相同
         */
        @Override
        public void setInputDirection(float x, float y) {
            inputDir.set(x, y);
        }

        /**
         * 每帧更新移动逻辑（由外部每帧调用，通常在 render 方法中）
         *
         * @param deltaSeconds 上一帧到当前帧的时间间隔（秒），例如 0.016（60 FPS）
         *
         * 核心算法：连续移动
         * 1. 归一化输入方向（保证对角线移动速度不会更快）
         * 2. 计算本帧移动距离：distance = moveSpeed * deltaSeconds
         * 3. 直接更新位置：newPos = oldPos + dir * distance
         * 4. 应用边界限制
         *
         * 与 GridMovementController 的区别：
         * - 不做格子对齐，位置可以是任意浮点数
         * - 移动更平滑，适合动作游戏/ARPG
         * - 渲染时建议对坐标取整，避免像素模糊
         */
        @Override
        public void update(float deltaSeconds) {
            // 如果没有输入方向或移动速度为 0，直接返回（不移动）
            if (inputDir.isZero() || stats.moveSpeed <= 0f) {
                return;
            }

            // 计算面朝方向（用于选择动画帧）
            // 优先判断水平方向，如果水平分量更大，则面向左/右；否则面向上下
            if (Math.abs(inputDir.x) > Math.abs(inputDir.y)) {
                facing = inputDir.x > 0 ? Facing.RIGHT : Facing.LEFT;
            } else {
                facing = inputDir.y > 0 ? Facing.UP : Facing.DOWN;
            }

            // 归一化输入方向（将任意向量转换为单位向量，保证对角线移动速度不会更快）
            Vector2 dir = new Vector2(inputDir).nor();

            // 计算本帧移动距离（像素单位）
            float distance = stats.moveSpeed * deltaSeconds;

            // 计算位移分量
            float dx = dir.x * distance;
            float dy = dir.y * distance;

            // 计算新位置
            float newX = position.x + dx;
            float newY = position.y + dy;

            // 边界限制：确保角色不会超出 bounds 矩形范围
            if (bounds != null) {
                newX = MathUtils.clamp(newX, bounds.x, bounds.x + bounds.width);
                newY = MathUtils.clamp(newY, bounds.y, bounds.y + bounds.height);
            }

            // 更新位置（可以是任意浮点数，不做格子对齐）
            position.set(newX, newY);
        }

        /**
         * 设置移动边界矩形（限制角色不能超出这个区域）
         *
         * @param bounds 边界矩形（null = 无边界限制）
         *
         * 用途：
         * - 战斗场景：限制角色在战斗区域内移动
         * - 地图边界：防止角色走出地图
         */
        @Override
        public void setBounds(Rectangle bounds) {
            this.bounds = bounds;
        }

        /**
         * 启用/禁用网格对齐（对于连续移动控制器，此方法无效果）
         *
         * @param enabled 此参数会被忽略，因为连续移动不使用格子对齐
         *
         * 注意：此方法只是为了保持接口兼容性，实际不会做任何操作
         */
        @Override
        public void setGridAligned(boolean enabled) {
            // 对于连续移动控制器，该设置无效果，保持接口兼容
        }

        /**
         * 获取当前面朝方向（用于选择行走动画帧）
         *
         * @return 当前面朝方向（DOWN/LEFT/RIGHT/UP）
         */
        @Override
        public Facing getFacing() {
            return facing;
        }

        @Override
        public void move(float x, float y) {
            this.setInputDirection(x,y);
        }
    }
}


