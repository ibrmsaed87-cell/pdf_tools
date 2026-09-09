#!/bin/bash
set -e

SRC="icon.png"
RES="app/src/main/res"

# Clean up existing vector/webp icons
rm -f $RES/mipmap-anydpi-v26/*.xml
rm -f $RES/mipmap-*/ic_launcher*.webp
rm -f $RES/mipmap-*/ic_launcher*.png
rm -f $RES/drawable/ic_launcher_*.xml

mkdir -p $RES/mipmap-mdpi
mkdir -p $RES/mipmap-hdpi
mkdir -p $RES/mipmap-xhdpi
mkdir -p $RES/mipmap-xxhdpi
mkdir -p $RES/mipmap-xxxhdpi
mkdir -p $RES/mipmap-anydpi-v26

# Generate adaptive foregrounds
convert $SRC -resize 108x108 $RES/mipmap-mdpi/ic_launcher_foreground.png
convert $SRC -resize 162x162 $RES/mipmap-hdpi/ic_launcher_foreground.png
convert $SRC -resize 216x216 $RES/mipmap-xhdpi/ic_launcher_foreground.png
convert $SRC -resize 324x324 $RES/mipmap-xxhdpi/ic_launcher_foreground.png
convert $SRC -resize 432x432 $RES/mipmap-xxxhdpi/ic_launcher_foreground.png

# Generate legacy icons (we'll just round the corners slightly for a nice legacy look, or just leave as is. Let's do a simple circle for ic_launcher_round.png and normal for ic_launcher.png)

# Function to generate legacy and round icons
generate_legacy() {
    local size=$1
    local outdir=$2
    
    # Square/normal legacy
    convert $SRC -resize ${size}x${size} $outdir/ic_launcher.png
    
    # Round legacy
    convert $SRC -resize ${size}x${size} \
      \( +clone -alpha extract \
         -draw "fill black polygon 0,0 0,$(($size/2)) $(($size/2)),0 fill white circle $(($size/2)),$(($size/2)) $(($size/2)),0" \
         \( +clone -flip \) -compose Multiply -composite \
         \( +clone -flop \) -compose Multiply -composite \
      \) -alpha off -compose CopyOpacity -composite $outdir/ic_launcher_round.png
}

generate_legacy 48 $RES/mipmap-mdpi
generate_legacy 72 $RES/mipmap-hdpi
generate_legacy 96 $RES/mipmap-xhdpi
generate_legacy 144 $RES/mipmap-xxhdpi
generate_legacy 192 $RES/mipmap-xxxhdpi

# Generate adaptive icon XML
cat << 'XML' > $RES/mipmap-anydpi-v26/ic_launcher.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
XML

cat << 'XML' > $RES/mipmap-anydpi-v26/ic_launcher_round.xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
XML

# Create background color (since image is solid, white is fine as a fallback background)
cat << 'XML' > $RES/values/ic_launcher_background.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#FFFFFF</color>
</resources>
XML

echo "Done"
