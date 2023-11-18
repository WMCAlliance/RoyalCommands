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
  usage: /nick [player] [nick/clear]
layout: command
title: /nick
---

```/nick``` is a command that allows for players and administrators to manage nicknames.

### Examples

**Setting a nickname**

```/nick -player jkcclemens -nick Developer``` – This would set ```jkcclemens```' nickname to ```Developer```

```/nick -p jkcclemens -n Developer``` – This does the same as the above example but uses shorter flags.

**Clearing a nickname**

```/nick -player jkcclemens -clear``` – This would clear ```jkcclemens```' nickname.

```/nick -p jkcclemens -c``` – This does the same as the above example but uses shorter flags.
