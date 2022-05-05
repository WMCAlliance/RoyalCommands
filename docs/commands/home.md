---
command:
  added: Pre-0.2.7
  aliases:
  - rhome
  configuration: []
  description: Teleports the player home.
  permissions:
  - rcmds.home
  - rcmds.exempt.home
  - rcmds.others.home
  supports:
    name-completion: false
    time-format: false
  usage: /home (home)
layout: command
title: /home
---

```/home``` Teleports the player to the ```(home)``` specified, if not specified the player would teleport to the home named ```home```
It is possible to teleport to another players home by using ```[player:home]```


### Examples
```/home mine``` - Would teleport the player to the home ```mine```
```/home Notch:mine``` - Would teleport the player to the home ```mine``` of the player ```Notch```

