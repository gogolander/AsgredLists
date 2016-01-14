import sys
sys.stdout.write('python echo module\ntype "break" or "quit" to quit\n')
sys.stdout.flush()
s = sys.stdin.readline().strip()
while s not in ['break', 'quit']:
    sys.stdout.write(s.upper() + '\n')
    sys.stdout.flush()
    s = sys.stdin.readline().strip()
