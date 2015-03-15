#!/bin/bash



for f in ./icons/*.svg; do
    filename=`basename $f .svg`.png
    echo "Converting $f to $filename"
    convert -density 300 -background none $f -resize 48x48 -gravity center -extent 48x48 ../res/drawable-mdpi/$filename  
    convert -density 300 -background none $f -resize 72x72 -gravity center -extent 72x72 ../res/drawable-hdpi/$filename  
    convert -density 300 -background none $f -resize 96x96 -gravity center -extent 96x96 ../res/drawable-xhdpi/$filename  
    convert -density 300 -background none $f -resize 144x144 -gravity center -extent 144x144 ../res/drawable-xxhdpi/$filename  
    echo 'Done'
done;