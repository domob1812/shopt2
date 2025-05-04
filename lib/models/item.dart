class Item {
  String id;
  String name;
  bool isChecked;
  int orderIndex; // For ordering within a shop
  String shopId;
  
  Item({
    required this.id,
    required this.name,
    required this.shopId,
    this.isChecked = false,
    this.orderIndex = 0,
  });
  
  factory Item.fromJson(Map<String, dynamic> json) {
    return Item(
      id: json['id'],
      name: json['name'],
      shopId: json['shopId'],
      isChecked: json['isChecked'] ?? false,
      orderIndex: json['orderIndex'] ?? 0,
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'shopId': shopId,
      'isChecked': isChecked,
      'orderIndex': orderIndex,
    };
  }
  
  Item copyWith({
    String? id,
    String? name,
    String? shopId,
    bool? isChecked,
    int? orderIndex,
  }) {
    return Item(
      id: id ?? this.id,
      name: name ?? this.name,
      shopId: shopId ?? this.shopId,
      isChecked: isChecked ?? this.isChecked,
      orderIndex: orderIndex ?? this.orderIndex,
    );
  }
}