package com.mangofit.app.logic;

/** 상점 아이템. ID 형식은 {부위}_{모양}_{색}. 예: hat_a_red */
public final class Item {
    public final int artwork;
    public final int label;
    public final boolean fullBody;
    public final String id;
    public final Slot slot;
    public final String shape;
    public final String color;
    public final int price;
    /** 망고 위 위치·크기. 같은 모양이면 색이 달라도 같다. */
    public final Placement placement;

    public Item(Slot slot, String shape, String color, int price, Placement placement) {
        this.artwork = 0;
        this.label = 0;
        this.fullBody = false;
        this.id = slot.code + "_" + shape + "_" + color;
        this.slot = slot;
        this.shape = shape;
        this.color = color;
        this.price = price;
        this.placement = placement;
    }

    /** 원본 PNG 아이템. 기존 아이템 ID와 구분되는 고정 ID를 사용한다. */
    public Item(String id, Slot slot, int artwork, int label, int price,
                Placement placement, boolean fullBody) {
        this.id = id;
        this.slot = slot;
        this.shape = id;
        this.color = "original";
        this.artwork = artwork;
        this.label = label;
        this.price = price;
        this.placement = placement;
        this.fullBody = fullBody;
    }
}
