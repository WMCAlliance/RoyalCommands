from os import makedirs
from os.path import exists
from shutil import move, rmtree

import yaml
from requests import get
from yaml import load, dump, Loader

r = None
data = {}


def create_good_dir(name):
    orig_name = name
    i = 0
    while exists(name):
        i += 1
        name = "{}{}".format(orig_name, i)
    makedirs(name)
    return name


def download():
    global r, data
    r = get(
        "https://raw.githubusercontent.com/WMCAlliance/RoyalCommands/native-1.19/modules/RoyalCommands/src/main/resources/plugin.yml")
    try:
        data = yaml.safe_load(r.text)
    except Exception as e:
        print("Could not parse plugin.yml: {}".format(e))


def load_yml():
    global r, data
    r = open("../modules/RoyalCommands/src/main/resources/plugin.yml", "r")
    try:
        data = yaml.safe_load(r)
    except Exception as e:
        print("Could not parse plugin.yml: {}".format(e))


def get_default_text(default):
    text = "Unknown"
    if type(default) == bool and not default:
        text = "No one"
    elif type(default) == bool and default:
        text = "Everyone"
    elif type(default) == str and default == "op":
        text = "Ops"
    return text


def generate_permission_index():
    if not exists("permissions.html"):
        front_matter = """---\nlayout: titled\ntitle: Permissions\nextra_css: |\n  <link rel="stylesheet" href="/css/bootstrap-table.css"/>\n  <link rel="stylesheet" href="//cdn.jsdelivr.net/g/pure@0.5.0(forms-min.css)"/>\n  <style>table { margin-top: 10px; }</style>\nextra_javascript: |\n  <script src="/js/list.min.js"></script>\n  <script>new List("permission_list", { valueNames: ["permission", "description", "default", "command"] });</script>\n---"""
    else:
        f = open("permissions.html")
        index = f.read()
        front_matter = "---".join(index.split("---")[:2]) + "---\n"
    index = """<div id="permission_list">\n  <div class="pure-form">\n    <input id="permission_search" type="text" class="search pure-input-1 pure-input-rounded" placeholder="Search"/>\n  </div>\n  <ul class="pure-paginator"></ul>\n  <table class="table">\n    <thead>\n      <tr>\n        <th>Permission</th>\n        <th>Description</th>\n        <th>Default</th>\n        <th>Command</th>\n      </tr>\n    </thead>\n    <tbody class="list">\n"""
    for permission in sorted(data["permissions"]):
        permission_data = data["permissions"][permission]
        default = get_default_text(permission_data["default"]) if "default" in permission_data else "Unknown"
        command = '<a class="command" href="/commands/{0}">/{0}</a>'.format(
            permission_data["command"]) if "command" in permission_data else "N/A"
        index += "      <tr>\n        <td class=\"permission\">{0}</td>\n        <td class=\"description\">{1}</td>\n        <td class=\"default\">{2}</td>\n        <td>{3}</td>\n      </tr>\n".format(
            permission, permission_data["description"], default, command)
    index += "    </tbody>\n  </table>\n</div>\n"
    f = open("permissions.html", "w")
    f.write("{}{}".format(front_matter, index))
    f.flush()
    f.close()


def generate_command_index():
    if not exists("commands.html"):
        front_matter = """---\nlayout: titled\ntitle: Commands\nextra_css: |\n  <link rel="stylesheet" href="/css/bootstrap-table.css"/>\n  <link rel="stylesheet" href="//cdn.jsdelivr.net/g/pure@0.5.0(forms-min.css)"/>\n  <style>table { margin-top: 10px; }</style>\nextra_javascript: |\n  <script src="/js/list.min.js"></script>\n  <script>new List("command_list", { valueNames: ["command", "alias"] });</script>\n---"""
    else:
        f = open("commands.html")
        index = f.read()
        front_matter = "---".join(index.split("---")[:2]) + "---\n"
    index = """<div id="command_list">\n  <div class="pure-form">\n    <input id="command_search" type="text" class="search pure-input-1 pure-input-rounded" placeholder="Search"/>\n  </div>\n  <table class="table">\n    <thead>\n      <tr>\n        <th>Command</th>\n        <th>Description</th>\n      </tr>\n    </thead>\n    <tbody class="list">\n"""
    for command in sorted(data["reflectcommands"]):
        command_data = data["reflectcommands"][command]
        index += "      <tr>\n        <td><a class=\"command\" href=\"/commands/{0}\">/{0}</a></td>\n        <td>{1}</td>".format(
            command, command_data["description"])
        if "aliases" in command_data:
            index += "\n        <td class=\"hidden alias\">{}</td>".format("/" + " /".join(command_data["aliases"]))
        index += "\n      </tr>\n"
    index += "    </tbody>\n  </table>\n</div>\n"
    f = open("commands.html", "w")
    f.write("{}{}".format(front_matter, index))
    f.flush()
    f.close()


def get_permissions_for_command(command):
    global r, data
    perms = []
    for permission in data["permissions"]:
        perm_data = data["permissions"][permission]
        if "command" not in perm_data or perm_data["command"].lower() != command.lower(): continue
        perms.append(permission)
    return perms


def generate_command_files():
    global r, data
    if r is None:
        print("Could not download plugin.yml")
        return
    commands = data["reflectcommands"]
    new_dir_name = create_good_dir("new_commands")
    for command in commands:
        command_data = commands[command]
        old_data = None
        if exists("commands/{}.md".format(command)):
            old_data = open("commands/{}.md".format(command)).read()
            front_matter = yaml.safe_load(old_data.split("---")[1])
        else:
            front_matter = {}
        fm_command = front_matter["command"] if "command" in front_matter else {}
        fm_command["description"] = command_data["description"]
        fm_command["added"] = command_data["version_added"]
        if "aliases" in command_data: fm_command["aliases"] = command_data["aliases"]
        fm_command["usage"] = command_data["usage"].replace("<command>", command)
        if "configuration" not in fm_command: fm_command["configuration"] = []
        if "permissions" not in fm_command or len(fm_command["permissions"]) < 1:
            fm_command["permissions"] = get_permissions_for_command(command)
        else:
            fm_command["permissions"] += list(
                set(get_permissions_for_command(command)) - set(fm_command["permissions"]))
        fm_command["permissions"] = fm_command["permissions"][0:1] + sorted(fm_command["permissions"][1:])
        supports = fm_command["supports"] if "supports" in fm_command else {}
        fm_command["supports"] = supports
        front_matter["command"] = fm_command
        front_matter["layout"] = "command"
        front_matter["title"] = "/{}".format(command)
        content = "---\n{}---{}".format(dump(front_matter, default_flow_style=False),
                                        old_data.split("---")[2] if old_data is not None else "")
        f = open("{}/{}.md".format(new_dir_name, command), "w")
        f.write(content)
        f.flush()
        f.close()
    rmtree("commands")
    move(new_dir_name, "commands")


if __name__ == "__main__":
    # download()
    load_yml()
    generate_command_files()
    generate_command_index()
    generate_permission_index()
