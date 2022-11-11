---
command:
  added: Pre-0.2.7
  aliases:
  - helm
  configuration: []
  description: Changes your helmet.
  permissions:
  - rcmds.helmet
  supports:
    name-completion: false
    time-format: false
  usage: /helmet [item/none]
layout: command
title: /helmet
---

```/helmet``` allows a user to have a custom helmet set

If this command is executed without the ```(item)``` argument, it will use the currently held item as the helmet

To clear a helmet ```none``` can be sent in place of an item name to clear the current helemt

If the config option ```helm.require_item``` is set to true the user is required to have the item in their inventory

### Examples

