---
command:
  added: Pre-0.2.7
  aliases:
  - dispname
  - name
  configuration:
  - nicknames
  description: Sets the display name of a player.
  permissions:
  - rcmds.nick
  - rcmds.exempt.nick
  - rcmds.exempt.nick.*
  - rcmds.exempt.nick.changelimit
  - rcmds.exempt.nick.length
  - rcmds.exempt.nick.regex
  - rcmds.nick.colors
  - rcmds.others.nick
  supports:
    name-completion: false
    time-format: false
  usage: /nick (player) [nick/clear]
layout: command
title: /nick
---

```/nick``` is a command that allows for players and administrators to manage nicknames.

### Examples

**Setting a nickname**

```/nick jkcclemens Developer``` – This would set ```jkcclemens```' nickname to ```Developer```.

```/nick Developer``` – This would set the nickname of the sender to ```Developer```.

**Clearing a nickname**

```/nick jkcclemens clear``` – This would clear ```jkcclemens```' nickname.

```/nick clear``` – This would clear the nickname of the sender.
