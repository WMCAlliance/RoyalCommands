package org.royaldev.royalcommands.rcommands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.util.ArrayList;
import java.util.List;

public abstract class TabCommand extends CACommand implements TabCompleter {

    private final List<Integer> completionTypes = new ArrayList<>();

    TabCommand(RoyalCommands instance, String name, boolean checkPermissions, Integer[] cts) {
        super(instance, name, checkPermissions);
        for (Integer i : cts) {
            if (i == null) i = 0;
            this.completionTypes.add(i);
        }
    }

    private List<String> getCompletionsFor(CommandSender cs, Command cmd, String label, String[] args, final CompletionType ct) {
        final List<String> possibilities = new ArrayList<>();
        if (args.length < 1) return possibilities;
        final String arg = args[args.length - 1].toLowerCase();
        switch (ct) {
            case ONLINE_PLAYER:
                for (final Player p : this.plugin.getServer().getOnlinePlayers()) {
                    final String name = p.getName();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case ITEM_ALIAS:
                possibilities.addAll(RoyalCommands.inm.getPossibleNames(arg));
                break;
            case ITEM:
                for (final Material m : Material.values()) {
                    final String name = m.name();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case CUSTOM:
                possibilities.addAll(this.getCustomCompletions(cs, cmd, label, args, arg));
                break;
            case PLUGIN:
                possibilities.addAll(this.getPluginCompletions(arg));
                break;
            case WORLD:
                for (final World w : this.plugin.getServer().getWorlds()) {
                    final String name = w.getName();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case LIST:
                final List<String> custom = this.customList(cs, cmd, label, args, arg);
                if (custom == null) break;
                for (final String s : custom) {
                    final String lowerCaseName = s.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), s);
                }
                break;
            case ENUM:
                final Enum[] enums = this.customEnum(cs, cmd, label, args, arg);
                if (enums == null) break;
                for (final Enum e : enums) {
                    final String name = e.name();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case ROYALCOMMANDS_COMMAND:
            case ANY_COMMAND:
                final SimpleCommandMap commandMap;
                try {
                    Object result = RUtils.getPrivateField(this.plugin.getServer().getPluginManager(), "commandMap");
                    commandMap = (SimpleCommandMap) result;
                } catch (Exception e) {
                    break;
                }
                for (final Command c : commandMap.getCommands()) {
                    if (ct == CompletionType.ROYALCOMMANDS_COMMAND && (!(c instanceof PluginCommand) || !this.plugin.getClass().getName().equals(((PluginCommand) c).getPlugin().getClass().getName()))) {
                        continue;
                    }
                    final String name = c.getName();
                    if (possibilities.contains(name)) continue;
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                    for (final String alias : c.getAliases()) {
                        if (possibilities.contains(alias)) continue;
                        final String lowerCaseAlias = alias.toLowerCase();
                        if (!lowerCaseAlias.startsWith(arg)) continue;
                        possibilities.add(lowerCaseAlias.equals(arg) ? 0 : possibilities.size(), alias);
                    }
                }
                break;
        }
        return this.filterCompletions(possibilities, cs, cmd, label, args, arg);
    }

    /**
     * An array of all Enum values used by {@link org.royaldev.royalcommands.rcommands.TabCommand.CompletionType#ENUM}.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return Array of Enums to be checked
     */
    @SuppressWarnings("UnusedParameters")
    Enum[] customEnum(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return new Enum[0];
    }

    /**
     * A list of values used by {@link org.royaldev.royalcommands.rcommands.TabCommand.CompletionType#LIST}.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return List of values to be checked
     */
    @SuppressWarnings("UnusedParameters")
    List<String> customList(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return new ArrayList<>();
    }

    /**
     * Filters all completions just before they are returned. This should be overridden by any class extending
     * TabCommand if necessary.
     *
     * @param completions Completions before filtering
     * @param cs          CommandSender completing
     * @param cmd         Command being completed
     * @param label       Label of command being completed
     * @param args        All arguments
     * @param arg         Argument to complete
     * @return Filtered completions to return
     */
    @SuppressWarnings("UnusedParameters")
    List<String> filterCompletions(List<String> completions, CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return completions;
    }

    /**
     * Gets the custom completions for an argument. This should be overridden by any class extending TabCommand. This
     * will be called on any argument with a CompletionType of CUSTOM.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return List of possible completions (not null)
     */
    @SuppressWarnings("UnusedParameters")
    List<String> getCustomCompletions(CommandSender cs, Command cmd, String label, String[] args, String arg) {
        return new ArrayList<>();
    }

    List<String> getPluginCompletions(String arg) {
        final List<String> possibilities = new ArrayList<>();
        for (final Plugin p : this.plugin.getServer().getPluginManager().getPlugins()) {
            final String name = p.getName();
            final String lowerCaseName = name.toLowerCase();
            if (!lowerCaseName.startsWith(arg)) continue;
            possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
        }
        return possibilities;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmd, String label, String[] args) {
        final ArrayList<String> possibilities = new ArrayList<>();
        final int lastArgIndex = args.length - 1;
        if (lastArgIndex >= this.completionTypes.size()) return possibilities;
        for (final CompletionType ct : CompletionType.getCompletionTypes(this.completionTypes.get(lastArgIndex))) {
            possibilities.addAll(this.getCompletionsFor(cs, cmd, label, args, ct));
        }
        return possibilities;
    }

    enum CompletionType {
        /**
         * Completes for any online player.
         */
        ONLINE_PLAYER(1),
        /**
         * Completes for any item alias in items.csv.
         */
        ITEM_ALIAS(2),
        /**
         * Completes for any material name (see {@link Material}).
         */
        ITEM(4),
        /**
         * Completes based on
         * {@link #getCustomCompletions(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[], String)},
         * which can be overridden.
         */
        CUSTOM(8),
        /**
         * Completes for any plugin loaded.
         */
        PLUGIN(16),
        /**
         * Completes for any world loaded.
         */
        WORLD(32),
        /**
         * Completes for a list specified by
         * {@link #customList(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[], String)}.
         */
        LIST(64),
        /**
         * Completes for an enum specified by
         * {@link #customEnum(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[], String)}.
         */
        ENUM(128),
        /**
         * Completes for any RoyalCommands command.
         */
        ROYALCOMMANDS_COMMAND(256),
        /**
         * Completes for any command.
         */
        ANY_COMMAND(512);

        private final int i;

        CompletionType(int i) {
            this.i = i;
        }

        public static List<CompletionType> getCompletionTypes(int i) {
            final List<CompletionType> cts = new ArrayList<>();
            for (final CompletionType ct : CompletionType.values()) {
                if ((i & ct.getInt()) <= 0) continue;
                cts.add(ct);
            }
            return cts;
        }

        public int getInt() {
            return this.i;
        }
    }
}
