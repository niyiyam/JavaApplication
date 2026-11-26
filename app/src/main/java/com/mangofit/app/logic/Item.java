package com.mangofit.app.logic;

/** 상점 아이템. 색을 바꾼 아이템은 {상품ID}__{색상코드}로 구분한다. */
public final class Item {
    /** 색상이 달라도 같은 구매 상품이다. */
    public final String baseId;
    public final ItemPalette.ColorOption tint;
    public final int artwork;
    public final int label;
    public final boolean fullBody;
    public final String id;
    public final Slot slot;
    public final int price;
    /** 망고 위 위치·크기. 같은 모양이면 색이 달라도 같다. */
    public final Placement placement;

    /** 원본 PNG 아이템 */
    public Item(String id, Slot slot, int artwork, int label, int price,
                Placement placement, boolean fullBody) {
        this.baseId = id;
        this.tint = null;
        this.id = id;
        this.slot = slot;
        this.artwork = artwork;
        this.label = label;
        this.price = price;
        this.placement = placement;
        this.fullBody = fullBody;
    }

    public Item(Item base, ItemPalette.ColorOption tint) {
        this.baseId = base.baseId;
        this.tint = tint;
        this.id = base.baseId + "__" + tint.code;
        this.slot = base.slot;
        this.artwork = base.artwork;
        this.label = base.label;
        this.price = base.price;
        this.placement = base.placement;
        this.fullBody = base.fullBody;
    }
}
