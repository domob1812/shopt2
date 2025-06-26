package eu.domob.shopt2.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.domob.shopt2.R;
import eu.domob.shopt2.data.ShoppingListItem;
import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {

    public interface OnShoppingListListener {
        void onItemChecked(ShoppingListItem item, boolean isChecked);
        void onQuantityClicked(ShoppingListItem item);
    }

    private List<ShoppingListItem> items;
    private Context context;
    private OnShoppingListListener listener;

    public ShoppingListAdapter(Context context, List<ShoppingListItem> items, OnShoppingListListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping_list, parent, false);
        return new ShoppingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
        if (items != null && position < items.size()) {
            ShoppingListItem item = items.get(position);
            holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public void updateItems(List<ShoppingListItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbItemChecked;
        private TextView tvItemName;
        private TextView tvQuantity;
        private ImageButton btnAddQuantity;

        public ShoppingListViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItemChecked = itemView.findViewById(R.id.cbItemChecked);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAddQuantity = itemView.findViewById(R.id.btnAddQuantity);
        }

        public void bind(ShoppingListItem item) {
            tvItemName.setText(item.getName());
            cbItemChecked.setChecked(item.isChecked());

            // Apply strikethrough for checked items
            if (item.isChecked()) {
                tvItemName.setPaintFlags(tvItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvItemName.setAlpha(0.6f);
            } else {
                tvItemName.setPaintFlags(tvItemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvItemName.setAlpha(1.0f);
            }

            // Handle quantity display
            String quantity = item.getQuantity();
            if (quantity != null && !quantity.trim().isEmpty()) {
                tvQuantity.setText(quantity);
                tvQuantity.setVisibility(View.VISIBLE);
                btnAddQuantity.setVisibility(View.GONE);
            } else {
                tvQuantity.setVisibility(View.GONE);
                btnAddQuantity.setVisibility(View.VISIBLE);
            }

            // Set up click listeners
            cbItemChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onItemChecked(item, isChecked);
                }
            });

            btnAddQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityClicked(item);
                }
            });

            tvQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuantityClicked(item);
                }
            });
        }
    }
}