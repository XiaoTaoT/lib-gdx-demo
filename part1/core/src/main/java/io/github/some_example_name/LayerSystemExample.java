package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;

/**
 * 图层系统使用示例
 * 展示如何在游戏中使用7层图层系统
 */
public class LayerSystemExample {
    
    private LayerSystemImpl layerSystem;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    
    public LayerSystemExample() {
        layerSystem = new LayerSystemImpl();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
    }
    
    /**
     * 初始化图层系统示例
     */
    public void initialize() {
        // 示例：添加背景层渲染器
        layerSystem.addRenderer(LayerSystem.Layer.BACKGROUND, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;
            
            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 渲染背景
                batch.setColor(0.5f, 0.7f, 1.0f, 1.0f); // 天蓝色
                batch.draw(new Texture(Gdx.files.internal("ui/uiskin.png")), 0, 0, 800, 480);
                batch.setColor(1, 1, 1, 1);
            }
            
            @Override
            public void update(float delta) {
                // 背景层通常不需要更新
            }
            
            @Override
            public boolean isEnabled() {
                return enabled;
            }
            
            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });
        
        // 示例：添加角色层渲染器
        layerSystem.addRenderer(LayerSystem.Layer.CHARACTER, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;
            @SuppressWarnings("unused")
            private float time = 0;
            
            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 渲染角色（示例代码）
                // 实际使用时，这里应该绘制角色精灵
            }
            
            @Override
            public void update(float delta) {
                time += delta;
                // 更新角色动画等
            }
            
            @Override
            public boolean isEnabled() {
                return enabled;
            }
            
            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });
        
        // 示例：添加UI层渲染器
        layerSystem.addRenderer(LayerSystem.Layer.UI, new LayerSystemImpl.LayerRenderer() {
            private boolean enabled = true;
            
            @Override
            public void render(SpriteBatch batch, OrthographicCamera camera) {
                // 渲染UI元素（血条、按钮等）
            }
            
            @Override
            public void update(float delta) {
                // 更新UI状态
            }
            
            @Override
            public boolean isEnabled() {
                return enabled;
            }
            
            @Override
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });
    }
    
    /**
     * 渲染方法示例
     */
    public void render(float delta) {
        // 更新所有图层
        layerSystem.updateAll(delta);
        
        // 开始渲染
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        // 渲染所有图层（按顺序从底层到顶层）
        layerSystem.renderAll(batch, camera);
        
        batch.end();
    }
    
    /**
     * 清理资源
     */
    public void dispose() {
        layerSystem.dispose();
        batch.dispose();
    }
    
    /**
     * 使用示例：控制图层可见性
     */
    public void exampleLayerControl() {
        // 隐藏特效层
        layerSystem.setLayerVisible(LayerSystem.Layer.EFFECT, false);
        
        // 设置UI层半透明
        layerSystem.setLayerAlpha(LayerSystem.Layer.UI, 0.5f);
        
        // 只渲染角色层
        batch.begin();
        layerSystem.renderLayer(batch, camera, LayerSystem.Layer.CHARACTER);
        batch.end();
    }
}

