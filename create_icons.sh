#!/bin/bash

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "Error: ImageMagick is not installed. Please install it first."
    echo "Ubuntu/Debian: sudo apt install imagemagick"
    echo "macOS: brew install imagemagick"
    exit 1
fi

# Check if icon.png exists
if [ ! -f "icon.png" ]; then
    echo "Error: icon.png not found in current directory"
    exit 1
fi

echo "Creating Android app icons from icon.png..."

# Define the sizes and directories
declare -A SIZES=(
    ["mdpi"]="48"
    ["hdpi"]="72"
    ["xhdpi"]="96"
    ["xxhdpi"]="144"
    ["xxxhdpi"]="192"
)

# Base directory for resources
RES_DIR="app/src/main/res"

# Fastlane directory for F-Droid
FASTLANE_DIR="fastlane/metadata/android/en-US/images"

# Remove old XML files and create foreground PNG files
for density in "${!SIZES[@]}"; do
    size=${SIZES[$density]}
    dir="$RES_DIR/mipmap-$density"
    
    echo "Creating ${size}x${size} icons for $density..."
    
    # Remove old XML file if it exists
    if [ -f "$dir/ic_launcher.xml" ]; then
        rm "$dir/ic_launcher.xml"
        echo "  Removed old $dir/ic_launcher.xml"
    fi
    
    # Create foreground PNG (transparent background preserved)
    convert icon.png -resize ${size}x${size} "$dir/ic_launcher_foreground.png"
    echo "  Created $dir/ic_launcher_foreground.png"
    
    # Create legacy icon (add white background)
    convert -size ${size}x${size} xc:white icon.png -resize ${size}x${size} -composite "$dir/ic_launcher.png"
    echo "  Created $dir/ic_launcher.png"
done

# Create F-Droid icon for fastlane metadata
echo "Creating F-Droid icon for fastlane..."

# Create fastlane images directory if it doesn't exist
mkdir -p "$FASTLANE_DIR"

# Create 512x512 icon for F-Droid (with white background)
convert -size 512x512 xc:white icon.png -resize 512x512 -composite "$FASTLANE_DIR/icon.png"
echo "  Created $FASTLANE_DIR/icon.png (512x512)"

echo ""
echo "All icons created successfully!"
echo ""
echo "Summary:"
echo "- Foreground icons (for adaptive icons): ic_launcher_foreground.png"
echo "- Legacy icons (with white background): ic_launcher.png"
echo "- F-Droid icon (512x512): fastlane/metadata/android/en-US/images/icon.png"
echo "- Background is already set to white in ic_launcher_background.xml"
echo ""
echo "Your app is now ready with the new custom icons and F-Droid metadata!"