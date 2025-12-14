package eu.domob.shopt2.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import eu.domob.shopt2.R;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Shop;
import eu.domob.shopt2.data.ShoppingListItem;
import java.util.List;

public class ShopCardAdapter extends RecyclerView.Adapter<ShopCardAdapter.ShopCardViewHolder> {

    public interface OnShopCardListener {
        void onEditItemsClicked(Shop shop);
    }

    private List<Shop> shops;
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnShopCardListener listener;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private ShoppingListAdapter.OnShoppingListListener shoppingListListener;

    public ShopCardAdapter(Context context, List<Shop> shops, OnShopCardListener listener, ShoppingListAdapter.OnShoppingListListener shoppingListListener) {
        this.context = context;
        this.shops = shops;
        this.listener = listener;
        this.shoppingListListener = shoppingListListener;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ShopCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_card, parent, false);
        return new ShopCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopCardViewHolder holder, int position) {
        Shop shop = shops.get(position);
        holder.bind(shop);
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    public void updateShops(List<Shop> newShops) {
        this.shops = newShops;
        notifyDataSetChanged();
    }

    /**
     * Scrolls the given item into view. If the shop containing the item is collapsed,
     * it will be expanded first. The item will be scrolled to the top of the view
     * (unless it's near the bottom of the list).
     */
    public void scrollToItem(RecyclerView outerRecyclerView, long shoppingListItemId) {
        // Find which shop contains the item
        int shopIndex = -1;
        long shopId = -1;
        int itemPositionInShop = -1;

        SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean dropTickedToBottom = prefs.getBoolean("drop_ticked_to_bottom", false);

        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            List<ShoppingListItem> items = databaseHelper.getShoppingListForShop(shop.getId(), dropTickedToBottom);
            for (int j = 0; j < items.size(); j++) {
                if (items.get(j).getId() == shoppingListItemId) {
                    shopIndex = i;
                    shopId = shop.getId();
                    itemPositionInShop = j;
                    break;
                }
            }
            if (shopIndex >= 0) break;
        }

        if (shopIndex < 0) return;

        // Expand shop if collapsed
        Shop shop = shops.get(shopIndex);
        if (shop.isCollapsed()) {
            shop.setCollapsed(false);
            databaseHelper.updateShopCollapsedState(shop.getId(), false);
            notifyItemChanged(shopIndex);
        }

        // Scroll shop to top first
        LinearLayoutManager layoutManager = (LinearLayoutManager) outerRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(shopIndex, 0);
        }

        // Post to wait for layout, then scroll item into view
        final int finalShopIndex = shopIndex;
        final int finalItemPosition = itemPositionInShop;
        outerRecyclerView.post(() -> {
            RecyclerView.ViewHolder vh = outerRecyclerView.findViewHolderForAdapterPosition(finalShopIndex);
            if (!(vh instanceof ShopCardViewHolder)) return;

            ShopCardViewHolder shopVH = (ShopCardViewHolder) vh;
            RecyclerView innerRecyclerView = shopVH.recyclerViewShoppingItems;

            RecyclerView.ViewHolder itemVH = innerRecyclerView.findViewHolderForAdapterPosition(finalItemPosition);
            if (itemVH == null) return;

            View itemView = itemVH.itemView;

            // Get item's position on screen
            int[] itemLocation = new int[2];
            itemView.getLocationOnScreen(itemLocation);

            int[] rvLocation = new int[2];
            outerRecyclerView.getLocationOnScreen(rvLocation);

            int offset = itemLocation[1] - rvLocation[1];
            outerRecyclerView.scrollBy(0, offset);
        });
    }

    class ShopCardViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout shopHeader;
        private ImageView ivCollapseIndicator;
        private TextView tvShopName;
        private TextView tvEmptyShop;
        private ImageButton btnUncheckShop;
        private ImageButton btnEditItems;
        private RecyclerView recyclerViewShoppingItems;
        private ShoppingListAdapter shoppingListAdapter;

        public ShopCardViewHolder(@NonNull View itemView) {
            super(itemView);
            shopHeader = itemView.findViewById(R.id.shopHeader);
            ivCollapseIndicator = itemView.findViewById(R.id.ivCollapseIndicator);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvEmptyShop = itemView.findViewById(R.id.tvEmptyShop);
            btnUncheckShop = itemView.findViewById(R.id.btnUncheckShop);
            btnEditItems = itemView.findViewById(R.id.btnEditItems);
            recyclerViewShoppingItems = itemView.findViewById(R.id.recyclerViewShoppingItems);
            recyclerViewShoppingItems.setRecycledViewPool(viewPool);

            recyclerViewShoppingItems.setLayoutManager(new LinearLayoutManager(context));
        }

        public void bind(Shop shop) {
            tvShopName.setText(shop.getName());
            
            btnUncheckShop.setOnClickListener(v -> {
                databaseHelper.uncheckItemsForShop(shop.getId());
                notifyDataSetChanged();
            });
            
            btnEditItems.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItemsClicked(shop);
                }
            });

            // Set up collapse/expand functionality
            shopHeader.setOnClickListener(v -> {
                shop.setCollapsed(!shop.isCollapsed());
                databaseHelper.updateShopCollapsedState(shop.getId(), shop.isCollapsed());
                updateCollapseState(shop.isCollapsed());
            });

            // Load shopping list items for this shop
            SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
            boolean dropTickedToBottom = prefs.getBoolean("drop_ticked_to_bottom", false);
            List<ShoppingListItem> shoppingItems = databaseHelper.getShoppingListForShop(shop.getId(), dropTickedToBottom);
            
            // Update collapse state
            updateCollapseState(shop.isCollapsed());

            if (shoppingItems.isEmpty()) {
                tvEmptyShop.setVisibility(shop.isCollapsed() ? View.GONE : View.VISIBLE);
                recyclerViewShoppingItems.setVisibility(View.GONE);
                shoppingListAdapter = null;
            } else {
                tvEmptyShop.setVisibility(View.GONE);
                recyclerViewShoppingItems.setVisibility(shop.isCollapsed() ? View.GONE : View.VISIBLE);
                
                // Always create new adapter to avoid stale references
                shoppingListAdapter = new ShoppingListAdapter(context, shoppingItems, new ShoppingListAdapter.OnShoppingListListener() {
                        @Override
                        public void onItemChecked(ShoppingListItem item, boolean isChecked, int position) {
                            item.setChecked(isChecked);
                            databaseHelper.updateShoppingListItem(item);
                            
                            // Read preference fresh to handle changes
                            SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
                            boolean dropTickedToBottom = prefs.getBoolean("drop_ticked_to_bottom", false);
                            // Always reload the list to re-sort, whether preference is on or off
                            // This ensures the correct order is always displayed
                            // Use the shopId from the item itself, not from captured shop variable
                            List<ShoppingListItem> updatedItems = databaseHelper.getShoppingListForShop(item.getShopId(), dropTickedToBottom);
                            shoppingListAdapter.updateItems(updatedItems);
                        }

                        @Override
                        public void onQuantityClicked(ShoppingListItem item) {
                            if (shoppingListListener != null) {
                                shoppingListListener.onQuantityClicked(item);
                            }
                        }
                    });
                recyclerViewShoppingItems.setAdapter(shoppingListAdapter);
            }
        }

        private void updateCollapseState(boolean isCollapsed) {
            ivCollapseIndicator.setRotation(isCollapsed ? -90 : 0);
            
            // Check if there are items in the RecyclerView
            boolean hasItems = shoppingListAdapter != null && shoppingListAdapter.getItemCount() > 0;
            
            if (hasItems) {
                tvEmptyShop.setVisibility(View.GONE);
                recyclerViewShoppingItems.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
            } else {
                tvEmptyShop.setVisibility(isCollapsed ? View.GONE : View.VISIBLE);
                recyclerViewShoppingItems.setVisibility(View.GONE);
            }
        }
    }
}
