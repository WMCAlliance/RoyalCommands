---
command:
  added: Pre-0.2.7
  aliases:
  - deaf
  - ignoreall
  configuration: []
  description: Toggles whether a player can see messages.
  permissions:
  - rcmds.deafen
  - rcmds.exempt.deafen
  - rcmds.others.deafen
  supports:
    name-completion: false
    time-format: false
  usage: /deafen (player)
layout: command
title: /deafen
---

```/deafen``` toggles the state of being "deaf" on the sender or ```(player)``` if specified, so long as the sender has
```rcmds.others.deafen``` and ```(player)``` does not have ```rcmds.exempt.deafen```.

If a player is "deaf," he will not receive any player chat messages, but will still receive plugin messages.

### Examples 

```/deafen``` - Toggles deaf status on the sender.
```/deafen Notch``` - Toggles deaf status on ```Notch```.

