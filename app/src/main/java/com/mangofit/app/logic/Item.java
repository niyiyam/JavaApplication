package com.mangofit.app.logic;

/** 상점 아이템. ID 형식은 {부위}_{모양}_{색}. 예: hat_a_red */
public final class Item {
    public final String id;
    public final Slot slot;
    public final String shape;
    public final String color;
    public final int price;

    public Item(Slot slot, String shape, String color, int price) {
        this.id = slot.code + "_" + shape + "_" + color;
        this.slot = slot;
        this.shape = shape;
        this.color = color;
        this.price = price;
    }
}
