---
command:
  added: Pre-0.2.7
  aliases:
  - dhome
  - rmhome
  - delhome
  - removehome
  - rdeletehome
  configuration: []
  description: Deletes a home.
  permissions:
  - rcmds.exempt.deletehome
  - rcmds.deletehome
  - rcmds.others.deletehome
  supports: {}
  usage: /deletehome [home]
layout: command
title: /deletehome
---
  
```/deletehome``` Deletes the home of a player, ```[home]``` is the home to be deleted from the player running the command, it is possible to delete another players home by using ```[player:home]```


### Examples
```/deletehome mine``` - Would delete the home ```mine```
```/deletehome Notch:mine``` - Would delete the home ```mine``` of the player ```Notch```
