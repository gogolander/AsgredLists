#!/bin/bash

xdotool windowactivate $(xdotool search --name 'graphics1')
sleep 0.03
xdotool key --clearmodifiers $1
