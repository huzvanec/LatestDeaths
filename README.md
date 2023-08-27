# LatestDeaths
## About
LatestDeaths is a plugin, that saves data after each player's death and than provides a simple way to read it
## Supported versions
Paper based servers 1.19.4+<br>
**Bukkit and Spigot are not supported!**
# Usage
#### Read your last death
`/ld`<br>
This requires the player to have the `latestdeaths.lastdeaths` permission
#### Read your last few deaths
`/ld <amount>`<br>
The amount limit can be [specified in the config](https://github.com/Mandlemankiller/LatestDeaths/blob/master/src/main/resources/config.yml#L24), players with `latesetdeaths.overlimit` permission can bypass this limit.
#### Read somebody else's few last deaths
`/ld <amount> <player name>`<br>
This requires the player to have the `latestdeaths.seeothers` permission.<br>
This command is also runnable from the console
#### Reload the plugin
`/ldadmin reload`<br>
This requires the player to have the `latestdeaths.admin` permission
## Build
Requirements: <br>
[Java](https://java.com), [Git](https://git-scm.com/), [Maven](https://maven.apache.org/)<br>
Clone the repository:<br>
```git clone https://github.com/Mandlemankiller/LatestDeaths``` <br>
Move to the folder:<br>
```cd LatestDeaths``` <br>
Run maven: <br>
```mvn package``` <br>
Done! The jar is now in the target directory, it's called ```LatestDeaths-1.0-SNAPSHOT.jar```