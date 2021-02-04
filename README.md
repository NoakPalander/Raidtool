# Raidtool

Kotlin based discord bot used as a raid tool
 
## Some commands
```
ff!help - Displays the help page
ff!schedule new - Removes the old schedule and creates a new schedule poll
ff!schedule set - Books the actual schedule
ff!schedule clean - Removes any schedule
ff!schedule when - Displays when the schedule is booked for
ff!bis <class> - Displays the best in slot gear for the entered class (e.g: ff!bis war)
ff!source - Displays the link to this github/page.

Random text memes:
ff!t0nk or ff!tonk
ff!divination
ff!shirk
ff!paladin
ff!war-chad
ff!snowman
ff!snowman2
```

## Running
* Running `gradle run` will hardcode the resource path to the config files
* Running `gradle shadowJar` generates a fat-jar, whilst- 
* `gradle bin` invokes `gradle ShadowJar` and copies the fat-jar and resources into a `bin` directory in the root directory
  
License
----
MIT
