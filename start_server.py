import subprocess

commands = 'cd server; ' \
'mvn package; ' \
'mvn exec:java -D exec.mainClass=VideoGameEncyclopedia.CreateDatabase'

subprocess.run(commands, shell=True)
