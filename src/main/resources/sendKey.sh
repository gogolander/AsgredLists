#!/bin/bash

xdotool windowactivate $(xdotool search --name 'graphics1');
xdotool key --clearmodifiers  $1
