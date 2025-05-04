# ShopT - A Simple Shopping List App

A Flutter-based shopping list app with these features:

- Group shopping items by shop
- Check off items as you shop
- Remove ticked items with one tap
- Edit the list of shops
- Edit and reorder items within each shop
- Items appear in your custom order (as they appear in the shop)

## How to Use

1. The main screen shows your shopping list grouped by shops
2. Tap the + button to add a new item
3. Tap the checkmark to check off an item
4. Tap the shop icon in the app bar to edit shops
5. Tap the edit button next to a shop to edit its items 
6. In the items edit screen, drag items to reorder them
7. Tap the broom icon to clear all checked items

## Technical Details

- Uses shared_preferences for local storage
- Implements provider for state management
- Features custom sorting of items within shops
- Built with Flutter