#!/usr/bin/env python
import sys
from random import uniform

sys.stdout.write('python echo module\ntype "break" or "quit" to quit\n')
sys.stdout.flush()
while True:
    s = sys.stdin.readline()
    s = s.rstrip('\n')
    if s in ['break', 'quit']:
        break
    elif s == 'random':
        x = uniform(0,0.201)
        sys.stdout.write('rms=%s. Accept?\n' %x)
        sys.stdout.flush()
        while True:
            response = sys.stdin.readline()
            response = response.rstrip('\n')
            if response == 'accept':
                sys.stdout.write('accepted\n')
                sys.stdout.flush()
                break
            elif response == 'reject':
                sys.stdout.write('rejected\n')
                sys.stdout.flush()
                break
            elif response in ['break','quit']:
                exit(0)
            else:
            	sys.stdout.write('wrong answer: %s\n' %response)
            	sys.stdout.flush()
    else:
        sys.stdout.write(s.upper() + '\n')
        sys.stdout.flush()
    sys.stdout.write('---------\n')
    sys.stdout.flush()
