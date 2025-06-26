package eu.domob.shopt2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import eu.domob.shopt2.adapters.ShopEditAdapter;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Shop;
import eu.domob.shopt2.utils.ItemTouchHelperCallback;
import java.util.ArrayList;
import java.util.List;

public class ShopEditActivity extends AppCompatActivity implements ShopEditAdapter.OnShopEditListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private RecyclerView recyclerViewShops;
    private FloatingActionButton fabAddShop;
    
    private ShopEditAdapter shopEditAdapter;
    private DatabaseHelper databaseHelper;
    private List<Shop> shops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_edit);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadData();
    }

    private void initViews() {
        recyclerViewShops = findViewById(R.id.recyclerViewShops);
        fabAddShop = findViewById(R.id.fabAddShop);
        databaseHelper = DatabaseHelper.getInstance(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        shops = new ArrayList<>();
        shopEditAdapter = new ShopEditAdapter(this, shops, this);
        recyclerViewShops.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewShops.setAdapter(shopEditAdapter);

        // Setup drag and drop
        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerViewShops);

        fabAddShop.setOnClickListener(v -> showAddShopDialog());
    }

    private void showAddShopDialog() {
        showShopDialog(null, getString(R.string.add_shop));
    }

    private void showEditShopDialog(Shop shop) {
        showShopDialog(shop, getString(R.string.edit_shop));
    }

    private void showShopDialog(Shop existingShop, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_shop, null);
        com.google.android.material.textfield.TextInputEditText etShopName = dialogView.findViewById(R.id.etShopName);
        
        if (existingShop != null) {
            etShopName.setText(existingShop.getName());
        }
        builder.setView(dialogView);

        builder.setPositiveButton(existingShop == null ? R.string.add : R.string.save, (dialog, which) -> {
            String shopName = etShopName.getText().toString().trim();
            if (TextUtils.isEmpty(shopName)) {
                Toast.makeText(this, R.string.shop_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingShop == null) {
                // Add new shop
                Shop newShop = new Shop(shopName, shops.size());
                databaseHelper.addShop(newShop);
                Toast.makeText(this, R.string.shop_added, Toast.LENGTH_SHORT).show();
            } else {
                // Update existing shop
                existingShop.setName(shopName);
                databaseHelper.updateShop(existingShop);
                Toast.makeText(this, R.string.shop_updated, Toast.LENGTH_SHORT).show();
            }
            loadData();
        });

        builder.setNegativeButton(R.string.cancel, null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Set focus on the input field and show keyboard
        etShopName.requestFocus();
    }

    private void showDeleteShopDialog(Shop shop) {
        int itemCount = databaseHelper.getItemCountForShop(shop.getId());
        String message = getString(R.string.delete_shop_message, shop.getName(), itemCount);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_shop);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.delete, (dialog, which) -> {
            databaseHelper.deleteShop(shop.getId());
            loadData();
            Toast.makeText(this, R.string.shop_deleted, Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void loadData() {
        shops = databaseHelper.getAllShops();
        shopEditAdapter.updateShops(shops);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditShop(Shop shop) {
        showEditShopDialog(shop);
    }

    @Override
    public void onDeleteShop(Shop shop) {
        showDeleteShopDialog(shop);
    }

    @Override
    public void onShopMoved(int fromPosition, int toPosition) {
        // The adapter already updated the local list, now save to database
        List<Long> newOrder = new ArrayList<>();
        for (Shop shop : shops) {
            newOrder.add(shop.getId());
        }
        databaseHelper.reorderShops(newOrder);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        shopEditAdapter.moveItem(fromPosition, toPosition);
    }
}