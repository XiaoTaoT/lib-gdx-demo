package com.github.xiaotaotao.ligdx.laboratory;

import com.badlogic.gdx.*;
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

    private SimplePixelCharacter player1;

    @Override
    public void show() {
        // 在 show() 方法中设置 InputProcessor，而不是在 render() 中
        // 这样可以确保输入处理器在 Screen 显示时就被设置，并且不会被其他代码覆盖
        Gdx.input.setInputProcessor(inputProcessor);
        player1 = new SimplePixelCharacter()

    }

    @Override
    public void render(float delta) {
        // render() 方法中不需要设置 InputProcessor
        // 如果需要在 render() 中处理输入，可以使用 Gdx.input.isKeyPressed() 等方法
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
                    Gdx.app.log("TAG","w 被按下");
                    break;
                case Input.Keys.A:
                    break;
                case Input.Keys.S:
                    break;
                case Input.Keys.D:
                    break;
            }

            return true;
        }
    }

}
