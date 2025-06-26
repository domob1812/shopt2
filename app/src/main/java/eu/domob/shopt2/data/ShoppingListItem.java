package eu.domob.shopt2.data;

public class ShoppingListItem {
    private long id;
    private Long itemId; // Reference to configured Item, null for ad-hoc items
    private String name;
    private long shopId;
    private boolean isChecked;
    private int orderIndex;
    private boolean isAdHoc;
    private String quantity;

    public ShoppingListItem() {}

    public ShoppingListItem(long id, Long itemId, String name, long shopId, 
                           boolean isChecked, int orderIndex, boolean isAdHoc, String quantity) {
        this.id = id;
        this.itemId = itemId;
        this.name = name;
        this.shopId = shopId;
        this.isChecked = isChecked;
        this.orderIndex = orderIndex;
        this.isAdHoc = isAdHoc;
        this.quantity = quantity;
    }

    public ShoppingListItem(String name, long shopId, int orderIndex, boolean isAdHoc) {
        this.name = name;
        this.shopId = shopId;
        this.orderIndex = orderIndex;
        this.isAdHoc = isAdHoc;
        this.isChecked = false;
        this.quantity = "";
    }

    // Create from configured item
    public static ShoppingListItem fromItem(Item item) {
        ShoppingListItem shoppingItem = new ShoppingListItem();
        shoppingItem.setItemId(item.getId());
        shoppingItem.setName(item.getName());
        shoppingItem.setShopId(item.getShopId());
        shoppingItem.setOrderIndex(item.getOrderIndex());
        shoppingItem.setAdHoc(false);
        shoppingItem.setChecked(false);
        shoppingItem.setQuantity("");
        return shoppingItem;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isAdHoc() {
        return isAdHoc;
    }

    public void setAdHoc(boolean adHoc) {
        isAdHoc = adHoc;
    }

    public String getQuantity() {
        return quantity != null ? quantity : "";
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", name='" + name + '\'' +
                ", shopId=" + shopId +
                ", isChecked=" + isChecked +
                ", orderIndex=" + orderIndex +
                ", isAdHoc=" + isAdHoc +
                ", quantity='" + quantity + '\'' +
                '}';
    }
}