package eu.domob.shopt2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.domob.shopt2.R;
import eu.domob.shopt2.data.Shop;
import java.util.List;

public class ShopSelectionAdapter extends RecyclerView.Adapter<ShopSelectionAdapter.ShopSelectionViewHolder> {

    public interface OnShopSelectionListener {
        void onShopSelected(Shop shop);
    }

    private List<Shop> shops;
    private Context context;
    private OnShopSelectionListener listener;

    public ShopSelectionAdapter(Context context, List<Shop> shops, OnShopSelectionListener listener) {
        this.context = context;
        this.shops = shops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_selection, parent, false);
        return new ShopSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopSelectionViewHolder holder, int position) {
        Shop shop = shops.get(position);
        holder.bind(shop);
    }

    @Override
    public int getItemCount() {
        return shops.size();
    }

    class ShopSelectionViewHolder extends RecyclerView.ViewHolder {
        private TextView tvShopName;

        public ShopSelectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvShopName = itemView.findViewById(R.id.tvShopName);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    Shop shop = shops.get(getAdapterPosition());
                    listener.onShopSelected(shop);
                }
            });
        }

        public void bind(Shop shop) {
            tvShopName.setText(shop.getName());
        }
    }
}