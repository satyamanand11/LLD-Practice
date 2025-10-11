package com.lld.elevator.entities.button;

import com.lld.elevator.enums.ButtonState;

public abstract class Button {
    protected int id;
    protected ButtonState state = ButtonState.IDLE;
    protected long lastChangedAt = System.currentTimeMillis();

    public int getId(){ return id; }
    public ButtonState getState(){ return state; }
    protected void light(){ state = ButtonState.ENGAGED; lastChangedAt = System.currentTimeMillis(); }
    protected void reset(){ state = ButtonState.IDLE; lastChangedAt = System.currentTimeMillis(); }

    public abstract void press();
}
