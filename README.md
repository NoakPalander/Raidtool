# Raidtool

Kotlin based discord bot used as a raid tool
 
## Some commands
```
ff!help - Displays the help page
ff!schedule new - Removes the old schedule and creates a new schedule poll
ff!schedule set - Books the actual schedule
ff!schedule set silent - Books the schedule without mentioning everyone
ff!schedule clean - Removes any schedule
ff!schedule when - Displays when the schedule is booked for
ff!bis <class> - Displays the best in slot gear for the entered class (e.g: ff!bis war)
```

## Running
* There's a shell script called `liverun.sh`, this runs the application via gradle and sets the resource path, you need to manually add your discord bots configurations into a json file deserializable by `ffxiv.raidtool.resource.ConfigKt`
* The other alternative is to run `gradle shadowJar` which copies the resources and a fat-jar into the `bin` folder, ready to be directly executed there. 

License
----
MIT
