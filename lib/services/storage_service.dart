import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/shop.dart';
import '../models/item.dart';
import '../models/shopping_list_item.dart';

class StorageService extends ChangeNotifier {
  late SharedPreferences _prefs;
  List<Shop> _shops = [];
  List<Item> _items = []; // Configured items
  List<ShoppingListItem> _shoppingList = []; // Items on the shopping list
  
  List<Shop> get shops => _shops;
  List<Item> get items => _items;
  List<ShoppingListItem> get shoppingList => _shoppingList;
  
  Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
    _loadData();
    
    // Add demo data if first run
    if (_shops.isEmpty) {
      _addDemoData();
    }
  }
  
  void _loadData() {
    final shopsJson = _prefs.getStringList('shops') ?? [];
    final itemsJson = _prefs.getStringList('items') ?? [];
    final shoppingListJson = _prefs.getStringList('shoppingList') ?? [];
    
    _shops = shopsJson.map((s) => Shop.fromJson(jsonDecode(s))).toList();
    _items = itemsJson.map((i) => Item.fromJson(jsonDecode(i))).toList();
    _shoppingList = shoppingListJson.map((i) => ShoppingListItem.fromJson(jsonDecode(i))).toList();
    notifyListeners();
  }
  
  Future<void> _saveData() async {
    final shopsJson = _shops.map((s) => jsonEncode(s.toJson())).toList();
    final itemsJson = _items.map((i) => jsonEncode(i.toJson())).toList();
    final shoppingListJson = _shoppingList.map((i) => jsonEncode(i.toJson())).toList();
    
    await _prefs.setStringList('shops', shopsJson);
    await _prefs.setStringList('items', itemsJson);
    await _prefs.setStringList('shoppingList', shoppingListJson);
    notifyListeners();
  }
  
  // Shop CRUD operations
  Future<void> addShop(Shop shop) async {
    _shops.add(shop);
    await _saveData();
  }
  
  Future<void> updateShop(Shop shop) async {
    final index = _shops.indexWhere((s) => s.id == shop.id);
    if (index >= 0) {
      _shops[index] = shop;
      await _saveData();
    }
  }
  
  Future<void> deleteShop(String shopId) async {
    _shops.removeWhere((s) => s.id == shopId);
    _items.removeWhere((i) => i.shopId == shopId);
    await _saveData();
  }
  
  // Item CRUD operations - for configured items
  Future<void> addItem(Item item) async {
    _items.add(item);
    final shopIndex = _shops.indexWhere((s) => s.id == item.shopId);
    if (shopIndex >= 0) {
      _shops[shopIndex].itemIds.add(item.id);
    }
    await _saveData();
  }
  
  Future<void> updateItem(Item item) async {
    final index = _items.indexWhere((i) => i.id == item.id);
    if (index >= 0) {
      _items[index] = item;
      await _saveData();
    }
  }
  
  Future<void> deleteItem(String itemId) async {
    final item = _items.firstWhere((i) => i.id == itemId, orElse: () => Item(id: "", name: "", shopId: ""));
    if (item.id.isNotEmpty) {
      final shopIndex = _shops.indexWhere((s) => s.id == item.shopId);
      if (shopIndex >= 0) {
        _shops[shopIndex].itemIds.remove(itemId);
      }
      
      // Also remove any references to this item from the shopping list
      _shoppingList.removeWhere((sli) => sli.itemId == itemId);
      
      _items.removeWhere((i) => i.id == itemId);
      await _saveData();
    }
  }
  
  // Shopping List Item CRUD operations
  Future<void> addToShoppingList(ShoppingListItem shoppingListItem) async {
    _shoppingList.add(shoppingListItem);
    await _saveData();
  }
  
  Future<void> updateShoppingListItem(ShoppingListItem shoppingListItem) async {
    final index = _shoppingList.indexWhere((i) => i.id == shoppingListItem.id);
    if (index >= 0) {
      _shoppingList[index] = shoppingListItem;
      await _saveData();
    }
  }
  
  Future<void> removeFromShoppingList(String shoppingListItemId) async {
    _shoppingList.removeWhere((i) => i.id == shoppingListItemId);
    await _saveData();
  }
  
  Future<void> toggleItemCheck(String shoppingListItemId) async {
    final index = _shoppingList.indexWhere((i) => i.id == shoppingListItemId);
    if (index >= 0) {
      _shoppingList[index] = _shoppingList[index].copyWith(isChecked: !_shoppingList[index].isChecked);
      await _saveData();
    }
  }
  
  Future<void> removeCheckedItems() async {
    _shoppingList.removeWhere((i) => i.isChecked);
    await _saveData();
  }
  
  Future<void> reorderItems(String shopId, List<String> newOrder) async {
    for (var i = 0; i < newOrder.length; i++) {
      final itemIndex = _items.indexWhere((item) => item.id == newOrder[i]);
      if (itemIndex >= 0) {
        _items[itemIndex] = _items[itemIndex].copyWith(orderIndex: i);
      }
    }
    
    final shopIndex = _shops.indexWhere((s) => s.id == shopId);
    if (shopIndex >= 0) {
      _shops[shopIndex] = _shops[shopIndex].copyWith(itemIds: newOrder);
    }
    
    await _saveData();
  }
  
  Future<void> reorderShoppingList(String shopId, List<String> newOrder) async {
    for (var i = 0; i < newOrder.length; i++) {
      final itemIndex = _shoppingList.indexWhere((item) => item.id == newOrder[i]);
      if (itemIndex >= 0) {
        _shoppingList[itemIndex] = _shoppingList[itemIndex].copyWith(orderIndex: i);
      }
    }
    await _saveData();
  }
  
  // Get configured items for a specific shop, ordered by orderIndex
  List<Item> getItemsForShop(String shopId) {
    final shopItems = _items.where((i) => i.shopId == shopId).toList();
    shopItems.sort((a, b) => a.orderIndex.compareTo(b.orderIndex));
    return shopItems;
  }
  
  // Get shopping list items for a specific shop
  List<ShoppingListItem> getShoppingListForShop(String shopId) {
    // First get all configured items
    final configuredItems = _shoppingList
        .where((i) => i.shopId == shopId && !i.isAdHoc)
        .toList();
    
    // Then get all ad-hoc items
    final adHocItems = _shoppingList
        .where((i) => i.shopId == shopId && i.isAdHoc)
        .toList();
    
    // Sort configured items by their orderIndex
    configuredItems.sort((a, b) {
      // If we have the corresponding Item objects, use their orderIndex
      final itemA = a.itemId != null ? _items.firstWhere((i) => i.id == a.itemId, orElse: () => Item(id: '', name: '', shopId: '', orderIndex: 999)) : null;
      final itemB = b.itemId != null ? _items.firstWhere((i) => i.id == b.itemId, orElse: () => Item(id: '', name: '', shopId: '', orderIndex: 999)) : null;
      
      if (itemA != null && itemB != null) {
        return itemA.orderIndex.compareTo(itemB.orderIndex);
      }
      return a.orderIndex.compareTo(b.orderIndex);
    });
    
    // Sort ad-hoc items by their orderIndex
    adHocItems.sort((a, b) => a.orderIndex.compareTo(b.orderIndex));
    
    // Combine the lists with ad-hoc items at the end
    return [...configuredItems, ...adHocItems];
  }
  
  // Add a configured item to the shopping list
  Future<void> addConfiguredItemToShoppingList(Item item) async {
    // Check if this item is already on the shopping list
    final existingItem = _shoppingList.any((i) => i.itemId == item.id);
    if (!existingItem) {
      final shoppingListItem = ShoppingListItem.fromItem(item);
      _shoppingList.add(shoppingListItem);
      await _saveData();
    }
  }
  
  // Add an ad-hoc item directly to the shopping list
  Future<void> addAdHocItemToShoppingList(String name, String shopId) async {
    final adHocItem = ShoppingListItem(
      id: DateTime.now().toString(),
      name: name,
      shopId: shopId,
      isAdHoc: true,
      orderIndex: 999, // Ad-hoc items go to the end
    );
    _shoppingList.add(adHocItem);
    await _saveData();
  }
  
  // Check if a configured item is on the shopping list
  bool isItemOnShoppingList(String itemId) {
    return _shoppingList.any((i) => i.itemId == itemId);
  }
  
  void _addDemoData() {
    // Demo shops
    final grocery = Shop(id: DateTime.now().toString(), name: "Grocery Store");
    final hardware = Shop(id: DateTime.now().add(Duration(seconds: 1)).toString(), name: "Hardware Store");
    
    _shops.addAll([grocery, hardware]);
    
    // Demo items for grocery store
    final groceryItems = [
      Item(id: "g1", name: "Milk", shopId: grocery.id, orderIndex: 0),
      Item(id: "g2", name: "Bread", shopId: grocery.id, orderIndex: 1),
      Item(id: "g3", name: "Eggs", shopId: grocery.id, orderIndex: 2),
      Item(id: "g4", name: "Apples", shopId: grocery.id, orderIndex: 3),
    ];
    
    // Demo items for hardware store
    final hardwareItems = [
      Item(id: "h1", name: "Nails", shopId: hardware.id, orderIndex: 0),
      Item(id: "h2", name: "Hammer", shopId: hardware.id, orderIndex: 1),
    ];
    
    _items.addAll([...groceryItems, ...hardwareItems]);
    
    // Add item IDs to shop
    grocery.itemIds.addAll(groceryItems.map((i) => i.id));
    hardware.itemIds.addAll(hardwareItems.map((i) => i.id));
    
    // Add some items to the shopping list
    final shoppingListItems = [
      ShoppingListItem.fromItem(groceryItems[0]),
      ShoppingListItem.fromItem(groceryItems[2]),
      ShoppingListItem.fromItem(hardwareItems[0]),
      // Add an ad-hoc item
      ShoppingListItem(
        id: "sl1",
        name: "Cookies",
        shopId: grocery.id,
        isAdHoc: true,
        orderIndex: 999,
      ),
    ];
    
    _shoppingList.addAll(shoppingListItems);
    
    _saveData();
  }
}