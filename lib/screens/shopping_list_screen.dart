import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/shop.dart';
import '../models/item.dart';
import '../models/shopping_list_item.dart';
import '../services/storage_service.dart';
import 'shop_edit_screen.dart';
import 'items_edit_screen.dart';

class ShoppingListScreen extends StatelessWidget {
  const ShoppingListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Shopping List'),
        actions: [
          IconButton(
            icon: const Icon(Icons.store),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const ShopEditScreen()),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.delete_sweep),
            onPressed: () {
              Provider.of<StorageService>(context, listen: false).removeCheckedItems();
            },
          ),
        ],
      ),
      body: Consumer<StorageService>(
        builder: (context, storage, child) {
          final shops = storage.shops;
          
          if (shops.isEmpty) {
            return const Center(
              child: Text('No shops added yet! Tap the shop icon to add one.'),
            );
          }
          
          return ListView.builder(
            itemCount: shops.length,
            itemBuilder: (context, index) {
              final shop = shops[index];
              final shoppingListItems = storage.getShoppingListForShop(shop.id);
              
              if (shoppingListItems.isEmpty) {
                return Container(); // Skip empty shops
              }
              
              return Card(
                margin: const EdgeInsets.all(8.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            shop.name,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          IconButton(
                            icon: const Icon(Icons.edit),
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => ItemsEditScreen(shopId: shop.id),
                                ),
                              );
                            },
                          ),
                        ],
                      ),
                    ),
                    const Divider(),
                    ...shoppingListItems.map((item) => _buildShoppingItem(context, item)).toList(),
                  ],
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _showAddItemDialog(context);
        },
        child: const Icon(Icons.add),
      ),
    );
  }
  
  Widget _buildShoppingItem(BuildContext context, ShoppingListItem item) {
    return ListTile(
      leading: Checkbox(
        value: item.isChecked,
        onChanged: (bool? value) {
          Provider.of<StorageService>(context, listen: false).toggleItemCheck(item.id);
        },
      ),
      title: Text(
        item.name,
        style: TextStyle(
          decoration: item.isChecked ? TextDecoration.lineThrough : null,
          color: item.isChecked ? Colors.grey : Colors.black,
        ),
      ),
      // Show a different indicator for ad-hoc items
      subtitle: item.isAdHoc ? const Text('Ad-hoc item', style: TextStyle(fontSize: 12, fontStyle: FontStyle.italic)) : null,
      trailing: IconButton(
        icon: const Icon(Icons.delete),
        onPressed: () {
          Provider.of<StorageService>(context, listen: false).removeFromShoppingList(item.id);
        },
      ),
    );
  }
  
  void _showAddItemDialog(BuildContext context) {
    final storage = Provider.of<StorageService>(context, listen: false);
    final shops = storage.shops;
    
    if (shops.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please add a shop first'),
        ),
      );
      return;
    }
    
    String selectedShopId = shops.first.id;
    String itemName = '';
    bool useExistingItem = true; // Toggle between configured and ad-hoc items
    List<Item> configuredItems = [];
    Item? selectedItem;
    
    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            // Get available configured items for the selected shop that aren't already on the list
            configuredItems = storage.getItemsForShop(selectedShopId)
                .where((item) => !storage.isItemOnShoppingList(item.id))
                .toList();
            
            // Reset selected item if needed
            if (selectedItem == null || selectedItem!.shopId != selectedShopId) {
              selectedItem = configuredItems.isNotEmpty ? configuredItems.first : null;
            }
            
            return AlertDialog(
              title: const Text('Add Item to Shopping List'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  DropdownButtonFormField<String>(
                    value: selectedShopId,
                    decoration: const InputDecoration(labelText: 'Shop'),
                    items: shops.map((shop) {
                      return DropdownMenuItem<String>(
                        value: shop.id,
                        child: Text(shop.name),
                      );
                    }).toList(),
                    onChanged: (value) {
                      setState(() {
                        selectedShopId = value!;
                        selectedItem = null; // Reset selected item
                      });
                    },
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: Text('Item type:'),
                      ),
                      ChoiceChip(
                        label: Text('Configured'),
                        selected: useExistingItem,
                        onSelected: (selected) {
                          setState(() {
                            useExistingItem = selected;
                          });
                        },
                      ),
                      const SizedBox(width: 8),
                      ChoiceChip(
                        label: Text('Ad-hoc'),
                        selected: !useExistingItem,
                        onSelected: (selected) {
                          setState(() {
                            useExistingItem = !selected;
                          });
                        },
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  if (useExistingItem) 
                    // Show dropdown of available configured items
                    configuredItems.isEmpty
                      ? const Text('No available configured items', style: TextStyle(color: Colors.red))
                      : DropdownButtonFormField<String>(
                          value: selectedItem?.id,
                          decoration: const InputDecoration(labelText: 'Select Item'),
                          items: configuredItems.map((item) {
                            return DropdownMenuItem<String>(
                              value: item.id,
                              child: Text(item.name),
                            );
                          }).toList(),
                          onChanged: (value) {
                            setState(() {
                              selectedItem = configuredItems.firstWhere((item) => item.id == value);
                            });
                          },
                        )
                  else
                    // Show text field for ad-hoc item
                    TextField(
                      decoration: const InputDecoration(labelText: 'Item Name'),
                      onChanged: (value) {
                        itemName = value;
                      },
                    ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () {
                    Navigator.pop(context);
                  },
                  child: const Text('Cancel'),
                ),
                TextButton(
                  onPressed: () {
                    if (useExistingItem) {
                      if (selectedItem != null) {
                        storage.addConfiguredItemToShoppingList(selectedItem!);
                        Navigator.pop(context);
                      }
                    } else {
                      if (itemName.trim().isNotEmpty) {
                        storage.addAdHocItemToShoppingList(itemName.trim(), selectedShopId);
                        Navigator.pop(context);
                      }
                    }
                  },
                  child: const Text('Add'),
                ),
              ],
            );
          },
        );
      },
    );
  }
}