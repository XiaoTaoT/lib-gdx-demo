package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Disposable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 7层图层系统的实现类
 * 提供分层渲染管理功能
 */
public class LayerSystemImpl implements LayerSystem, Disposable {
    
    // 图层渲染器列表（每个图层可以有多个渲染器）
    private final ArrayList<LayerRenderer>[] layers;
    
    // 图层可见性
    private final Map<Layer, Boolean> layerVisibility;
    
    // 图层透明度
    private final Map<Layer, Float> layerAlpha;
    
    @SuppressWarnings("unchecked")
    public LayerSystemImpl() {
        // 初始化7个图层
        layers = new ArrayList[7];
        for (int i = 0; i < 7; i++) {
            layers[i] = new ArrayList<>();
        }
        
        // 初始化图层可见性和透明度
        layerVisibility = new HashMap<>();
        layerAlpha = new HashMap<>();
        for (Layer layer : Layer.values()) {
            layerVisibility.put(layer, true);
            layerAlpha.put(layer, 1.0f);
        }
    }
    
    @Override
    public void renderAll(SpriteBatch batch, OrthographicCamera camera) {
        // 按顺序渲染所有可见图层
        for (Layer layer : Layer.values()) {
            if (isLayerVisible(layer)) {
                renderLayer(batch, camera, layer);
            }
        }
    }
    
    @Override
    public void renderLayer(SpriteBatch batch, OrthographicCamera camera, Layer layer) {
        ArrayList<LayerRenderer> renderers = layers[layer.getIndex()];
        float alpha = getLayerAlpha(layer);
        
        // 保存原始颜色
        float oldAlpha = batch.getColor().a;
        
        // 设置图层透明度
        if (alpha < 1.0f) {
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, alpha);
        }
        
        // 渲染该图层的所有渲染器
        for (LayerRenderer renderer : renderers) {
            if (renderer.isEnabled()) {
                renderer.render(batch, camera);
            }
        }
        
        // 恢复原始透明度
        if (alpha < 1.0f) {
            batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b, oldAlpha);
        }
    }
    
    @Override
    public void updateAll(float delta) {
        // 更新所有图层
        for (Layer layer : Layer.values()) {
            updateLayer(delta, layer);
        }
    }
    
    @Override
    public void updateLayer(float delta, Layer layer) {
        ArrayList<LayerRenderer> renderers = layers[layer.getIndex()];
        
        // 更新该图层的所有渲染器
        for (LayerRenderer renderer : renderers) {
            if (renderer.isEnabled()) {
                renderer.update(delta);
            }
        }
    }
    
    @Override
    public void setLayerVisible(Layer layer, boolean visible) {
        layerVisibility.put(layer, visible);
    }
    
    @Override
    public boolean isLayerVisible(Layer layer) {
        return layerVisibility.getOrDefault(layer, true);
    }
    
    @Override
    public void setLayerAlpha(Layer layer, float alpha) {
        layerAlpha.put(layer, Math.max(0.0f, Math.min(1.0f, alpha)));
    }
    
    @Override
    public float getLayerAlpha(Layer layer) {
        return layerAlpha.getOrDefault(layer, 1.0f);
    }
    
    /**
     * 添加渲染器到指定图层
     * @param layer 图层
     * @param renderer 渲染器
     */
    public void addRenderer(Layer layer, LayerRenderer renderer) {
        layers[layer.getIndex()].add(renderer);
    }
    
    /**
     * 从指定图层移除渲染器
     * @param layer 图层
     * @param renderer 渲染器
     */
    public void removeRenderer(Layer layer, LayerRenderer renderer) {
        layers[layer.getIndex()].remove(renderer);
    }
    
    /**
     * 清空指定图层的所有渲染器
     * @param layer 图层
     */
    public void clearLayer(Layer layer) {
        layers[layer.getIndex()].clear();
    }
    
    /**
     * 获取指定图层的渲染器数量
     * @param layer 图层
     * @return 渲染器数量
     */
    public int getRendererCount(Layer layer) {
        return layers[layer.getIndex()].size();
    }
    
    @Override
    public void dispose() {
        // 清理所有渲染器
        for (ArrayList<LayerRenderer> layer : layers) {
            for (LayerRenderer renderer : layer) {
                if (renderer instanceof Disposable) {
                    ((Disposable) renderer).dispose();
                }
            }
            layer.clear();
        }
    }
    
    /**
     * 图层渲染器接口
     * 每个图层可以包含多个渲染器
     */
    public interface LayerRenderer {
        /**
         * 渲染方法
         * @param batch SpriteBatch
         * @param camera 相机
         */
        void render(SpriteBatch batch, OrthographicCamera camera);
        
        /**
         * 更新方法（用于动画等）
         * @param delta 帧时间间隔
         */
        void update(float delta);
        
        /**
         * 是否启用
         * @return 是否启用
         */
        boolean isEnabled();
        
        /**
         * 设置是否启用
         * @param enabled 是否启用
         */
        void setEnabled(boolean enabled);
    }
}

