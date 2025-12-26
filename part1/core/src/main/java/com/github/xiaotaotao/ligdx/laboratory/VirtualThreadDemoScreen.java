package com.github.xiaotaotao.ligdx.laboratory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 使用Java虚拟线程管理1000个对象进行无规则运动的演示
 * 
 * @Description: 虚拟线程演示 - 1000个对象无规则运动
 * @ClassName: VirtualThreadDemoScreen
 * @Author: TwT
 * @Date: 2025/12/23
 * @Version: 1.0
 */
public class VirtualThreadDemoScreen implements Screen {
    
    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;
    private static final int OBJECT_COUNT = 1000;
    
    private SpriteBatch batch;
    private Texture objectTexture;
    
    // 存储所有移动对象
    private final List<MovingObject> objects = new ArrayList<>();
    
    // 渲染数据缓存（线程安全，由虚拟线程准备，主线程读取）
    // 使用AtomicReference实现无锁读取，提升性能
    private final AtomicReference<List<RenderData>> renderDataCache = new AtomicReference<>(new ArrayList<>());
    
    // 控制虚拟线程运行
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread updateThread;
    private Thread renderDataPrepareThread;
    
    /**
     * 渲染数据类 - 存储需要渲染的对象位置信息
     * 由虚拟线程准备，主线程读取
     */
    private static class RenderData {
        final float x;
        final float y;
        
        RenderData(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    /**
     * 移动对象类 - 每个对象有自己的位置、速度等属性
     */
    private static class MovingObject {
        private float x;
        private float y;
        private float vx;  // 水平速度
        private float vy;  // 垂直速度
        private float speed;  // 当前速度大小
        private float directionChangeTimer;  // 方向改变计时器
        private float directionChangeInterval;  // 方向改变间隔（秒）
        
        public MovingObject(float startX, float startY) {
            this.x = startX;
            this.y = startY;
            this.speed = MathUtils.random(50f, 200f);  // 随机速度 50-200 像素/秒
            this.directionChangeInterval = MathUtils.random(0.5f, 3.0f);  // 随机改变方向间隔
            this.directionChangeTimer = 0f;
            // 随机初始方向
            float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed;
        }
        
        /**
         * 更新对象位置和运动状态
         * @param delta 时间间隔（秒）
         */
        public void update(float delta) {
            // 更新方向改变计时器
            directionChangeTimer += delta;
            
            // 随机改变方向
            if (directionChangeTimer >= directionChangeInterval) {
                // 随机改变速度大小
                speed = MathUtils.random(50f, 200f);
                
                // 随机改变方向（添加一些随机性，不完全随机）
                float angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians;
                vx = MathUtils.cos(angle) * speed;
                vy = MathUtils.sin(angle) * speed;
                
                // 重置计时器并设置新的间隔
                directionChangeTimer = 0f;
                directionChangeInterval = MathUtils.random(0.5f, 3.0f);
            }
            
            // 更新位置
            x += vx * delta;
            y += vy * delta;
            
            // 边界反弹
            if (x < 0) {
                x = 0;
                vx = -vx;
            } else if (x > VIRTUAL_WIDTH - 10) {
                x = VIRTUAL_WIDTH - 10;
                vx = -vx;
            }
            
            if (y < 0) {
                y = 0;
                vy = -vy;
            } else if (y > VIRTUAL_HEIGHT - 10) {
                y = VIRTUAL_HEIGHT - 10;
                vy = -vy;
            }
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
    }
    
    @Override
    public void show() {
        batch = new SpriteBatch();
        
        // 创建简单的纹理（10x10像素的彩色方块）
        Pixmap pixmap = new Pixmap(10, 10, Pixmap.Format.RGBA8888);
        pixmap.setColor(MathUtils.random(0.5f, 1f), MathUtils.random(0.5f, 1f), 
                       MathUtils.random(0.5f, 1f), 1f);
        pixmap.fill();
        objectTexture = new Texture(pixmap);
        pixmap.dispose();
        
        // 初始化1000个对象，随机分布在屏幕上
        for (int i = 0; i < OBJECT_COUNT; i++) {
            float startX = MathUtils.random(0f, VIRTUAL_WIDTH - 10);
            float startY = MathUtils.random(0f, VIRTUAL_HEIGHT - 10);
            objects.add(new MovingObject(startX, startY));
        }
        
        // 启动虚拟线程来更新所有对象
        startUpdateThread();
        
        // 启动虚拟线程来准备渲染数据
        startRenderDataPrepareThread();
    }
    
    /**
     * 使用虚拟线程准备渲染数据（收集对象位置信息）
     * 这样可以减少主线程的工作量，提升渲染效率
     */
    private void startRenderDataPrepareThread() {
        running.set(true);
        
        // 创建虚拟线程来准备渲染数据
        renderDataPrepareThread = Thread.ofVirtual().start(() -> {
            while (running.get()) {
                // 创建新的渲染数据列表
                List<RenderData> newRenderData = new ArrayList<>(objects.size());
                
                // 使用虚拟线程并行收集渲染数据
                int chunkSize = 100;  // 每个虚拟线程处理100个对象
                List<Thread> threads = new ArrayList<>();
                List<List<RenderData>> chunkResults = new ArrayList<>();
                
                for (int i = 0; i < objects.size(); i += chunkSize) {
                    final int start = i;
                    final int end = Math.min(i + chunkSize, objects.size());
                    final List<RenderData> chunkData = new ArrayList<>(end - start);
                    chunkResults.add(chunkData);
                    
                    // 为每个chunk创建一个虚拟线程来收集渲染数据
                    Thread thread = Thread.ofVirtual().start(() -> {
                        for (int j = start; j < end; j++) {
                            MovingObject obj = objects.get(j);
                            chunkData.add(new RenderData(obj.getX(), obj.getY()));
                        }
                    });
                    threads.add(thread);
                }
                
                // 等待所有虚拟线程完成
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // 合并所有chunk的数据
                for (List<RenderData> chunkData : chunkResults) {
                    newRenderData.addAll(chunkData);
                }
                
                // 原子性地更新渲染数据缓存
                renderDataCache.set(newRenderData);
                
                // 控制准备频率（与渲染同步，约60 FPS）
                try {
                    Thread.sleep(16);  // 约16ms，60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    /**
     * 使用虚拟线程更新所有对象
     */
    private void startUpdateThread() {
        running.set(true);
        
        // 创建虚拟线程来更新对象
        updateThread = Thread.ofVirtual().start(() -> {
            long lastTime = System.nanoTime();
            
            while (running.get()) {
                long currentTime = System.nanoTime();
                float delta = (currentTime - lastTime) / 1_000_000_000f;  // 转换为秒
                lastTime = currentTime;
                
                // 限制delta，避免时间跳跃过大
                if (delta > 0.1f) {
                    delta = 0.1f;
                }
                
                // 使用虚拟线程池并行更新对象
                // 将1000个对象分成多个任务，每个任务处理一部分对象
                int chunkSize = 100;  // 每个虚拟线程处理100个对象
                List<Thread> threads = new ArrayList<>();
                
                for (int i = 0; i < objects.size(); i += chunkSize) {
                    final int start = i;
                    final int end = Math.min(i + chunkSize, objects.size());
                    final float finalDelta = delta;
                    
                    // 为每个chunk创建一个虚拟线程
                    Thread thread = Thread.ofVirtual().start(() -> {
                        for (int j = start; j < end; j++) {
                            objects.get(j).update(finalDelta);
                        }
                    });
                    threads.add(thread);
                }
                
                // 等待所有虚拟线程完成
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                // 控制更新频率（约60 FPS）
                try {
                    Thread.sleep(16);  // 约16ms，60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    @Override
    public void render(float delta) {
        // 清屏
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // 使用预准备的渲染数据（由虚拟线程准备，减少主线程工作量）
        // 主线程只需要执行OpenGL调用，数据准备已在后台完成
        batch.begin();
        List<RenderData> renderData = renderDataCache.get();
        if (!renderData.isEmpty()) {
            // 使用预准备的渲染数据，主线程只需要执行OpenGL调用
            for (RenderData data : renderData) {
                batch.draw(objectTexture, data.x, data.y);
            }
        } else {
            // 回退方案：直接读取对象位置（首次渲染或缓存未准备好时）
            for (MovingObject obj : objects) {
                batch.draw(objectTexture, obj.getX(), obj.getY());
            }
        }
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        // 可以在这里处理窗口大小变化
    }
    
    @Override
    public void pause() {
        // 暂停时可以停止更新线程
    }
    
    @Override
    public void resume() {
        // 恢复时可以重新启动更新线程
    }
    
    @Override
    public void hide() {
        // 停止所有线程
        running.set(false);
        if (updateThread != null) {
            try {
                updateThread.join(1000);  // 等待最多1秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (renderDataPrepareThread != null) {
            try {
                renderDataPrepareThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Override
    public void dispose() {
        // 停止所有线程
        running.set(false);
        if (updateThread != null) {
            try {
                updateThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (renderDataPrepareThread != null) {
            try {
                renderDataPrepareThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 释放资源
        if (batch != null) {
            batch.dispose();
        }
        if (objectTexture != null) {
            objectTexture.dispose();
        }
    }
}

