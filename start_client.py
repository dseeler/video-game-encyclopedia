import subprocess

commands = 'cd client; ' \
'yarn install; ' \
'yarn start;'

subprocess.run(commands, shell=True)
