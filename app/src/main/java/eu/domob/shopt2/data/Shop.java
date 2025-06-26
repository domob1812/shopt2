package eu.domob.shopt2.data;

public class Shop {
    private long id;
    private String name;
    private int orderIndex;

    public Shop() {}

    public Shop(long id, String name, int orderIndex) {
        this.id = id;
        this.name = name;
        this.orderIndex = orderIndex;
    }

    public Shop(String name, int orderIndex) {
        this.name = name;
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

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", orderIndex=" + orderIndex +
                '}';
    }
}