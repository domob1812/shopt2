class Item {
  String id;
  String name;
  int orderIndex; // For ordering within a shop
  String shopId;
  
  Item({
    required this.id,
    required this.name,
    required this.shopId,
    this.orderIndex = 0,
  });
  
  factory Item.fromJson(Map<String, dynamic> json) {
    return Item(
      id: json['id'],
      name: json['name'],
      shopId: json['shopId'],
      orderIndex: json['orderIndex'] ?? 0,
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'shopId': shopId,
      'orderIndex': orderIndex,
    };
  }
  
  Item copyWith({
    String? id,
    String? name,
    String? shopId,
    int? orderIndex,
  }) {
    return Item(
      id: id ?? this.id,
      name: name ?? this.name,
      shopId: shopId ?? this.shopId,
      orderIndex: orderIndex ?? this.orderIndex,
    );
  }
}