package com.github.xiaotaotao.ligdx.laboratory.player;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

public interface SystemInterfaceDesign {

}

//region
class Entity {
    //位置
    private Vector2 position;

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }
}
//endregion

//region 控制系统

interface IControl {
    //键盘输入
//    void keyPressed(Input.Keys key);

    //鼠标输入
//    void buttonPressed(Input.Buttons button);
}

enum MoveDirection {
    /**
     * 向下（屏幕下方）
     */
    DOWN,
    /**
     * 向左（屏幕左侧）
     */
    LEFT,
    /**
     * 向右（屏幕右侧）
     */
    RIGHT,
    /**
     * 向上（屏幕上方）
     */
    UP
}

interface IMoveControl {
    void move(Entity entity, MoveDirection moveDirection);
}

class FourDirectionalMovement implements IMoveControl {
    private Entity entity;
    private MoveDirection moveDirection;
    InputProcessor inputProcessor = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {

            float fx = 0, fy = 0;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) fx -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) fx += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) fy += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) fy -= 1;

            Vector2 position = entity.getPosition();

            // 计算本帧移动距离（像素单位）
            float distance = 120f * Gdx.graphics.getDeltaTime();

            // 计算位移分量
            float dx = fx * distance;
            float dy = fy * distance;

            // 计算新位置
            float newX = position.x + dx;
            float newY = position.y + dy;

            // 更新位置（可以是任意浮点数，不做格子对齐）
            position.set(newX, newY);

            return true;
        }
    };

    void moveUp(Entity entity) {
        move(entity, MoveDirection.UP);
    }

    void moveDown(Entity entity) {
        move(entity, MoveDirection.DOWN);
    }

    @Override
    public void move(Entity entity, MoveDirection moveDirection) {

    }
}

//endregion
