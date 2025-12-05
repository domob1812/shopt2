package eu.domob.shopt2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "shopt.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_SHOPS = "shops";
    private static final String TABLE_ITEMS = "items";
    private static final String TABLE_SHOPPING_LIST = "shopping_list";

    // Shop table columns
    private static final String SHOP_ID = "id";
    private static final String SHOP_NAME = "name";
    private static final String SHOP_ORDER_INDEX = "order_index";
    private static final String SHOP_IS_COLLAPSED = "is_collapsed";

    // Item table columns
    private static final String ITEM_ID = "id";
    private static final String ITEM_NAME = "name";
    private static final String ITEM_SHOP_ID = "shop_id";
    private static final String ITEM_ORDER_INDEX = "order_index";

    // Shopping list table columns
    private static final String SL_ID = "id";
    private static final String SL_ITEM_ID = "item_id";
    private static final String SL_NAME = "name";
    private static final String SL_SHOP_ID = "shop_id";
    private static final String SL_IS_CHECKED = "is_checked";
    private static final String SL_ORDER_INDEX = "order_index";
    private static final String SL_IS_AD_HOC = "is_ad_hoc";
    private static final String SL_QUANTITY = "quantity";

    // Create table statements
    private static final String CREATE_SHOPS_TABLE = "CREATE TABLE " + TABLE_SHOPS + " ("
            + SHOP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SHOP_NAME + " TEXT NOT NULL, "
            + SHOP_ORDER_INDEX + " INTEGER DEFAULT 0, "
            + SHOP_IS_COLLAPSED + " INTEGER DEFAULT 0"
            + ")";

    private static final String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + " ("
            + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ITEM_NAME + " TEXT NOT NULL, "
            + ITEM_SHOP_ID + " INTEGER NOT NULL, "
            + ITEM_ORDER_INDEX + " INTEGER DEFAULT 0, "
            + "FOREIGN KEY(" + ITEM_SHOP_ID + ") REFERENCES " + TABLE_SHOPS + "(" + SHOP_ID + ") ON DELETE CASCADE"
            + ")";

    private static final String CREATE_SHOPPING_LIST_TABLE = "CREATE TABLE " + TABLE_SHOPPING_LIST + " ("
            + SL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SL_ITEM_ID + " INTEGER, "
            + SL_NAME + " TEXT NOT NULL, "
            + SL_SHOP_ID + " INTEGER NOT NULL, "
            + SL_IS_CHECKED + " INTEGER DEFAULT 0, "
            + SL_ORDER_INDEX + " INTEGER DEFAULT 0, "
            + SL_IS_AD_HOC + " INTEGER DEFAULT 0, "
            + SL_QUANTITY + " TEXT DEFAULT '', "
            + "FOREIGN KEY(" + SL_SHOP_ID + ") REFERENCES " + TABLE_SHOPS + "(" + SHOP_ID + ") ON DELETE CASCADE, "
            + "FOREIGN KEY(" + SL_ITEM_ID + ") REFERENCES " + TABLE_ITEMS + "(" + ITEM_ID + ") ON DELETE CASCADE"
            + ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SHOPS_TABLE);
        db.execSQL(CREATE_ITEMS_TABLE);  
        db.execSQL(CREATE_SHOPPING_LIST_TABLE);
        
        // Insert demo data
        insertDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_SHOPS + " ADD COLUMN " + SHOP_IS_COLLAPSED + " INTEGER DEFAULT 0");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    private void insertDemoData(SQLiteDatabase db) {
        // Demo shops
        ContentValues shopValues = new ContentValues();
        shopValues.put(SHOP_NAME, "Grocery Store");
        shopValues.put(SHOP_ORDER_INDEX, 0);
        long groceryId = db.insert(TABLE_SHOPS, null, shopValues);

        shopValues.clear();
        shopValues.put(SHOP_NAME, "Hardware Store");
        shopValues.put(SHOP_ORDER_INDEX, 1);
        long hardwareId = db.insert(TABLE_SHOPS, null, shopValues);

        // Demo items for grocery store
        ContentValues itemValues = new ContentValues();
        String[] groceryItems = {"Milk", "Bread", "Eggs", "Apples"};
        for (int i = 0; i < groceryItems.length; i++) {
            itemValues.clear();
            itemValues.put(ITEM_NAME, groceryItems[i]);
            itemValues.put(ITEM_SHOP_ID, groceryId);
            itemValues.put(ITEM_ORDER_INDEX, i);
            db.insert(TABLE_ITEMS, null, itemValues);
        }

        // Demo items for hardware store
        String[] hardwareItems = {"Nails", "Hammer"};
        for (int i = 0; i < hardwareItems.length; i++) {
            itemValues.clear();
            itemValues.put(ITEM_NAME, hardwareItems[i]);
            itemValues.put(ITEM_SHOP_ID, hardwareId);
            itemValues.put(ITEM_ORDER_INDEX, i);
            db.insert(TABLE_ITEMS, null, itemValues);
        }

        // Add some items to shopping list
        ContentValues slValues = new ContentValues();
        // Get item IDs for the demo items
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{ITEM_ID}, 
                                ITEM_NAME + "=? AND " + ITEM_SHOP_ID + "=?", 
                                new String[]{"Milk", String.valueOf(groceryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            long milkId = cursor.getLong(0);
            slValues.put(SL_ITEM_ID, milkId);
            slValues.put(SL_NAME, "Milk");
            slValues.put(SL_SHOP_ID, groceryId);
            slValues.put(SL_ORDER_INDEX, 0);
            slValues.put(SL_IS_AD_HOC, 0);
            db.insert(TABLE_SHOPPING_LIST, null, slValues);
        }
        cursor.close();

        cursor = db.query(TABLE_ITEMS, new String[]{ITEM_ID}, 
                         ITEM_NAME + "=? AND " + ITEM_SHOP_ID + "=?", 
                         new String[]{"Eggs", String.valueOf(groceryId)}, null, null, null);
        if (cursor.moveToFirst()) {
            long eggsId = cursor.getLong(0);
            slValues.clear();
            slValues.put(SL_ITEM_ID, eggsId);
            slValues.put(SL_NAME, "Eggs");
            slValues.put(SL_SHOP_ID, groceryId);
            slValues.put(SL_ORDER_INDEX, 2);
            slValues.put(SL_IS_AD_HOC, 0);
            db.insert(TABLE_SHOPPING_LIST, null, slValues);
        }
        cursor.close();

        cursor = db.query(TABLE_ITEMS, new String[]{ITEM_ID}, 
                         ITEM_NAME + "=? AND " + ITEM_SHOP_ID + "=?", 
                         new String[]{"Nails", String.valueOf(hardwareId)}, null, null, null);
        if (cursor.moveToFirst()) {
            long nailsId = cursor.getLong(0);
            slValues.clear();
            slValues.put(SL_ITEM_ID, nailsId);
            slValues.put(SL_NAME, "Nails");
            slValues.put(SL_SHOP_ID, hardwareId);
            slValues.put(SL_ORDER_INDEX, 0);
            slValues.put(SL_IS_AD_HOC, 0);
            db.insert(TABLE_SHOPPING_LIST, null, slValues);
        }
        cursor.close();

        // Add an ad-hoc item
        slValues.clear();
        slValues.put(SL_NAME, "Cookies");
        slValues.put(SL_SHOP_ID, groceryId);
        slValues.put(SL_ORDER_INDEX, 999);
        slValues.put(SL_IS_AD_HOC, 1);
        db.insert(TABLE_SHOPPING_LIST, null, slValues);
    }

    // Shop CRUD operations
    public long addShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SHOP_NAME, shop.getName());
        values.put(SHOP_ORDER_INDEX, shop.getOrderIndex());
        
        long id = db.insert(TABLE_SHOPS, null, values);
        shop.setId(id);
        return id;
    }

    public List<Shop> getAllShops() {
        List<Shop> shops = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_SHOPS, null, null, null, null, null, SHOP_ORDER_INDEX + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                Shop shop = new Shop();
                shop.setId(cursor.getLong(cursor.getColumnIndexOrThrow(SHOP_ID)));
                shop.setName(cursor.getString(cursor.getColumnIndexOrThrow(SHOP_NAME)));
                shop.setOrderIndex(cursor.getInt(cursor.getColumnIndexOrThrow(SHOP_ORDER_INDEX)));
                shop.setCollapsed(cursor.getInt(cursor.getColumnIndexOrThrow(SHOP_IS_COLLAPSED)) == 1);
                shops.add(shop);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return shops;
    }

    public boolean updateShop(Shop shop) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SHOP_NAME, shop.getName());
        values.put(SHOP_ORDER_INDEX, shop.getOrderIndex());
        values.put(SHOP_IS_COLLAPSED, shop.isCollapsed() ? 1 : 0);
        
        int result = db.update(TABLE_SHOPS, values, SHOP_ID + "=?", 
                              new String[]{String.valueOf(shop.getId())});
        return result > 0;
    }

    public boolean deleteShop(long shopId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SHOPS, SHOP_ID + "=?", 
                              new String[]{String.valueOf(shopId)});
        return result > 0;
    }

    public void reorderShops(List<Long> newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < newOrder.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(SHOP_ORDER_INDEX, i);
                db.update(TABLE_SHOPS, values, SHOP_ID + "=?", 
                         new String[]{String.valueOf(newOrder.get(i))});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateShopCollapsedState(long shopId, boolean isCollapsed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SHOP_IS_COLLAPSED, isCollapsed ? 1 : 0);
        
        int result = db.update(TABLE_SHOPS, values, SHOP_ID + "=?", 
                              new String[]{String.valueOf(shopId)});
        return result > 0;
    }

    // Item CRUD operations
    public long addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, item.getName());
        values.put(ITEM_SHOP_ID, item.getShopId());
        values.put(ITEM_ORDER_INDEX, item.getOrderIndex());
        
        long id = db.insert(TABLE_ITEMS, null, values);
        item.setId(id);
        return id;
    }

    public List<Item> getItemsForShop(long shopId) {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ITEMS, null, ITEM_SHOP_ID + "=?", 
                                new String[]{String.valueOf(shopId)}, null, null, ITEM_ORDER_INDEX + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ITEM_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(ITEM_NAME)));
                item.setShopId(cursor.getLong(cursor.getColumnIndexOrThrow(ITEM_SHOP_ID)));
                item.setOrderIndex(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_ORDER_INDEX)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_ITEMS, null, null, null, null, null, ITEM_NAME + " ASC");
        
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ITEM_ID)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(ITEM_NAME)));
                item.setShopId(cursor.getLong(cursor.getColumnIndexOrThrow(ITEM_SHOP_ID)));
                item.setOrderIndex(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_ORDER_INDEX)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public boolean updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ITEM_NAME, item.getName());
        values.put(ITEM_SHOP_ID, item.getShopId());
        values.put(ITEM_ORDER_INDEX, item.getOrderIndex());
        
        int result = db.update(TABLE_ITEMS, values, ITEM_ID + "=?", 
                              new String[]{String.valueOf(item.getId())});
        return result > 0;
    }

    public boolean deleteItem(long itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_ITEMS, ITEM_ID + "=?", 
                              new String[]{String.valueOf(itemId)});
        return result > 0;
    }

    public void reorderItems(long shopId, List<Long> newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < newOrder.size(); i++) {
                ContentValues values = new ContentValues();
                values.put(ITEM_ORDER_INDEX, i);
                db.update(TABLE_ITEMS, values, ITEM_ID + "=?", 
                         new String[]{String.valueOf(newOrder.get(i))});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // Shopping List CRUD operations
    public long addToShoppingList(ShoppingListItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (item.getItemId() != null) {
            values.put(SL_ITEM_ID, item.getItemId());
        }
        values.put(SL_NAME, item.getName());
        values.put(SL_SHOP_ID, item.getShopId());
        values.put(SL_IS_CHECKED, item.isChecked() ? 1 : 0);
        values.put(SL_ORDER_INDEX, item.getOrderIndex());
        values.put(SL_IS_AD_HOC, item.isAdHoc() ? 1 : 0);
        values.put(SL_QUANTITY, item.getQuantity());
        
        long id = db.insert(TABLE_SHOPPING_LIST, null, values);
        item.setId(id);
        return id;
    }

    public List<ShoppingListItem> getShoppingListForShop(long shopId, boolean dropTickedToBottom) {
        List<ShoppingListItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Use a raw query with a LEFT JOIN to fetch and sort items efficiently in one go.
        // Sorting logic:
        // 1. If dropTickedToBottom is true: Sort by checked state (unchecked first)
        // 2. Regular items (is_ad_hoc = 0) appear before ad-hoc items (is_ad_hoc = 1).
        // 3. Regular items are sorted by their predefined order_index from the items table.
        // 4. Ad-hoc items are sorted by their name.
        String orderByClause;
        if (dropTickedToBottom) {
            orderByClause = " ORDER BY sl." + SL_IS_CHECKED + " ASC, sl." + SL_IS_AD_HOC + " ASC, i." + ITEM_ORDER_INDEX + " ASC, sl." + SL_NAME + " ASC";
        } else {
            orderByClause = " ORDER BY sl." + SL_IS_AD_HOC + " ASC, i." + ITEM_ORDER_INDEX + " ASC, sl." + SL_NAME + " ASC";
        }

        String query = "SELECT sl." + SL_ID + ", sl." + SL_ITEM_ID + ", sl." + SL_NAME + ", sl." + SL_SHOP_ID +
                ", sl." + SL_IS_CHECKED + ", sl." + SL_ORDER_INDEX + ", sl." + SL_IS_AD_HOC +
                ", sl." + SL_QUANTITY +
                " FROM " + TABLE_SHOPPING_LIST + " sl" +
                " LEFT JOIN " + TABLE_ITEMS + " i ON sl." + SL_ITEM_ID + " = i." + ITEM_ID +
                " WHERE sl." + SL_SHOP_ID + " = ?" +
                orderByClause;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(shopId)});

        if (cursor.moveToFirst()) {
            do {
                ShoppingListItem item = new ShoppingListItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(SL_ID)));

                int itemIdIndex = cursor.getColumnIndexOrThrow(SL_ITEM_ID);
                if (!cursor.isNull(itemIdIndex)) {
                    item.setItemId(cursor.getLong(itemIdIndex));
                }

                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(SL_NAME)));
                item.setShopId(cursor.getLong(cursor.getColumnIndexOrThrow(SL_SHOP_ID)));
                item.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(SL_IS_CHECKED)) == 1);
                item.setOrderIndex(cursor.getInt(cursor.getColumnIndexOrThrow(SL_ORDER_INDEX)));
                item.setAdHoc(cursor.getInt(cursor.getColumnIndexOrThrow(SL_IS_AD_HOC)) == 1);
                item.setQuantity(cursor.getString(cursor.getColumnIndexOrThrow(SL_QUANTITY)));
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return items;
    }

    public boolean updateShoppingListItem(ShoppingListItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (item.getItemId() != null) {
            values.put(SL_ITEM_ID, item.getItemId());
        }
        values.put(SL_NAME, item.getName());
        values.put(SL_SHOP_ID, item.getShopId());
        values.put(SL_IS_CHECKED, item.isChecked() ? 1 : 0);
        values.put(SL_ORDER_INDEX, item.getOrderIndex());
        values.put(SL_IS_AD_HOC, item.isAdHoc() ? 1 : 0);
        values.put(SL_QUANTITY, item.getQuantity());
        
        int result = db.update(TABLE_SHOPPING_LIST, values, SL_ID + "=?", 
                              new String[]{String.valueOf(item.getId())});
        return result > 0;
    }

    public boolean removeFromShoppingList(long shoppingListItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SHOPPING_LIST, SL_ID + "=?", 
                              new String[]{String.valueOf(shoppingListItemId)});
        return result > 0;
    }

    public boolean toggleItemCheck(long shoppingListItemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // First get the current state
        Cursor cursor = db.query(TABLE_SHOPPING_LIST, new String[]{SL_IS_CHECKED}, 
                                SL_ID + "=?", new String[]{String.valueOf(shoppingListItemId)}, 
                                null, null, null);
        
        if (cursor.moveToFirst()) {
            boolean currentState = cursor.getInt(0) == 1;
            cursor.close();
            
            ContentValues values = new ContentValues();
            values.put(SL_IS_CHECKED, currentState ? 0 : 1);
            
            int result = db.update(TABLE_SHOPPING_LIST, values, SL_ID + "=?", 
                                  new String[]{String.valueOf(shoppingListItemId)});
            return result > 0;
        }
        
        cursor.close();
        return false;
    }

    public void uncheckAllItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SL_IS_CHECKED, 0);
        db.update(TABLE_SHOPPING_LIST, values, SL_IS_CHECKED + "=?", new String[]{"1"});
    }

    public void uncheckItemsForShop(long shopId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SL_IS_CHECKED, 0);
        db.update(TABLE_SHOPPING_LIST, values, SL_SHOP_ID + "=? AND " + SL_IS_CHECKED + "=?", 
                 new String[]{String.valueOf(shopId), "1"});
    }

    public void removeCheckedItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SHOPPING_LIST, SL_IS_CHECKED + "=?", new String[]{"1"});
    }

    public boolean isItemOnShoppingList(long itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SHOPPING_LIST, new String[]{SL_ID}, 
                                SL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)}, 
                                null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getItemCountForShop(long shopId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, new String[]{"COUNT(*)"}, 
                                ITEM_SHOP_ID + "=?", new String[]{String.valueOf(shopId)}, 
                                null, null, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_SHOPPING_LIST, null, null);
            db.delete(TABLE_ITEMS, null, null);
            db.delete(TABLE_SHOPS, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
