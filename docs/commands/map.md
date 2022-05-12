---
command:
  added: 3.2.0
  aliases:
  - mapadjust
  configuration: []
  description: Adjusts maps.
  permissions:
  - rcmds.map
  supports:
    name-completion: false
    time-format: false
  usage: /map [subcommands]
layout: command
title: /map
---

```/map``` provides the ability to modify aspects and details of a map item. Only the map in a player's hand can be modified. Running one of the below commands without extra arguments will provide you with example values. The entire command (and all available subcommands) are covered by the ```rcmds.map``` permission.

```/map help``` - Displays an ingame list of available arguments.
```/map scale [scaletype]``` - Sets the scale of the map in hand. Accepts ```closest```, ```close```, ```normal```, ```far``` and ```farthest``` as valid scales. Can alternatively be accessed via ```scaling```, ```setscale``` and ```setscaling```.
```/map position [x] [z]``` - Sets the center position of the map in hand. Cannot be a decimal. Can alternatively be accessed via ```reposition```, ```pos```, ```repos```, ```setposition```, ```setpos```, ```coords```, ```coordinates```, ```setcoords``` and ```setcoordinates```.
```/map lock``` - Sets the lock status of the map in hand
```/map tracking``` - Sets if tracking should be used on the map in hand.
```/map unlimited``` - Sets if unlimited tracking should be used on the map in hand.
```/map world [world]``` - Changes the world displayed by the map in hand. Can alternatively be accessed via ```setworld```.
```/map info``` - Displays information about the map in hand. Can alternatively be accessed via ```getinfo```, ```information``` and ```getinformation```.

### Examples

```/map scale closest``` - Sets the map to the closest scale.  
```/map position 0 0``` - Centers the map on the coordinates 0x and 0z.  
```/map world survival``` - Set's the map to display the survival world.
