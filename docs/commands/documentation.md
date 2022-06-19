---
command:
  added: 3.3.0
  aliases:
  - docs
  - doc
  configuration: []
  description: Gets a link to the documentation of a command or permission node.
  permissions:
  - rcmds.documentation
  supports: {}
  usage: /documentation [command|permission] [name]
layout: command
title: /documentation
---

```/documentation``` Gets a link to the documentation of a command or permission node ```[name]``` is the name of the command or permission node

### Examples
```/documentation command documentation``` - Would return the link for this page
```/documentation permission rcmds.fireball``` - Would return the link for the permission for the fireball command