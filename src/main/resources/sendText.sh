#!/bin/bash

xdotool windowactivate $(xdotool search --name 'graphics1');
xdotool type --clearmodifiers  $1
