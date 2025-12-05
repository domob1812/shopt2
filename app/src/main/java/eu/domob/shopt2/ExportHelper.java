package eu.domob.shopt2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Item;
import eu.domob.shopt2.data.Shop;
import eu.domob.shopt2.data.ShoppingListItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExportHelper {

    private static final int JSON_FORMAT_VERSION = 1;

    private final Context context;
    private final DatabaseHelper databaseHelper;

    public ExportHelper(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    public String generateJson() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("format", "shopt-backup");
        root.put("version", JSON_FORMAT_VERSION);

        JSONArray shopsArray = new JSONArray();
        List<Shop> shops = databaseHelper.getAllShops();

        for (Shop shop : shops) {
            JSONObject shopObj = new JSONObject();
            shopObj.put("name", shop.getName());

            JSONArray itemsArray = new JSONArray();
            List<Item> items = databaseHelper.getItemsForShop(shop.getId());

            for (Item item : items) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("name", item.getName());

                ShoppingListItem shoppingItem = findShoppingListItem(shop.getId(), item.getId());
                if (shoppingItem != null) {
                    JSONObject onlistObj = new JSONObject();
                    String quantity = shoppingItem.getQuantity();
                    if (quantity != null && !quantity.isEmpty()) {
                        onlistObj.put("quantity", quantity);
                    }
                    itemObj.put("onlist", onlistObj);
                }

                itemsArray.put(itemObj);
            }

            List<ShoppingListItem> adHocItems = getAdHocItemsForShop(shop.getId());
            for (ShoppingListItem adHocItem : adHocItems) {
                JSONObject itemObj = new JSONObject();
                itemObj.put("name", adHocItem.getName());
                itemObj.put("adhoc", true);

                JSONObject onlistObj = new JSONObject();
                String quantity = adHocItem.getQuantity();
                if (quantity != null && !quantity.isEmpty()) {
                    onlistObj.put("quantity", quantity);
                }
                itemObj.put("onlist", onlistObj);

                itemsArray.put(itemObj);
            }

            shopObj.put("items", itemsArray);
            shopsArray.put(shopObj);
        }

        root.put("shops", shopsArray);
        return root.toString(2);
    }

    private ShoppingListItem findShoppingListItem(long shopId, long itemId) {
        List<ShoppingListItem> items = databaseHelper.getShoppingListForShop(shopId, false);
        for (ShoppingListItem item : items) {
            if (!item.isAdHoc() && item.getItemId() != null && item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    private List<ShoppingListItem> getAdHocItemsForShop(long shopId) {
        List<ShoppingListItem> allItems = databaseHelper.getShoppingListForShop(shopId, false);
        List<ShoppingListItem> adHocItems = new java.util.ArrayList<>();
        for (ShoppingListItem item : allItems) {
            if (item.isAdHoc()) {
                adHocItems.add(item);
            }
        }
        return adHocItems;
    }

    public Uri saveToFile() throws IOException, JSONException {
        String json = generateJson();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String filename = "shopt-backup-" + timestamp + ".json";

        File cacheDir = context.getCacheDir();
        File file = new File(cacheDir, filename);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(json.getBytes());
        fos.close();

        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public Intent createSaveIntent() throws IOException, JSONException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String filename = "shopt-backup-" + timestamp + ".json";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, filename);
        return intent;
    }

    public void writeToUri(Uri uri) throws IOException, JSONException {
        String json = generateJson();
        FileOutputStream fos = (FileOutputStream) context.getContentResolver().openOutputStream(uri);
        if (fos != null) {
            fos.write(json.getBytes());
            fos.close();
        }
    }

    public Intent createShareIntent() throws IOException, JSONException {
        Uri uri = saveToFile();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return Intent.createChooser(shareIntent, null);
    }
}
