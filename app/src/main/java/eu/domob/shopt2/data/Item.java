package eu.domob.shopt2.data;

public class Item {
    private long id;
    private String name;
    private long shopId;
    private int orderIndex;

    public Item() {}

    public Item(long id, String name, long shopId, int orderIndex) {
        this.id = id;
        this.name = name;
        this.shopId = shopId;
        this.orderIndex = orderIndex;
    }

    public Item(String name, long shopId, int orderIndex) {
        this.name = name;
        this.shopId = shopId;
        this.orderIndex = orderIndex;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shopId=" + shopId +
                ", orderIndex=" + orderIndex +
                '}';
    }
}