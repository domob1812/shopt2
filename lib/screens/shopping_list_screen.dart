import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/shop.dart';
import '../models/item.dart';
import '../models/shopping_list_item.dart';
import '../services/storage_service.dart';
import 'shop_edit_screen.dart';
import 'items_edit_screen.dart';
import 'package:flutter/services.dart';

class ShoppingListScreen extends StatelessWidget {
  const ShoppingListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final TextEditingController itemNameController = TextEditingController();
    final FocusNode itemFocusNode = FocusNode();
    TextEditingController autocompleteController = TextEditingController();
    FocusNode autocompleteFocusNode = FocusNode();
    
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
      body: Column(
        children: [
          // Add item input bar at the top
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Row(
              children: [
                Expanded(
                  child: Consumer<StorageService>(
                    builder: (context, storage, child) {
                      // Get all configured items across all shops
                      List<Item> allItems = [];
                      for (var shop in storage.shops) {
                        allItems.addAll(storage.getItemsForShop(shop.id));
                      }
                      
                      // Sort items alphabetically for better user experience
                      allItems.sort((a, b) => a.name.toLowerCase().compareTo(b.name.toLowerCase()));
                      
                      // Deduplicate items by name (keeping only the first occurrence)
                      final itemNameSet = <String>{};
                      final uniqueNameItems = allItems.where((item) {
                        final isNew = !itemNameSet.contains(item.name.toLowerCase());
                        if (isNew) {
                          itemNameSet.add(item.name.toLowerCase());
                        }
                        return isNew;
                      }).toList();
                      
                      return Autocomplete<String>(
                        optionsBuilder: (TextEditingValue textEditingValue) {
                          if (textEditingValue.text.isEmpty) {
                            return const [];
                          }
                          return uniqueNameItems
                            .where((Item item) {
                              return item.name.toLowerCase().contains(
                                textEditingValue.text.toLowerCase(),
                              );
                            })
                            .map((item) => item.name)
                            .toList();
                        },
                        displayStringForOption: (String option) => option,
                        fieldViewBuilder: (context, controller, focusNode, onFieldSubmitted) {
                          // Store references to the autocomplete controller and focus node
                          // so we can access them in onSelected
                          autocompleteController = controller;
                          autocompleteFocusNode = focusNode;
                          
                          // Update the controllers to use the ones from Autocomplete
                          // This is needed to keep them in sync with the Autocomplete widget
                          itemNameController.text = controller.text;
                          controller.addListener(() {
                            itemNameController.text = controller.text;
                          });
                          
                          return TextField(
                            controller: controller,
                            focusNode: focusNode,
                            decoration: const InputDecoration(
                              hintText: 'Enter item name',
                              border: OutlineInputBorder(),
                              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                            onSubmitted: (value) {
                              if (value.trim().isNotEmpty) {
                                _handleItemAddition(context, value.trim());
                                controller.clear();
                                onFieldSubmitted();
                              }
                            },
                          );
                        },
                        onSelected: (String selectedItemName) {
                          // When an option is selected from the dropdown, use the same logic as the add button
                          _handleItemAddition(context, selectedItemName);
                          // Clear both controllers to ensure the input box is empty
                          itemNameController.clear();
                          autocompleteController.clear();  // Clear the autocomplete's controller
                          // Hide keyboard when an item is selected
                          FocusScope.of(context).unfocus();
                        },
                        optionsViewBuilder: (context, onSelected, options) {
                          return Align(
                            alignment: Alignment.topLeft,
                            child: Material(
                              elevation: 4.0,
                              child: ConstrainedBox(
                                constraints: const BoxConstraints(maxHeight: 200),
                                child: ListView.builder(
                                  padding: const EdgeInsets.all(8.0),
                                  shrinkWrap: true,
                                  itemCount: options.length,
                                  itemBuilder: (BuildContext context, int index) {
                                    final String itemName = options.elementAt(index);
                                    return ListTile(
                                      title: Text(itemName),
                                      onTap: () {
                                        onSelected(itemName);
                                      },
                                    );
                                  },
                                ),
                              ),
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.add_circle),
                  color: Theme.of(context).primaryColor,
                  onPressed: () {
                    if (itemNameController.text.trim().isNotEmpty) {
                      _handleItemAddition(context, itemNameController.text.trim());
                      itemNameController.clear();
                      autocompleteController.clear();  // Also clear the autocomplete controller
                      // Hide keyboard
                      FocusScope.of(context).unfocus();
                    }
                  },
                ),
              ],
            ),
          ),
          // Main content
          Expanded(
            child: Consumer<StorageService>(
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
                          if (shoppingListItems.isEmpty)
                            Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Center(
                                child: Text(
                                  'No items in this shop',
                                  style: TextStyle(fontStyle: FontStyle.italic, color: Colors.grey),
                                ),
                              ),
                            )
                          else
                            ...shoppingListItems.map((item) => _buildShoppingItem(context, item)).toList(),
                        ],
                      ),
                    );
                  },
                );
              },
            ),
          ),
        ],
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
      title: InkWell(
        onTap: () {
          // Toggle checkbox when tapping on item name
          Provider.of<StorageService>(context, listen: false).toggleItemCheck(item.id);
        },
        child: Row(
          children: [
            Expanded(
              child: Text(
                item.name,
                style: TextStyle(
                  decoration: item.isChecked ? TextDecoration.lineThrough : null,
                  color: item.isChecked ? Colors.grey : Colors.black,
                ),
              ),
            ),
            if (item.quantity.isNotEmpty)
              InkWell(
                onTap: () => _showQuantityEditDialog(context, item),
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 8.0),
                  child: Text(
                    item.quantity,
                    style: TextStyle(
                      color: Colors.blue[700],
                      fontWeight: FontWeight.bold,
                      decoration: item.isChecked ? TextDecoration.lineThrough : null,
                    ),
                  ),
                ),
              )
            else
              IconButton(
                onPressed: () => _showQuantityEditDialog(context, item),
                icon: const Icon(Icons.add_box_outlined, color: Colors.blue),
                tooltip: 'Add quantity',
                constraints: const BoxConstraints(),
                padding: const EdgeInsets.all(4.0),
                splashRadius: 24.0,
              ),
          ],
        ),
      ),
      // No subtitle needed for ad-hoc items anymore
      subtitle: null,
    );
  }
  
  void _showQuantityEditDialog(BuildContext context, ShoppingListItem item) {
    String quantity = item.quantity;

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Edit Quantity'),
          content: TextField(
            decoration: const InputDecoration(
              labelText: 'Quantity',
              hintText: 'e.g., 2 kg, 3 bottles, 500g, etc.',
            ),
            controller: TextEditingController(text: quantity),
            onChanged: (value) {
              quantity = value;
            },
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
                Provider.of<StorageService>(context, listen: false)
                    .updateShoppingListItem(item.copyWith(quantity: quantity.trim()));
                Navigator.pop(context);
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );
  }

  void _handleItemAddition(BuildContext context, String itemName) {
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
    
    // Find all items across all shops that match the entered name
    List<Item> matchingItems = [];
    for (var shop in shops) {
      final shopItems = storage.getItemsForShop(shop.id);
      final matches = shopItems.where(
        (item) => item.name.toLowerCase() == itemName.toLowerCase()
      ).toList();
      matchingItems.addAll(matches);
    }
    
    // Case 1: Exact match found in exactly one shop
    if (matchingItems.length == 1) {
      storage.addConfiguredItemToShoppingList(matchingItems.first);
      return;
    }
    
    // Case 2: Multiple matching items OR no matches
    // Show dialog to select which shop to add the item to
    _showShopSelectionDialog(context, itemName, matchingItems);
  }
  
  void _showShopSelectionDialog(BuildContext context, String itemName, List<Item> matchingItems) {
    final storage = Provider.of<StorageService>(context, listen: false);
    final shops = storage.shops;
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Select Shop'),
          content: Container(
            width: double.maxFinite,
            child: ListView.builder(
              shrinkWrap: true,
              itemCount: shops.length,
              itemBuilder: (context, index) {
                final shop = shops[index];
                // Check if this shop has a matching item
                final hasMatchingItem = matchingItems.any((item) => item.shopId == shop.id);
                
                return ListTile(
                  title: Text(shop.name),
                  onTap: () {
                    // If we have a matching configured item, use it
                    if (hasMatchingItem) {
                      final matchingItem = matchingItems.firstWhere((item) => item.shopId == shop.id);
                      storage.addConfiguredItemToShoppingList(matchingItem);
                    } else {
                      // Otherwise add as ad-hoc item
                      storage.addAdHocItemToShoppingList(itemName, shop.id);
                    }
                    Navigator.pop(context);
                  },
                );
              },
            ),
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
              },
              child: const Text('Cancel'),
            ),
          ],
        );
      },
    );
  }
}