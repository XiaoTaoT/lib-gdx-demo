package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * 7层图层系统接口
 * 用于管理不同深度的渲染层，从底层到顶层依次为：
 * 1. BACKGROUND - 背景层（最底层）
 * 2. MAP - 地图层
 * 3. DECORATION - 地面装饰层
 * 4. CHARACTER - 角色层
 * 5. EFFECT - 特效层
 * 6. UI - UI层
 * 7. TOP_UI - 顶层UI（最上层）
 */
public interface LayerSystem {
    
    /**
     * 图层枚举，定义7个渲染层
     */
    enum Layer {
        BACKGROUND(0),    // 背景层
        MAP(1),          // 地图层
        DECORATION(2),   // 地面装饰层
        CHARACTER(3),    // 角色层
        EFFECT(4),       // 特效层
        UI(5),           // UI层
        TOP_UI(6);       // 顶层UI
        
        private final int index;
        
        Layer(int index) {
            this.index = index;
        }
        
        public int getIndex() {
            return index;
        }
    }
    
    /**
     * 渲染所有图层
     * @param batch 用于渲染的 SpriteBatch
     * @param camera 相机
     */
    void renderAll(SpriteBatch batch, OrthographicCamera camera);
    
    /**
     * 渲染指定图层
     * @param batch 用于渲染的 SpriteBatch
     * @param camera 相机
     * @param layer 要渲染的图层
     */
    void renderLayer(SpriteBatch batch, OrthographicCamera camera, Layer layer);
    
    /**
     * 更新所有图层（用于动画等）
     * @param delta 帧时间间隔
     */
    void updateAll(float delta);
    
    /**
     * 更新指定图层
     * @param delta 帧时间间隔
     * @param layer 要更新的图层
     */
    void updateLayer(float delta, Layer layer);
    
    /**
     * 设置图层是否可见
     * @param layer 图层
     * @param visible 是否可见
     */
    void setLayerVisible(Layer layer, boolean visible);
    
    /**
     * 获取图层是否可见
     * @param layer 图层
     * @return 是否可见
     */
    boolean isLayerVisible(Layer layer);
    
    /**
     * 设置图层透明度
     * @param layer 图层
     * @param alpha 透明度（0.0-1.0）
     */
    void setLayerAlpha(Layer layer, float alpha);
    
    /**
     * 获取图层透明度
     * @param layer 图层
     * @return 透明度（0.0-1.0）
     */
    float getLayerAlpha(Layer layer);
    
    /**
     * 清理所有图层资源
     */
    void dispose();
}

