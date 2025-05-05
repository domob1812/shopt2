import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/shop.dart';
import '../services/storage_service.dart';

class ShopEditScreen extends StatelessWidget {
  const ShopEditScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final storage = Provider.of<StorageService>(context);
    final shops = storage.shops;
    
    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Shops'),
        bottom: shops.length > 1 ? PreferredSize(
          preferredSize: const Size.fromHeight(30),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Text(
              'Drag shops to change their order',
              style: TextStyle(color: Colors.white70, fontSize: 14),
            ),
          ),
        ) : null,
      ),
      body: Consumer<StorageService>(
        builder: (context, storage, child) {
          final shops = storage.shops;
          
          return ReorderableListView.builder(
            buildDefaultDragHandles: false,
            itemCount: shops.length,
            onReorder: (oldIndex, newIndex) {
              // Handle reordering logic
              if (oldIndex < newIndex) {
                newIndex -= 1;
              }
              
              // Create a new order list with the moved item at the new index
              final List<String> newOrder = shops.map((s) => s.id).toList();
              final String movedId = newOrder.removeAt(oldIndex);
              newOrder.insert(newIndex, movedId);
              
              // Update the storage with the new order
              storage.reorderShops(newOrder);
            },
            itemBuilder: (context, index) {
              final shop = shops[index];
              return Card(
                key: Key(shop.id),
                margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                child: ListTile(
                  title: Text(shop.name),
                  subtitle: Text('${storage.getItemsForShop(shop.id).length} items'),
                  leading: MouseRegion(
                    cursor: SystemMouseCursors.click,
                    child: ReorderableDragStartListener(
                      index: index,
                      child: const Icon(Icons.drag_handle),
                    ),
                  ),
                  trailing: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.edit),
                        onPressed: () {
                          _showEditShopDialog(context, shop);
                        },
                      ),
                      IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () {
                          _confirmDeleteShop(context, shop);
                        },
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          _showAddShopDialog(context);
        },
        child: const Icon(Icons.add),
      ),
    );
  }
  
  void _showAddShopDialog(BuildContext context) {
    String shopName = '';
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Add Shop'),
          content: TextField(
            autofocus: true,
            decoration: const InputDecoration(labelText: 'Shop Name'),
            onChanged: (value) {
              shopName = value;
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
                if (shopName.trim().isNotEmpty) {
                  final newShop = Shop(
                    id: DateTime.now().toString(),
                    name: shopName.trim(),
                  );
                  Provider.of<StorageService>(context, listen: false).addShop(newShop);
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
  
  void _showEditShopDialog(BuildContext context, Shop shop) {
    String shopName = shop.name;
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Edit Shop'),
          content: TextField(
            autofocus: true,
            decoration: const InputDecoration(labelText: 'Shop Name'),
            controller: TextEditingController(text: shopName),
            onChanged: (value) {
              shopName = value;
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
                if (shopName.trim().isNotEmpty) {
                  final updatedShop = shop.copyWith(name: shopName.trim());
                  Provider.of<StorageService>(context, listen: false).updateShop(updatedShop);
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
  
  void _confirmDeleteShop(BuildContext context, Shop shop) {
    final storage = Provider.of<StorageService>(context, listen: false);
    final itemCount = storage.getItemsForShop(shop.id).length;
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Delete Shop'),
          content: Text(
            'Are you sure you want to delete "${shop.name}"? '
            'This will also delete all ${itemCount} items in this shop.'
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
                storage.deleteShop(shop.id);
                Navigator.pop(context);
              },
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );
  }
}