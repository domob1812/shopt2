package eu.domob.shopt2.data;

/**
 * Describes a single item that is being copied or moved from one shop to another.
 *
 * <p>A transfer either refers to a configured item ({@link #itemId} non-null) or to an
 * ad-hoc shopping-list entry ({@link #itemId} null). If the item is also present on the
 * source shop's shopping list, {@link #shoppingListItemId} holds that entry's id and
 * {@link #quantity} its quantity.</p>
 */
public class ItemTransfer {
    private final Long itemId;
    private final Long shoppingListItemId;
    private final String name;
    private final long sourceShopId;
    private final String quantity;

    public ItemTransfer(Long itemId, Long shoppingListItemId, String name,
                        long sourceShopId, String quantity) {
        this.itemId = itemId;
        this.shoppingListItemId = shoppingListItemId;
        this.name = name;
        this.sourceShopId = sourceShopId;
        this.quantity = quantity != null ? quantity : "";
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getShoppingListItemId() {
        return shoppingListItemId;
    }

    public String getName() {
        return name;
    }

    public long getSourceShopId() {
        return sourceShopId;
    }

    public String getQuantity() {
        return quantity;
    }
}
