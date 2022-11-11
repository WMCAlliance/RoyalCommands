---
command:
  added: Pre-0.2.7
  aliases:
  - gm
  - gamem
  - gmode
  configuration: []
  description: Toggles your gamemode.
  permissions:
  - rcmds.gamemode
  - rcmds.exempt.gamemode
  supports:
    name-completion: false
    time-format: false
  usage: /gamemode (player) [gamemode]
layout: command
title: /gamemode
---

```/gamemode``` Allows the ```[gamemode]``` of the ```(player)``` to be changed
Without a ```[gamemode]``` the command will cycle through creative and survival for the ```(player)```, without a ```(player)``` the command will change the gamemode for the player whom sent the command
Ff no ```(player)``` and ```[gamemode]``` is specified the gamemode of the player sending the command will cycle between creative and survival

### Examples
```/gamemode spectator``` - Would set the gamemode of the sender to ```spectator```
```/gamemode Notch spectator``` - Would set the gamemode of the player ```Notch``` to ```spectator```
```/gamemode``` - Would cycle between the gamemode of the sender between creative and survival

