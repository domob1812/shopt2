package eu.domob.shopt2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
        private TextView tvShopName;
        private TextView tvEmptyShop;
        private ImageButton btnEditItems;
        private RecyclerView recyclerViewShoppingItems;
        private ShoppingListAdapter shoppingListAdapter;

        public ShopCardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvEmptyShop = itemView.findViewById(R.id.tvEmptyShop);
            btnEditItems = itemView.findViewById(R.id.btnEditItems);
            recyclerViewShoppingItems = itemView.findViewById(R.id.recyclerViewShoppingItems);

            recyclerViewShoppingItems.setLayoutManager(new LinearLayoutManager(context));
            shoppingListAdapter = new ShoppingListAdapter(context, null, new ShoppingListAdapter.OnShoppingListListener() {
                @Override
                public void onItemChecked(ShoppingListItem item, boolean isChecked) {
                    item.setChecked(isChecked);
                    databaseHelper.updateShoppingListItem(item);
                    // Post the adapter notification to avoid RecyclerView layout issues
                    recyclerViewShoppingItems.post(() -> shoppingListAdapter.notifyDataSetChanged());
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

        public void bind(Shop shop) {
            tvShopName.setText(shop.getName());
            
            btnEditItems.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItemsClicked(shop);
                }
            });

            // Load shopping list items for this shop
            List<ShoppingListItem> shoppingItems = databaseHelper.getShoppingListForShop(shop.getId());
            
            if (shoppingItems.isEmpty()) {
                tvEmptyShop.setVisibility(View.VISIBLE);
                recyclerViewShoppingItems.setVisibility(View.GONE);
            } else {
                tvEmptyShop.setVisibility(View.GONE);
                recyclerViewShoppingItems.setVisibility(View.VISIBLE);
                shoppingListAdapter.updateItems(shoppingItems);
            }
        }
    }
}