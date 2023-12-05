---
command:
  added: Pre-0.2.7
  aliases:
  - silence
  configuration: []
  description: Stops a user from speaking.
  permissions:
  - rcmds.mute
  - rcmds.exempt.mute
  supports:
    name-completion: false
    time-format: false
  usage: /mute [player] (time) (reason)
layout: command
title: /mute
---

```/mute``` is a command that allows administrators to mute players.

### Examples

**Setting a nickname**

```/mute jkcclemens``` – This would toggle mute of the player ```jkcclemens```.

```/mute WizardCM 5m``` – This would mute the player ```WizardCM``` for 5 minutes.

```/mute Mu5tank05 1h Please stop talking!``` – This would mute the player ```Mu5tank05``` for 5 minutes with the reason ```Please stop talking!```.

