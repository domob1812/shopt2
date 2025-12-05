package eu.domob.shopt2.adapters;

import android.content.Context;
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
            List<ShoppingListItem> shoppingItems = databaseHelper.getShoppingListForShop(shop.getId());
            
            // Update collapse state
            updateCollapseState(shop.isCollapsed());

            if (shoppingItems.isEmpty()) {
                tvEmptyShop.setVisibility(shop.isCollapsed() ? View.GONE : View.VISIBLE);
                recyclerViewShoppingItems.setVisibility(View.GONE);
            } else {
                tvEmptyShop.setVisibility(View.GONE);
                recyclerViewShoppingItems.setVisibility(shop.isCollapsed() ? View.GONE : View.VISIBLE);
                
                // Create a new adapter for each shop to avoid state confusion
                final ShoppingListAdapter newAdapter = new ShoppingListAdapter(context, shoppingItems, new ShoppingListAdapter.OnShoppingListListener() {
                    @Override
                    public void onItemChecked(ShoppingListItem item, boolean isChecked, int position) {
                        item.setChecked(isChecked);
                        databaseHelper.updateShoppingListItem(item);
                        // Don't call notifyItemChanged - the checkbox state is already updated visually
                    }

                    @Override
                    public void onQuantityClicked(ShoppingListItem item) {
                        if (shoppingListListener != null) {
                            shoppingListListener.onQuantityClicked(item);
                        }
                    }
                });
                recyclerViewShoppingItems.setAdapter(newAdapter);
                shoppingListAdapter = newAdapter;
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
