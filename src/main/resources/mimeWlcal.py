#!/usr/bin/python3
import sys
from random import uniform

print('python echo module\ntype "break" or "quit" to quit\n', file=sys.stdout)
sys.stdout.flush()
repeat = True
while repeat:
    s = input()
    if s in ['break', 'quit']:
        repeat = False
    elif s == 'random':
        x = uniform(0,0.201)
        print('rms=%s. Accept?' %x, file=sys.stdout)
        sys.stdout.flush()
        error = True;
        while error:
            response = input()
            if response == 'accept':
                print('accepted', file=sys.stdout)
                sys.stdout.flush()
                error = False
            elif response == 'reject':
                print('rejected', file=sys.stdout)
                sys.stdout.flush()
                error = False
            elif response in ['break','quit']:
            	error = False
            else:
            	print('wrong answer: %s' %response, file=sys.stderr)
            	sys.stderr.flush()
            	error = True
        repeat = True
    else:
        print(s.upper(), file=sys.stdout)
        sys.stdout.flush()
        repeat = True
    print('---------', file=sys.stdout)
    sys.stdout.flush()
    
