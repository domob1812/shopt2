class Shop {
  String id;
  String name;
  List<String> itemIds; // References to Item objects
  
  Shop({
    required this.id,
    required this.name,
    List<String>? itemIds,
  }) : itemIds = itemIds ?? [];
  
  factory Shop.fromJson(Map<String, dynamic> json) {
    return Shop(
      id: json['id'],
      name: json['name'],
      itemIds: List<String>.from(json['itemIds'] ?? []),
    );
  }
  
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'itemIds': itemIds,
    };
  }
  
  Shop copyWith({
    String? id,
    String? name,
    List<String>? itemIds,
  }) {
    return Shop(
      id: id ?? this.id,
      name: name ?? this.name,
      itemIds: itemIds ?? this.itemIds,
    );
  }
}