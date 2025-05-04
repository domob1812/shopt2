import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:reorderables/reorderables.dart';
import '../models/shop.dart';
import '../models/item.dart';
import '../services/storage_service.dart';

class ItemsEditScreen extends StatefulWidget {
  final String shopId;
  
  const ItemsEditScreen({required this.shopId, super.key});

  @override
  State<ItemsEditScreen> createState() => _ItemsEditScreenState();
}

class _ItemsEditScreenState extends State<ItemsEditScreen> {
  @override
  Widget build(BuildContext context) {
    return Consumer<StorageService>(
      builder: (context, storage, child) {
        final shop = storage.shops.firstWhere(
          (s) => s.id == widget.shopId,
          orElse: () => Shop(id: '', name: 'Unknown Shop'),
        );
        
        if (shop.id.isEmpty) {
          return Scaffold(
            appBar: AppBar(title: const Text('Shop Not Found')),
            body: const Center(child: Text('The requested shop could not be found')),
          );
        }
        
        final items = storage.getItemsForShop(shop.id);
        
        return Scaffold(
          appBar: AppBar(
            title: Text('Edit ${shop.name} Items'),
          ),
          body: Column(
            children: [
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Text(
                  'Drag items to reorder them as they appear in the shop',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.grey[700],
                  ),
                ),
              ),
              Expanded(
                child: items.isEmpty
                    ? const Center(child: Text('No items in this shop yet'))
                    : ReorderableListView.builder(
                        itemCount: items.length,
                        onReorder: (oldIndex, newIndex) {
                          if (oldIndex < newIndex) {
                            newIndex -= 1;
                          }
                          
                          // Create the new order
                          final List<String> newOrder = List.from(shop.itemIds);
                          final String itemId = newOrder.removeAt(oldIndex);
                          newOrder.insert(newIndex, itemId);
                          
                          // Update storage with new order
                          storage.reorderItems(shop.id, newOrder);
                        },
                        itemBuilder: (context, index) {
                          final item = items[index];
                          // Check if this item is on the shopping list
                          final bool isOnShoppingList = storage.isItemOnShoppingList(item.id);
                          
                          return ListTile(
                            key: ValueKey(item.id),
                            leading: const Icon(Icons.drag_handle),
                            title: Text(item.name),
                            subtitle: isOnShoppingList 
                                ? const Text('On shopping list', style: TextStyle(fontSize: 12, color: Colors.green)) 
                                : null,
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                // Add to shopping list button
                                if (!isOnShoppingList)
                                  IconButton(
                                    icon: const Icon(Icons.add_shopping_cart),
                                    onPressed: () {
                                      storage.addConfiguredItemToShoppingList(item);
                                      ScaffoldMessenger.of(context).showSnackBar(
                                        SnackBar(
                                          content: Text('${item.name} added to shopping list'),
                                          duration: const Duration(seconds: 2),
                                        ),
                                      );
                                    },
                                    tooltip: 'Add to shopping list',
                                  ),
                                IconButton(
                                  icon: const Icon(Icons.edit),
                                  onPressed: () {
                                    _showEditItemDialog(context, item);
                                  },
                                ),
                                IconButton(
                                  icon: const Icon(Icons.delete),
                                  onPressed: () {
                                    storage.deleteItem(item.id);
                                  },
                                ),
                              ],
                            ),
                          );
                        },
                      ),
              ),
            ],
          ),
          floatingActionButton: FloatingActionButton(
            onPressed: () {
              _showAddItemDialog(context, shop);
            },
            child: const Icon(Icons.add),
          ),
        );
      },
    );
  }
  
  void _showAddItemDialog(BuildContext context, Shop shop) {
    String itemName = '';
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Add Item'),
          content: TextField(
            autofocus: true,
            decoration: const InputDecoration(labelText: 'Item Name'),
            onChanged: (value) {
              itemName = value;
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
                if (itemName.trim().isNotEmpty) {
                  final storage = Provider.of<StorageService>(context, listen: false);
                  final newItem = Item(
                    id: DateTime.now().toString(),
                    name: itemName.trim(),
                    shopId: shop.id,
                    orderIndex: shop.itemIds.length, // Add to end
                  );
                  storage.addItem(newItem);
                  Navigator.pop(context);
                }
              },
              child: const Text('Add'),
            ),
          ],
        );
      },
    );
  }
  
  void _showEditItemDialog(BuildContext context, Item item) {
    String itemName = item.name;
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Edit Item'),
          content: TextField(
            autofocus: true,
            decoration: const InputDecoration(labelText: 'Item Name'),
            controller: TextEditingController(text: itemName),
            onChanged: (value) {
              itemName = value;
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
                if (itemName.trim().isNotEmpty) {
                  final updatedItem = item.copyWith(name: itemName.trim());
                  Provider.of<StorageService>(context, listen: false).updateItem(updatedItem);
                  Navigator.pop(context);
                }
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );
  }
}