package com.github.xiaotaotao.ligdx.laboratory;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.xiaotaotao.ligdx.laboratory.character.SimplePixelCharacter;

/**
 * @Desciption:
 * @ClassName:P2Screen
 * @Author:TwT
 * @Date:2025/12/23 22:32
 * @Version:1.0
 **/
public class P2Screen implements Screen {
    private InputProcessor inputProcessor = new InputProcessorImpl();
    private static final float VIRTUAL_WIDTH = 1920;
    private static final float VIRTUAL_HEIGHT = 1080;
    private SimplePixelCharacter player1;
    // 以颜色块代替的“贴图”
    private Texture playerTexture;
    private SpriteBatch batch;

    private boolean isMoving = false;
    private float fx = 0, fy = 0;

    @Override
    public void show() {
        // 在 show() 方法中设置 InputProcessor，而不是在 render() 中
        // 这样可以确保输入处理器在 Screen 显示时就被设置，并且不会被其他代码覆盖
        Gdx.input.setInputProcessor(inputProcessor);
        player1 = SimplePixelCharacter.create();

        batch = new SpriteBatch();

        // 简单的 1x1 像素纹理，用于拉伸绘制
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 1, 0, 1);
        pixmap.fill();
        playerTexture = new Texture(pixmap);
    }

    @Override
    public void render(float delta) {
        // render() 方法中不需要设置 InputProcessor
        // 如果需要在 render() 中处理输入，可以使用 Gdx.input.isKeyPressed() 等方法
        player1.getMovementController().move(fx, fy);

        player1.update(delta);
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        player1.draw(batch, playerTexture);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

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

    }


    class InputProcessorImpl extends InputAdapter {


        @Override
        public boolean keyDown(int keycode) {
            switch (keycode) {
                case Input.Keys.W:
                    fy += 1;
                    break;
                case Input.Keys.A:
                    fx -= 1;
                    break;
                case Input.Keys.S:
                    fy -= 1;
                    break;
                case Input.Keys.D:
                    fx += 1;
                    break;
            }
            isMoving = true;
            return true;
        }

        @Override
        public boolean keyUp(int keycode) {
            fx = 0;
            fy = 0;
            isMoving = false;
            return true;
        }
    }

}
