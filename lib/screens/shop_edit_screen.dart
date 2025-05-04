import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/shop.dart';
import '../services/storage_service.dart';

class ShopEditScreen extends StatelessWidget {
  const ShopEditScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Edit Shops'),
      ),
      body: Consumer<StorageService>(
        builder: (context, storage, child) {
          final shops = storage.shops;
          
          return ListView.builder(
            itemCount: shops.length,
            itemBuilder: (context, index) {
              final shop = shops[index];
              return Card(
                margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                child: ListTile(
                  title: Text(shop.name),
                  subtitle: Text('${storage.getItemsForShop(shop.id).length} items'),
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