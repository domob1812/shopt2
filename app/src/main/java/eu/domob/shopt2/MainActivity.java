package eu.domob.shopt2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import eu.domob.shopt2.adapters.ShopCardAdapter;
import eu.domob.shopt2.adapters.ShoppingListAdapter;
import eu.domob.shopt2.adapters.ShopSelectionAdapter;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Item;
import eu.domob.shopt2.data.Shop;
import eu.domob.shopt2.data.ShoppingListItem;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ShopCardAdapter.OnShopCardListener {

    private RecyclerView recyclerViewShops;
    private TextView tvEmptyState;
    private AutoCompleteTextView etItemName;
    private FloatingActionButton fabAddItem;
    
    private ShopCardAdapter shopCardAdapter;
    private DatabaseHelper databaseHelper;
    private List<Shop> shops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupAddItemInput();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        recyclerViewShops = findViewById(R.id.recyclerViewShops);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        etItemName = findViewById(R.id.etItemName);
        fabAddItem = findViewById(R.id.fabAddItem);
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupRecyclerView() {
        shops = new ArrayList<>();
        shopCardAdapter = new ShopCardAdapter(this, shops, this, new ShoppingListAdapter.OnShoppingListListener() {
            @Override
            public void onItemChecked(ShoppingListItem item, boolean isChecked) {
                // Already handled in adapter
            }

            @Override
            public void onQuantityClicked(ShoppingListItem item) {
                showQuantityDialog(item);
            }
        });
        recyclerViewShops.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShops.setAdapter(shopCardAdapter);
    }

    private void setupAddItemInput() {
        // Setup autocomplete for item names
        updateAutoComplete();

        fabAddItem.setOnClickListener(v -> addItemToShoppingList());

        etItemName.setOnEditorActionListener((v, actionId, event) -> {
            addItemToShoppingList();
            return true;
        });
    }

    private void updateAutoComplete() {
        List<Item> allItems = databaseHelper.getAllItems();
        List<String> itemNames = new ArrayList<>();
        for (Item item : allItems) {
            itemNames.add(item.getName());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, itemNames);
        etItemName.setAdapter(adapter);
    }

    private void addItemToShoppingList() {
        String itemName = etItemName.getText().toString().trim();
        if (TextUtils.isEmpty(itemName)) {
            return;
        }

        // Check if shops exist
        if (shops.isEmpty()) {
            Toast.makeText(this, R.string.please_add_shop_first, Toast.LENGTH_SHORT).show();
            return;
        }

        // Find if this item already exists in any shop
        List<Item> allItems = databaseHelper.getAllItems();
        Item existingItem = null;
        for (Item item : allItems) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            // Check if already on shopping list
            if (databaseHelper.isItemOnShoppingList(existingItem.getId())) {
                Toast.makeText(this, itemName + " is already on the shopping list", Toast.LENGTH_SHORT).show();
                etItemName.setText("");
                return;
            }
            
            // Add existing item to shopping list
            ShoppingListItem shoppingItem = ShoppingListItem.fromItem(existingItem);
            databaseHelper.addToShoppingList(shoppingItem);
            Toast.makeText(this, getString(R.string.added_to_shopping_list, itemName), Toast.LENGTH_SHORT).show();
            etItemName.setText("");
            loadData();
        } else {
            // New item - show shop selection dialog
            showShopSelectionDialog(itemName);
        }
    }

    private void showShopSelectionDialog(String itemName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_shop);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_shop_selection, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewShops);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ShopSelectionAdapter adapter = new ShopSelectionAdapter(this, shops, shop -> {
            // Add as ad-hoc item to shopping list
            ShoppingListItem shoppingItem = new ShoppingListItem(itemName, shop.getId(), 999, true);
            databaseHelper.addToShoppingList(shoppingItem);
            Toast.makeText(this, getString(R.string.added_to_shopping_list, itemName), Toast.LENGTH_SHORT).show();
            etItemName.setText("");
            loadData();
        });
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setNegativeButton(R.string.cancel, null);
        
        AlertDialog dialog = builder.create();
        adapter = new ShopSelectionAdapter(this, shops, shop -> {
            dialog.dismiss();
            // Add as ad-hoc item to shopping list
            ShoppingListItem shoppingItem = new ShoppingListItem(itemName, shop.getId(), 999, true);
            databaseHelper.addToShoppingList(shoppingItem);
            Toast.makeText(this, getString(R.string.added_to_shopping_list, itemName), Toast.LENGTH_SHORT).show();
            etItemName.setText("");
            loadData();
        });
        recyclerView.setAdapter(adapter);
        
        dialog.show();
    }

    private void showQuantityDialog(ShoppingListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_quantity);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quantity, null);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        etQuantity.setText(item.getQuantity());

        builder.setView(dialogView);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String quantity = etQuantity.getText().toString().trim();
            item.setQuantity(quantity);
            databaseHelper.updateShoppingListItem(item);
            loadData();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void loadData() {
        shops = databaseHelper.getAllShops();
        shopCardAdapter.updateShops(shops);
        updateAutoComplete();

        if (shops.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            recyclerViewShops.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            recyclerViewShops.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_shops) {
            startActivity(new Intent(this, ShopEditActivity.class));
            return true;
        } else if (id == R.id.action_clear_checked) {
            clearCheckedItems();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearCheckedItems() {
        databaseHelper.removeCheckedItems();
        loadData();
        Toast.makeText(this, "Checked items cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditItemsClicked(Shop shop) {
        Intent intent = new Intent(this, ItemsEditActivity.class);
        intent.putExtra("shopId", shop.getId());
        intent.putExtra("shopName", shop.getName());
        startActivity(intent);
    }
}