package com.badlogic.gdx.scenes.scene2d.ui;

import ninja.trek.entity.Entity;
import ninja.trek.ui.UIActionButton;

/**
 * Created by n on 24/11/2017.
 */

class UIOtherActionButton extends UIActionButton {
    public final Entity.ButtonType type;

    public UIOtherActionButton(int i, Skin skin, Table entityActionTable, Entity.ButtonType t) {
        super(i, "", skin, entityActionTable);
        setText("" + t);
        type = t;
    }
}
