import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/shop.dart';
import '../models/item.dart';

class StorageService extends ChangeNotifier {
  late SharedPreferences _prefs;
  List<Shop> _shops = [];
  List<Item> _items = [];
  
  List<Shop> get shops => _shops;
  List<Item> get items => _items;
  
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
    
    _shops = shopsJson.map((s) => Shop.fromJson(jsonDecode(s))).toList();
    _items = itemsJson.map((i) => Item.fromJson(jsonDecode(i))).toList();
    notifyListeners();
  }
  
  Future<void> _saveData() async {
    final shopsJson = _shops.map((s) => jsonEncode(s.toJson())).toList();
    final itemsJson = _items.map((i) => jsonEncode(i.toJson())).toList();
    
    await _prefs.setStringList('shops', shopsJson);
    await _prefs.setStringList('items', itemsJson);
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
  
  // Item CRUD operations
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
      _items.removeWhere((i) => i.id == itemId);
      await _saveData();
    }
  }
  
  Future<void> toggleItemCheck(String itemId) async {
    final index = _items.indexWhere((i) => i.id == itemId);
    if (index >= 0) {
      _items[index] = _items[index].copyWith(isChecked: !_items[index].isChecked);
      await _saveData();
    }
  }
  
  Future<void> removeCheckedItems() async {
    final checkedItemIds = _items.where((i) => i.isChecked).map((i) => i.id).toList();
    
    for (var shopIndex = 0; shopIndex < _shops.length; shopIndex++) {
      _shops[shopIndex].itemIds.removeWhere((id) => checkedItemIds.contains(id));
    }
    
    _items.removeWhere((i) => i.isChecked);
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
  
  // Get items for a specific shop, ordered by orderIndex
  List<Item> getItemsForShop(String shopId) {
    final shopItems = _items.where((i) => i.shopId == shopId).toList();
    shopItems.sort((a, b) => a.orderIndex.compareTo(b.orderIndex));
    return shopItems;
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
    
    _saveData();
  }
}