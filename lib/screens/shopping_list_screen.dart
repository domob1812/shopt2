import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/shop.dart';
import '../models/item.dart';
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
              final shopItems = storage.getItemsForShop(shop.id);
              
              if (shopItems.isEmpty) {
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
                    ...shopItems.map((item) => _buildShoppingItem(context, item)).toList(),
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
  
  Widget _buildShoppingItem(BuildContext context, Item item) {
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
      trailing: IconButton(
        icon: const Icon(Icons.delete),
        onPressed: () {
          Provider.of<StorageService>(context, listen: false).deleteItem(item.id);
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
    
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Add Item'),
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
                  selectedShopId = value!;
                },
              ),
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
                if (itemName.trim().isNotEmpty) {
                  final shop = shops.firstWhere((s) => s.id == selectedShopId);
                  final newItem = Item(
                    id: DateTime.now().toString(),
                    name: itemName.trim(),
                    shopId: selectedShopId,
                    orderIndex: shop.itemIds.length,
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
}