class Shop {
  String id;
  String name;
  List<String> itemIds; // References to Item objects
  int orderIndex; // For ordering shops in the list
  
  Shop({
    required this.id,
    required this.name,
    List<String>? itemIds,
    this.orderIndex = 0,
  }) : itemIds = itemIds ?? [];
  
  factory Shop.fromJson(Map<String, dynamic> json) {
    return Shop(
      id: json['id'],
      name: json['name'],
      itemIds: List<String>.from(json['itemIds'] ?? []),
      orderIndex: json['orderIndex'] ?? 0,
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'itemIds': itemIds,
      'orderIndex': orderIndex,
    };
  }
  
  Shop copyWith({
    String? id,
    String? name,
    List<String>? itemIds,
    int? orderIndex,
  }) {
    return Shop(
      id: id ?? this.id,
      name: name ?? this.name,
      itemIds: itemIds ?? this.itemIds,
      orderIndex: orderIndex ?? this.orderIndex,
    );
  }
}