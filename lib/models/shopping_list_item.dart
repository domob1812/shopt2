import '../models/item.dart';

class ShoppingListItem {
  String id;
  String? itemId; // Reference to a configured Item, null for ad-hoc items
  String name;
  bool isChecked;
  String shopId;
  int orderIndex; // For ordering within the shopping list
  bool isAdHoc; // Indicates if this is an ad-hoc item (not pre-configured)
  String quantity; // Quantity of the item to buy, including units
  
  ShoppingListItem({
    required this.id,
    this.itemId,
    required this.name,
    required this.shopId,
    this.isChecked = false,
    this.orderIndex = 0,
    this.isAdHoc = false,
    this.quantity = '',
  });
  
  factory ShoppingListItem.fromJson(Map<String, dynamic> json) {
    return ShoppingListItem(
      id: json['id'],
      itemId: json['itemId'],
      name: json['name'],
      shopId: json['shopId'],
      isChecked: json['isChecked'] ?? false,
      orderIndex: json['orderIndex'] ?? 0,
      isAdHoc: json['isAdHoc'] ?? false,
      quantity: json['quantity'] ?? '',
    );
  }
  
  factory ShoppingListItem.fromItem(Item item) {
    return ShoppingListItem(
      id: DateTime.now().toString(),
      itemId: item.id,
      name: item.name,
      shopId: item.shopId,
      orderIndex: item.orderIndex,
      isAdHoc: false,
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'itemId': itemId,
      'name': name,
      'shopId': shopId,
      'isChecked': isChecked,
      'orderIndex': orderIndex,
      'isAdHoc': isAdHoc,
      'quantity': quantity,
    };
  }
  
  ShoppingListItem copyWith({
    String? id,
    String? itemId,
    String? name,
    String? shopId,
    bool? isChecked,
    int? orderIndex,
    bool? isAdHoc,
    String? quantity,
  }) {
    return ShoppingListItem(
      id: id ?? this.id,
      itemId: itemId ?? this.itemId,
      name: name ?? this.name,
      shopId: shopId ?? this.shopId,
      isChecked: isChecked ?? this.isChecked,
      orderIndex: orderIndex ?? this.orderIndex,
      isAdHoc: isAdHoc ?? this.isAdHoc,
      quantity: quantity ?? this.quantity,
    );
  }
}