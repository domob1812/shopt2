package eu.domob.shopt2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import eu.domob.shopt2.adapters.ItemEditAdapter;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Item;
import eu.domob.shopt2.data.ShoppingListItem;
import eu.domob.shopt2.utils.ItemTouchHelperCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemsEditActivity extends AppCompatActivity implements ItemEditAdapter.OnItemEditListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private RecyclerView recyclerViewItems;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddItem;
    
    private ItemEditAdapter itemEditAdapter;
    private DatabaseHelper databaseHelper;
    private List<Item> items;
    private long shopId;
    private String shopName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_edit);

        getIntentExtras();
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
    }

    private void getIntentExtras() {
        shopId = getIntent().getLongExtra("shopId", -1);
        shopName = getIntent().getStringExtra("shopName");
        if (shopId == -1 || shopName == null) {
            finish();
            return;
        }
    }

    private void initViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddItem = findViewById(R.id.fabAddItem);
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.edit_items, shopName));
        }
    }

    private void setupRecyclerView() {
        items = new ArrayList<>();
        itemEditAdapter = new ItemEditAdapter(this, items, this);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemEditAdapter);

        // Setup drag and drop
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewItems);

        fabAddItem.setOnClickListener(v -> showAddItemDialog());
    }

    private void showAddItemDialog() {
        showItemDialog(null, getString(R.string.add_item));
    }

    private void showEditItemDialog(Item item) {
        showItemDialog(item, getString(R.string.edit_item));
    }

    private void showItemDialog(Item existingItem, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        EditText editText = new EditText(this);
        editText.setHint(R.string.item_name);
        if (existingItem != null) {
            editText.setText(existingItem.getName());
        }
        builder.setView(editText);

        builder.setPositiveButton(existingItem == null ? R.string.add : R.string.save, (dialog, which) -> {
            String itemName = editText.getText().toString().trim();
            if (TextUtils.isEmpty(itemName)) {
                Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingItem == null) {
                // Add new item
                Item newItem = new Item(itemName, shopId, items.size());
                databaseHelper.addItem(newItem);
            } else {
                // Update existing item
                existingItem.setName(itemName);
                databaseHelper.updateItem(existingItem);
            }
            loadData();
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void addCheckedItemsToShoppingList() {
        Set<Long> checkedItemIds = itemEditAdapter.getCheckedItems();
        if (checkedItemIds.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        int addedCount = 0;
        for (long itemId : checkedItemIds) {
            if (!databaseHelper.isItemOnShoppingList(itemId)) {
                for (Item item : items) {
                    if (item.getId() == itemId) {
                        ShoppingListItem shoppingItem = ShoppingListItem.fromItem(item);
                        databaseHelper.addToShoppingList(shoppingItem);
                        addedCount++;
                        break;
                    }
                }
            }
        }

        itemEditAdapter.clearCheckedItems();
        loadData();

        String message = addedCount == 1 ? addedCount + " item added to shopping list" : addedCount + " items added to shopping list";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void deleteCheckedItems() {
        Set<Long> checkedItemIds = itemEditAdapter.getCheckedItems();
        if (checkedItemIds.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        int count = checkedItemIds.size();
        String message = count == 1 ? "Delete 1 item?" : "Delete " + count + " items?";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Items");
        builder.setMessage(message);
        builder.setPositiveButton(R.string.delete, (dialog, which) -> {
            for (long itemId : checkedItemIds) {
                databaseHelper.deleteItem(itemId);
            }
            itemEditAdapter.clearCheckedItems();
            loadData();
            String deleted = count == 1 ? "1 item deleted" : count + " items deleted";
            Toast.makeText(this, deleted, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void loadData() {
        items = databaseHelper.getItemsForShop(shopId);
        itemEditAdapter.updateItems(items);

        if (items.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewItems.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewItems.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_add_to_shopping_list) {
            addCheckedItemsToShoppingList();
            return true;
        } else if (itemId == R.id.action_delete_checked) {
            deleteCheckedItems();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditItem(Item item) {
        showEditItemDialog(item);
    }



    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        // The adapter already updated the local list, now save to database
        List<Long> newOrder = new ArrayList<>();
        for (Item item : items) {
            newOrder.add(item.getId());
        }
        databaseHelper.reorderItems(shopId, newOrder);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        itemEditAdapter.moveItem(fromPosition, toPosition);
    }
}
