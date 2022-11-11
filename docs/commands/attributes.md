---
command:
  added: 3.3.0
  aliases:
  - attribute
  - attrs
  - attr
  configuration: []
  description: Modifies attributes placed on items.
  permissions:
  - rcmds.attributes
  supports:
    name-completion: false
    time-format: false
  usage: /attributes [subcommand]
layout: command
title: /attributes
---

```/attribute``` allows the modification of attributes placed on items.

There are four possible subcommands ```add```,```clear```,```list```,```remove```

### Examples

```/attribute add generic_attack_damage add_number 10``` - Would give the item an attack damage attribute value of 10.  
```/attribute clear``` - Would remove all the attributes from an item.
```/attribute list``` - Displays the current attributes for an item.
```/attribute remove 5b1e88ed-6191-4a25-bf51-446b86bff5c3``` - Would remove the attribute with uuid of 5b1e88ed-6191-4a25-bf51-446b86bff5c3 from an item.



