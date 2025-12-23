package com.github.xiaotaotao.ligdx.laboratory.shape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * 形状绘制演示 Screen
 * 
 * 功能展示：
 * - 矩形（填充和线条）
 * - 圆形（填充和线条）
 * - 椭圆（填充和线条）
 * - 扇形（填充和线条）
 * - 五角星（填充和线条）
 * - 多边形（三角形、四边形等）
 * - 线条和折线
 * 
 * 操作说明：
 * - 数字键 1-7：切换显示不同的形状
 * - R：切换填充/线条模式
 * - 形状会自动旋转和缩放（动画效果）
 */
public class ShapeDemoScreen implements Screen {
    
    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;
    
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;
    
    /** 当前显示的形状类型 */
    private ShapeType currentShape = ShapeType.RECTANGLE;
    
    /** 是否填充模式（true=填充，false=线条） */
    private boolean filled = true;
    
    /** 动画时间（用于旋转和缩放） */
    private float time = 0f;
    
    /** 形状类型枚举 */
    private enum ShapeType {
        RECTANGLE,  // 矩形
        CIRCLE,     // 圆形
        ELLIPSE,    // 椭圆
        SECTOR,     // 扇形
        STAR,       // 五角星
        POLYGON,    // 多边形
        LINE        // 线条
    }
    
    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        
        batch = new SpriteBatch();
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        
        time = 0f;
    }
    
    @Override
    public void render(float delta) {
        handleInput();
        time += delta;
        
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        
        // 使用 ShapeRenderer 绘制形状
        // 重要：在调用 begin() 之前读取 filled 的值，确保整个绘制过程中使用相同的值
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // 读取当前的 filled 状态（确保在整个绘制过程中保持一致）
        boolean currentFilled = filled;
        ShapeRenderer.ShapeType shapeType = currentFilled ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line;
        
        // 开始绘制（必须在调用任何绘制方法之前调用）
        // 确保 ShapeRenderer 处于正确的模式
        // 注意：如果 ShapeRenderer 已经处于某个状态，begin() 会抛出异常
        // 为了安全起见，我们确保在调用 begin() 之前 ShapeRenderer 处于未开始状态
        try {
            shapeRenderer.end(); // 如果 ShapeRenderer 已经处于某个状态，先结束它
        } catch (Exception ignored) {
            // 如果 end() 失败，说明 ShapeRenderer 可能没有开始，忽略这个异常
        }
        // 现在可以安全地调用 begin()
        shapeRenderer.begin(shapeType);
        
        try {
            // 绘制所有形状（传递当前的 filled 状态）
            drawShapes(currentFilled);
        } finally {
            // 确保总是调用 end()
            shapeRenderer.end();
        }
        
        // 使用 SpriteBatch 绘制 UI
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawUI();
        batch.end();
    }
    
    /**
     * 处理输入
     */
    private void handleInput() {
        // 数字键切换形状
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) currentShape = ShapeType.RECTANGLE;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) currentShape = ShapeType.CIRCLE;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) currentShape = ShapeType.ELLIPSE;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) currentShape = ShapeType.SECTOR;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) currentShape = ShapeType.STAR;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) currentShape = ShapeType.POLYGON;
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_7)) currentShape = ShapeType.LINE;
        
        // R 键切换填充/线条模式
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            filled = !filled;
        }
    }
    
    /**
     * 绘制所有形状
     * @param isFilled 当前是否为填充模式（从 render() 方法传入，确保与 begin() 调用一致）
     */
    private void drawShapes(boolean isFilled) {
        float centerX = VIRTUAL_WIDTH / 2f;
        float centerY = VIRTUAL_HEIGHT / 2f;
        
        // 根据当前形状类型绘制
        switch (currentShape) {
            case RECTANGLE:
                drawRectangle(centerX, centerY, isFilled);
                break;
            case CIRCLE:
                drawCircle(centerX, centerY);
                break;
            case ELLIPSE:
                drawEllipse(centerX, centerY, isFilled);
                break;
            case SECTOR:
                drawSector(centerX, centerY, isFilled);
                break;
            case STAR:
                drawStar(centerX, centerY, isFilled);
                break;
            case POLYGON:
                drawPolygon(centerX, centerY, isFilled);
                break;
            case LINE:
                drawLines(centerX, centerY);
                break;
        }
    }
    
    /**
     * 绘制矩形
     * @param isFilled 是否为填充模式（从 drawShapes() 传入）
     */
    private void drawRectangle(float x, float y, boolean isFilled) {
        float width = 200 + MathUtils.sin(time * 2f) * 50;
        float height = 150 + MathUtils.cos(time * 2f) * 50;
        float rotation = time * 30f; // 每秒旋转30度
        
        shapeRenderer.setColor(0.2f, 0.8f, 1f, 0.8f);
        
        // 传递 isFilled 参数，确保与 ShapeRenderer 的模式一致
        drawRotatedRectangle(x, y, width, height, rotation, isFilled);
    }
    
    /**
     * 绘制矩形（使用 ShapeRenderer.rect 方法）
     * 
     * 注意：rect 方法在 Filled 和 Line 模式下都可以使用，避免了模式不匹配的问题
     * 但是 rect 方法不支持旋转，如果需要旋转效果，需要使用 polygon 方法
     * 
     * @param isFilled 是否为填充模式（rect 方法会自动根据 ShapeRenderer 的模式处理）
     */
    private void drawRotatedRectangle(float x, float y, float width, float height, float rotation, boolean isFilled) {
        // 计算矩形的左下角坐标（rect 方法使用左下角作为起点）
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        float rectX = x - halfWidth;
        float rectY = y - halfHeight;
        
        // 使用 ShapeRenderer 的 rect 方法绘制矩形
        // rect 方法在 Filled 和 Line 模式下都可以使用，无需检查模式
        // 参数：x, y, width, height
        shapeRenderer.rect(rectX, rectY, width, height);
        
        // 注意：如果需要旋转效果，rect 方法不支持旋转
        // 如果需要旋转，可以：
        // 1. 使用 polygon 方法（需要确保 ShapeRenderer 处于正确的模式）
        // 2. 或者使用 Matrix4 变换（更复杂）
    }
    
    /**
     * 绘制圆形
     */
    private void drawCircle(float x, float y) {
        float radius = 100 + MathUtils.sin(time * 2f) * 30;
        
        shapeRenderer.setColor(1f, 0.3f, 0.3f, 0.8f);
        shapeRenderer.circle(x, y, radius);
    }
    
    /**
     * 绘制椭圆
     * @param isFilled 是否为填充模式
     */
    private void drawEllipse(float x, float y, boolean isFilled) {
        float radiusX = 150 + MathUtils.sin(time * 2f) * 50;
        float radiusY = 100 + MathUtils.cos(time * 2f) * 30;
        int segments = 64; // 椭圆分段数（越多越平滑）
        
        shapeRenderer.setColor(0.3f, 1f, 0.3f, 0.8f);
        
        if (isFilled) {
            // 填充椭圆（用多边形模拟）
            drawFilledEllipse(x, y, radiusX, radiusY, segments);
        } else {
            // 线条椭圆
            drawEllipseOutline(x, y, radiusX, radiusY, segments);
        }
    }
    
    /**
     * 绘制填充椭圆
     */
    private void drawFilledEllipse(float x, float y, float radiusX, float radiusY, int segments) {
        float[] vertices = new float[segments * 2 + 2];
        vertices[0] = x;
        vertices[1] = y;
        
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * MathUtils.PI2;
            vertices[i * 2 + 2] = x + MathUtils.cos(angle) * radiusX;
            vertices[i * 2 + 3] = y + MathUtils.sin(angle) * radiusY;
        }
        
        shapeRenderer.triangle(x, y, vertices[2], vertices[3], vertices[segments * 2], vertices[segments * 2 + 1]);
        for (int i = 0; i < segments; i++) {
            shapeRenderer.triangle(x, y, 
                vertices[i * 2 + 2], vertices[i * 2 + 3],
                vertices[(i + 1) * 2 + 2], vertices[(i + 1) * 2 + 3]);
        }
    }
    
    /**
     * 绘制椭圆轮廓
     */
    private void drawEllipseOutline(float x, float y, float radiusX, float radiusY, int segments) {
        for (int i = 0; i < segments; i++) {
            float angle1 = (i / (float) segments) * MathUtils.PI2;
            float angle2 = ((i + 1) / (float) segments) * MathUtils.PI2;
            
            float x1 = x + MathUtils.cos(angle1) * radiusX;
            float y1 = y + MathUtils.sin(angle1) * radiusY;
            float x2 = x + MathUtils.cos(angle2) * radiusX;
            float y2 = y + MathUtils.sin(angle2) * radiusY;
            
            shapeRenderer.line(x1, y1, x2, y2);
        }
    }
    
    /**
     * 绘制扇形
     * @param isFilled 是否为填充模式
     */
    private void drawSector(float x, float y, boolean isFilled) {
        float radius = 120 + MathUtils.sin(time * 2f) * 40;
        float startAngle = time * 45f; // 起始角度（旋转）
        float angleSpan = 90f; // 扇形角度范围（90度）
        int segments = 32;
        
        shapeRenderer.setColor(1f, 1f, 0.3f, 0.8f);
        
        if (isFilled) {
            // 填充扇形
            drawFilledSector(x, y, radius, startAngle, angleSpan, segments);
        } else {
            // 线条扇形
            drawSectorOutline(x, y, radius, startAngle, angleSpan, segments);
        }
    }
    
    /**
     * 绘制填充扇形
     */
    private void drawFilledSector(float x, float y, float radius, float startAngle, float angleSpan, int segments) {
        float startRad = startAngle * MathUtils.degreesToRadians;
        float spanRad = angleSpan * MathUtils.degreesToRadians;
        
        // 中心点
        float centerX = x;
        float centerY = y;
        
        // 起始点
        float prevX = centerX + MathUtils.cos(startRad) * radius;
        float prevY = centerY + MathUtils.sin(startRad) * radius;
        
        // 绘制扇形（用三角形拼接）
        for (int i = 1; i <= segments; i++) {
            float angle = startRad + (spanRad * i / segments);
            float currX = centerX + MathUtils.cos(angle) * radius;
            float currY = centerY + MathUtils.sin(angle) * radius;
            
            shapeRenderer.triangle(centerX, centerY, prevX, prevY, currX, currY);
            
            prevX = currX;
            prevY = currY;
        }
    }
    
    /**
     * 绘制扇形轮廓
     */
    private void drawSectorOutline(float x, float y, float radius, float startAngle, float angleSpan, int segments) {
        float startRad = startAngle * MathUtils.degreesToRadians;
        float spanRad = angleSpan * MathUtils.degreesToRadians;
        
        // 绘制两条半径
        float endX = x + MathUtils.cos(startRad) * radius;
        float endY = y + MathUtils.sin(startRad) * radius;
        shapeRenderer.line(x, y, endX, endY);
        
        float endX2 = x + MathUtils.cos(startRad + spanRad) * radius;
        float endY2 = y + MathUtils.sin(startRad + spanRad) * radius;
        shapeRenderer.line(x, y, endX2, endY2);
        
        // 绘制弧线
        float prevX = endX;
        float prevY = endY;
        for (int i = 1; i <= segments; i++) {
            float angle = startRad + (spanRad * i / segments);
            float currX = x + MathUtils.cos(angle) * radius;
            float currY = y + MathUtils.sin(angle) * radius;
            
            shapeRenderer.line(prevX, prevY, currX, currY);
            
            prevX = currX;
            prevY = currY;
        }
    }
    
    /**
     * 绘制五角星
     * @param isFilled 是否为填充模式
     */
    private void drawStar(float x, float y, boolean isFilled) {
        float outerRadius = 100 + MathUtils.sin(time * 2f) * 30;
        float innerRadius = outerRadius * 0.4f;
        int points = 5; // 五角星
        float rotation = time * 20f; // 旋转动画
        
        shapeRenderer.setColor(1f, 0.5f, 0.8f, 0.8f);
        
        // 计算五角星的顶点
        float[] vertices = calculateStarVertices(x, y, outerRadius, innerRadius, points, rotation);
        
        if (isFilled) {
            // 填充五角星（用多边形）
            shapeRenderer.polygon(vertices);
        } else {
            // 线条五角星（用折线）
            for (int i = 0; i < points * 2; i++) {
                int next = (i + 1) % (points * 2);
                shapeRenderer.line(vertices[i * 2], vertices[i * 2 + 1], 
                                 vertices[next * 2], vertices[next * 2 + 1]);
            }
        }
    }
    
    /**
     * 计算五角星的顶点坐标
     */
    private float[] calculateStarVertices(float x, float y, float outerRadius, float innerRadius, 
                                          int points, float rotation) {
        float[] vertices = new float[points * 4];
        float angleStep = 360f / points;
        float rotationRad = rotation * MathUtils.degreesToRadians;
        
        for (int i = 0; i < points; i++) {
            // 外顶点
            float outerAngle = (i * angleStep) * MathUtils.degreesToRadians + rotationRad;
            vertices[i * 4] = x + MathUtils.cos(outerAngle) * outerRadius;
            vertices[i * 4 + 1] = y + MathUtils.sin(outerAngle) * outerRadius;
            
            // 内顶点
            float innerAngle = ((i + 0.5f) * angleStep) * MathUtils.degreesToRadians + rotationRad;
            vertices[i * 4 + 2] = x + MathUtils.cos(innerAngle) * innerRadius;
            vertices[i * 4 + 3] = y + MathUtils.sin(innerAngle) * innerRadius;
        }
        
        return vertices;
    }
    
    /**
     * 绘制多边形（六边形）
     * @param isFilled 是否为填充模式
     */
    private void drawPolygon(float x, float y, boolean isFilled) {
        float radius = 100 + MathUtils.sin(time * 2f) * 30;
        int sides = 6; // 六边形
        float rotation = time * 25f;
        
        shapeRenderer.setColor(0.8f, 0.3f, 1f, 0.8f);
        
        // 计算多边形顶点
        float[] vertices = calculatePolygonVertices(x, y, radius, sides, rotation);
        
        if (isFilled) {
            shapeRenderer.polygon(vertices);
        } else {
            // 绘制多边形轮廓
            for (int i = 0; i < sides; i++) {
                int next = (i + 1) % sides;
                shapeRenderer.line(vertices[i * 2], vertices[i * 2 + 1], 
                                 vertices[next * 2], vertices[next * 2 + 1]);
            }
        }
    }
    
    /**
     * 计算多边形顶点坐标
     */
    private float[] calculatePolygonVertices(float x, float y, float radius, int sides, float rotation) {
        float[] vertices = new float[sides * 2];
        float angleStep = 360f / sides;
        float rotationRad = rotation * MathUtils.degreesToRadians;
        
        for (int i = 0; i < sides; i++) {
            float angle = (i * angleStep) * MathUtils.degreesToRadians + rotationRad;
            vertices[i * 2] = x + MathUtils.cos(angle) * radius;
            vertices[i * 2 + 1] = y + MathUtils.sin(angle) * radius;
        }
        
        return vertices;
    }
    
    /**
     * 绘制线条和折线
     */
    private void drawLines(float x, float y) {
        shapeRenderer.setColor(0.3f, 1f, 1f, 0.8f);
        
        // 绘制一些示例线条
        float radius = 150;
        float angle = time * 30f;
        
        // 从中心向外辐射的线条
        for (int i = 0; i < 8; i++) {
            float lineAngle = (angle + i * 45f) * MathUtils.degreesToRadians;
            float endX = x + MathUtils.cos(lineAngle) * radius;
            float endY = y + MathUtils.sin(lineAngle) * radius;
            shapeRenderer.line(x, y, endX, endY);
        }
        
        // 绘制一个螺旋线
        shapeRenderer.setColor(1f, 0.8f, 0.3f, 0.8f);
        float spiralRadius = 20;
        float prevX = x;
        float prevY = y;
        for (int i = 0; i < 100; i++) {
            float spiralAngle = i * 0.2f;
            float currRadius = spiralRadius + i * 1.5f;
            float currX = x + MathUtils.cos(spiralAngle) * currRadius;
            float currY = y + MathUtils.sin(spiralAngle) * currRadius;
            shapeRenderer.line(prevX, prevY, currX, currY);
            prevX = currX;
            prevY = currY;
        }
    }
    
    /**
     * 绘制 UI
     */
    private void drawUI() {
        font.setColor(1f, 1f, 1f, 1f);
        
        // 标题
        font.draw(batch, "Shape Drawing Demo", 20, VIRTUAL_HEIGHT - 20);
        
        // 当前形状
        String shapeName = currentShape.name();
        font.draw(batch, "Current Shape: " + shapeName, 20, VIRTUAL_HEIGHT - 50);
        
        // 模式
        String mode = filled ? "Filled" : "Line";
        font.draw(batch, "Mode: " + mode, 20, VIRTUAL_HEIGHT - 80);
        
        // 操作提示
        font.draw(batch, "Controls:", 20, VIRTUAL_HEIGHT - 120);
        font.draw(batch, "1-7: Switch Shape", 20, VIRTUAL_HEIGHT - 150);
        font.draw(batch, "R: Toggle Fill/Line", 20, VIRTUAL_HEIGHT - 180);
        
        // 形状列表
        font.setColor(0.7f, 0.7f, 0.7f, 1f);
        font.draw(batch, "1: Rectangle", 20, VIRTUAL_HEIGHT - 220);
        font.draw(batch, "2: Circle", 20, VIRTUAL_HEIGHT - 250);
        font.draw(batch, "3: Ellipse", 20, VIRTUAL_HEIGHT - 280);
        font.draw(batch, "4: Sector", 20, VIRTUAL_HEIGHT - 310);
        font.draw(batch, "5: Star", 20, VIRTUAL_HEIGHT - 340);
        font.draw(batch, "6: Polygon", 20, VIRTUAL_HEIGHT - 370);
        font.draw(batch, "7: Lines", 20, VIRTUAL_HEIGHT - 400);
        
        font.setColor(1f, 1f, 1f, 1f);
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    
    @Override
    public void pause() {
    }
    
    @Override
    public void resume() {
    }
    
    @Override
    public void hide() {
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}

