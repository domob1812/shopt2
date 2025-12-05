package eu.domob.shopt2;

import android.content.Context;
import android.net.Uri;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.Item;
import eu.domob.shopt2.data.Shop;
import eu.domob.shopt2.data.ShoppingListItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImportHelper {

    private static final int EXPECTED_JSON_VERSION = 1;
    private static final String EXPECTED_FORMAT = "shopt-backup";

    private final Context context;
    private final DatabaseHelper databaseHelper;

    public ImportHelper(Context context) {
        this.context = context;
        this.databaseHelper = DatabaseHelper.getInstance(context);
    }

    public void validateBackup(Uri uri) throws IOException, JSONException, InvalidBackupException {
        String json = readFromUri(uri);
        JSONObject root = new JSONObject(json);
        validateFormat(root);
    }

    public void importFromUri(Uri uri) throws IOException, JSONException, InvalidBackupException {
        String json = readFromUri(uri);
        JSONObject root = new JSONObject(json);

        validateFormat(root);

        List<ImportShop> importShops = parseShops(root);
        replaceData(importShops);
    }

    private String readFromUri(Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open file");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        inputStream.close();

        return stringBuilder.toString();
    }

    private void validateFormat(JSONObject root) throws JSONException, InvalidBackupException {
        if (!root.has("format") || !EXPECTED_FORMAT.equals(root.getString("format"))) {
            throw new InvalidBackupException();
        }

        if (!root.has("version") || root.getInt("version") != EXPECTED_JSON_VERSION) {
            throw new InvalidBackupException();
        }

        if (!root.has("shops")) {
            throw new InvalidBackupException();
        }
    }

    private List<ImportShop> parseShops(JSONObject root) throws JSONException {
        List<ImportShop> shops = new ArrayList<>();
        JSONArray shopsArray = root.getJSONArray("shops");

        for (int i = 0; i < shopsArray.length(); i++) {
            JSONObject shopObj = shopsArray.getJSONObject(i);
            ImportShop shop = new ImportShop();
            shop.name = shopObj.getString("name");
            shop.items = new ArrayList<>();

            if (shopObj.has("items")) {
                JSONArray itemsArray = shopObj.getJSONArray("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    JSONObject itemObj = itemsArray.getJSONObject(j);
                    ImportItem item = new ImportItem();
                    item.name = itemObj.getString("name");
                    item.isAdHoc = itemObj.optBoolean("adhoc", false);

                    if (itemObj.has("onlist")) {
                        item.onList = true;
                        JSONObject onlistObj = itemObj.getJSONObject("onlist");
                        item.quantity = onlistObj.optString("quantity", "");
                    }

                    shop.items.add(item);
                }
            }

            shops.add(shop);
        }

        return shops;
    }

    private void replaceData(List<ImportShop> importShops) {
        databaseHelper.clearAllData();

        for (int i = 0; i < importShops.size(); i++) {
            ImportShop importShop = importShops.get(i);

            Shop shop = new Shop();
            shop.setName(importShop.name);
            shop.setOrderIndex(i);
            long shopId = databaseHelper.addShop(shop);

            for (int j = 0; j < importShop.items.size(); j++) {
                ImportItem importItem = importShop.items.get(j);

                if (!importItem.isAdHoc) {
                    Item item = new Item();
                    item.setName(importItem.name);
                    item.setShopId(shopId);
                    item.setOrderIndex(j);
                    long itemId = databaseHelper.addItem(item);

                    if (importItem.onList) {
                        ShoppingListItem shoppingItem = ShoppingListItem.fromItem(item);
                        shoppingItem.setQuantity(importItem.quantity);
                        databaseHelper.addToShoppingList(shoppingItem);
                    }
                } else if (importItem.onList) {
                    ShoppingListItem shoppingItem = new ShoppingListItem(importItem.name, shopId, 999, true);
                    shoppingItem.setQuantity(importItem.quantity);
                    databaseHelper.addToShoppingList(shoppingItem);
                }
            }
        }
    }

    private static class ImportShop {
        String name;
        List<ImportItem> items;
    }

    private static class ImportItem {
        String name;
        boolean isAdHoc;
        boolean onList;
        String quantity = "";
    }

    public static class InvalidBackupException extends Exception {
    }
}
