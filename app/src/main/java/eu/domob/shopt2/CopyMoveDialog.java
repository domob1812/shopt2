package eu.domob.shopt2;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import eu.domob.shopt2.data.DatabaseHelper;
import eu.domob.shopt2.data.ItemTransfer;
import eu.domob.shopt2.data.Shop;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared dialog to copy or move a set of selected items to another shop. Used both by the
 * "edit items for a shop" screen and the main shopping list screen.
 */
public final class CopyMoveDialog {

    public interface OnDoneListener {
        void onDone();
    }

    private CopyMoveDialog() {}

    public static void show(Context context, List<ItemTransfer> items, List<Shop> allShops,
                            OnDoneListener listener) {
        if (items == null || items.isEmpty()) {
            Toast.makeText(context, R.string.no_items_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Exclude every shop that is a source of one of the selected items.
        Set<Long> sourceShopIds = new HashSet<>();
        for (ItemTransfer transfer : items) {
            sourceShopIds.add(transfer.getSourceShopId());
        }

        List<Shop> targetShops = new ArrayList<>();
        for (Shop shop : allShops) {
            if (!sourceShopIds.contains(shop.getId())) {
                targetShops.add(shop);
            }
        }

        if (targetShops.isEmpty()) {
            Toast.makeText(context, R.string.no_target_shop, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_copy_move, null);
        MaterialAutoCompleteTextView actvTargetShop = dialogView.findViewById(R.id.actvTargetShop);
        CheckBox cbCopy = dialogView.findViewById(R.id.cbCopy);

        List<String> shopNames = new ArrayList<>();
        for (Shop shop : targetShops) {
            shopNames.add(shop.getName());
        }
        actvTargetShop.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, shopNames));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.copy_or_move)
                .setView(dialogView)
                .setPositiveButton(R.string.apply, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    int position = shopNames.indexOf(actvTargetShop.getText().toString());
                    if (position < 0) {
                        Toast.makeText(context, R.string.select_shop, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Shop targetShop = targetShops.get(position);
                    boolean move = !cbCopy.isChecked();

                    DatabaseHelper.getInstance(context)
                            .moveOrCopyItems(items, targetShop.getId(), move);

                    dialog.dismiss();

                    if (listener != null) {
                        listener.onDone();
                    }

                    int count = items.size();
                    Toast.makeText(context, context.getResources().getQuantityString(
                                    move ? R.plurals.items_moved : R.plurals.items_copied, count, count),
                            Toast.LENGTH_SHORT).show();
                }));

        dialog.show();
    }
}
