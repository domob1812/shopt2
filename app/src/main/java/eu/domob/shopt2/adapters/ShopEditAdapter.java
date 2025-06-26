package eu.domob.shopt2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.domob.shopt2.R;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Shop;
import java.util.Collections;
import java.util.List;

public class ShopEditAdapter extends RecyclerView.Adapter<ShopEditAdapter.ShopEditViewHolder> {

    public interface OnShopEditListener {
        void onEditShop(Shop shop);
        void onDeleteShop(Shop shop);
        void onShopMoved(int fromPosition, int toPosition);
    }

    private List<Shop> shops;
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnShopEditListener listener;

    public ShopEditAdapter(Context context, List<Shop> shops, OnShopEditListener listener) {
        this.context = context;
        this.shops = shops;
        this.listener = listener;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ShopEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_edit, parent, false);
        return new ShopEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopEditViewHolder holder, int position) {
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

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(shops, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(shops, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        
        if (listener != null) {
            listener.onShopMoved(fromPosition, toPosition);
        }
    }

    class ShopEditViewHolder extends RecyclerView.ViewHolder {
        private TextView tvShopName;
        private TextView tvItemCount;
        private ImageView ivDragHandle;
        private ImageButton btnEditShop;
        private ImageButton btnDeleteShop;

        public ShopEditViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
            btnEditShop = itemView.findViewById(R.id.btnEditShop);
            btnDeleteShop = itemView.findViewById(R.id.btnDeleteShop);
        }

        public void bind(Shop shop) {
            tvShopName.setText(shop.getName());
            
            int itemCount = databaseHelper.getItemCountForShop(shop.getId());
            String itemCountText = itemCount == 1 ? 
                context.getString(R.string.item_count_singular, itemCount) :
                context.getString(R.string.item_count_plural, itemCount);
            tvItemCount.setText(itemCountText);

            btnEditShop.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditShop(shop);
                }
            });

            btnDeleteShop.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteShop(shop);
                }
            });
        }
    }
}