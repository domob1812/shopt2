package eu.domob.shopt2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import eu.domob.shopt2.R;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Item;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemEditAdapter extends RecyclerView.Adapter<ItemEditAdapter.ItemEditViewHolder> {

    public interface OnItemEditListener {
        void onEditItem(Item item);
        void onItemMoved(int fromPosition, int toPosition);
    }

    private List<Item> items;
    private Context context;
    private DatabaseHelper databaseHelper;
    private OnItemEditListener listener;
    private Set<Long> checkedItems = new HashSet<>();

    public ItemEditAdapter(Context context, List<Item> items, OnItemEditListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ItemEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit, parent, false);
        return new ItemEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemEditViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public Set<Long> getCheckedItems() {
        return new HashSet<>(checkedItems);
    }

    public void clearCheckedItems() {
        checkedItems.clear();
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(items, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(items, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        
        if (listener != null) {
            listener.onItemMoved(fromPosition, toPosition);
        }
    }

    class ItemEditViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbItemSelect;
        private TextView tvItemName;
        private TextView tvOnShoppingList;
        private ImageButton btnEditItem;

        public ItemEditViewHolder(@NonNull View itemView) {
            super(itemView);
            cbItemSelect = itemView.findViewById(R.id.cbItemSelect);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvOnShoppingList = itemView.findViewById(R.id.tvOnShoppingList);
            btnEditItem = itemView.findViewById(R.id.btnEditItem);
        }

        public void bind(Item item) {
            tvItemName.setText(item.getName());

            // Check if item is on shopping list
            boolean isOnShoppingList = databaseHelper.isItemOnShoppingList(item.getId());
            if (isOnShoppingList) {
                tvOnShoppingList.setVisibility(View.VISIBLE);
            } else {
                tvOnShoppingList.setVisibility(View.GONE);
            }

            // Set checkbox state
            cbItemSelect.setOnCheckedChangeListener(null);
            cbItemSelect.setChecked(checkedItems.contains(item.getId()));
            cbItemSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkedItems.add(item.getId());
                } else {
                    checkedItems.remove(item.getId());
                }
            });

            // Toggle checkbox on item name click
            itemView.setOnClickListener(v -> {
                cbItemSelect.toggle();
            });

            btnEditItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditItem(item);
                }
            });
        }
    }
}
