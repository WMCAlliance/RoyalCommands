/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import net.md_5.bungee.api.chat.TextComponent;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.royaldev.royalcommands.AuthorizationHandler;
import org.royaldev.royalcommands.Config;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor {

    protected final RoyalCommands plugin;
    /**
     * The AuthorizationHandler for this command. This is essentially an alias of this.plugin.ah.
     */
    protected final AuthorizationHandler ah;
    private final String name;
    private final boolean checkPermissions;
    private final List<Flag> expectedFlags = new ArrayList<>();
    private final List<String> helpNames = Arrays.asList("help", "h", "?");

    /**
     * Constructs a BaseCommand. This command has some backend utilities to help speed up command development and make
     * usage better overall.
     *
     * @param instance         The instance of RoyalCommands that this command is being registered with
     * @param name             The name of this command
     * @param checkPermissions Whether to check permissions using the auto-generated permission
     */
    public BaseCommand(final RoyalCommands instance, final String name, final boolean checkPermissions) {
        this.plugin = instance;
        this.ah = this.plugin.ah;
        this.name = name;
        this.checkPermissions = checkPermissions;
    }

    /**
     * The body of the command to be run. Depending on the constructor
     * ({@link #BaseCommand(org.royaldev.royalcommands.RoyalCommands, String, boolean)}), permissions will have already
     * been checked. The command name matching the name of this command is already checked. All unhandled exceptions
     * will be caught and displayed to the user in a friendly format.
     *
     * @param cs    The CommandSender using the command
     * @param cmd   The Command being used
     * @param label The label of the command (alias)
     * @param args  The arguments passed to the command
     * @return true to not display usage, false to display usage (essentially)
     */
    protected abstract boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args);


    private void showFlagHelp(final CommandSender cs, final Command cmd, final String label) {
        cs.sendMessage(cmd.getDescription());
        cs.sendMessage(cmd.getUsage().replaceFirst("<command>", label));
        cs.sendMessage(MessageColor.POSITIVE + "Expected flags:");
        for (final Flag f : this.getExpectedFlags()) {
            String message = "  " + MessageColor.NEUTRAL + "-" + f.getNames();
            if (f.getType() != null) message += " [" + f.getType().getSimpleName() + "]";
            cs.sendMessage(message);
        }
    }

    protected void addExpectedFlag(final Flag f) {
        Preconditions.checkNotNull(f, "f cannot be null");
        if (this.expectedFlags.contains(f)) throw new IllegalArgumentException("Flag already exists!");
        this.expectedFlags.add(f);
    }

    /**
     * Gets the CommandArguments from the given arguments. This allows for flags to be used.
     *
     * @param args Arguments to generate CommandArguments from
     * @return CommandArguments
     * @see org.royaldev.royalcommands.rcommands.CACommand
     */
    protected CommandArguments getCommandArguments(final String[] args) {
        return new CommandArguments(args);
    }

    public List<Flag> getExpectedFlags() {
        return this.expectedFlags;
    }

    /**
     * Gets the name of this command.
     *
     * @return Name
     */
    public String getName() {
        return this.name;
    }
    /**
     * Handles an exception. Generates a useful debug paste and sends it to the user if enabled. Also prints the stack
     * trace to the console and tells the user that an exception occurred.
     *
     * @param cs      The CommandSender using the command
     * @param cmd     The Command being used
     * @param label   The label of the command (alias)
     * @param args    The arguments passed to the command
     * @param message Message to be shown about the exception
     * @param t       The exception thrown
     */
    protected void handleException(final CommandSender cs, final Command cmd, final String label, final String[] args, final Throwable t, final String message) {
        cs.spigot().sendMessage(TextComponent.fromLegacy(message, MessageColor.NEGATIVE.bc()));
        t.printStackTrace();
    }

    protected void handleException(final CommandSender cs, final Command cmd, final String label, final String[] args, final Throwable t) {
        this.handleException(cs, cmd, label, args, t, "An exception occurred while processing that command.");
    }

    /**
     * The real onCommand that will be called by Bukkit. This checks for the command name to match, permissions if
     * specified, and runs the
     * {@link #runCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, String, String[])} method. If
     * any exception is unhandled in that method, it will be handled and displayed to the user in a friendly format.
     * <br>
     * Due to the nature of this class, this method cannot be overridden.
     *
     * @param cs    The CommandSender using the command
     * @param cmd   The Command being used
     * @param label The label of the command (alias)
     * @param args  The arguments passed to the command
     * @return true to not display usage, false to display usage (essentially)
     */
    @Override
    public final boolean onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        if (!cmd.getName().equalsIgnoreCase(this.name)) return false;
        if (this.checkPermissions && !this.ah.isAuthorized(cs, cmd)) {
            RUtils.dispNoPerms(cs, new String[]{this.ah.getPermission(cmd)}); // ensure calling to varargs method
            return true;
        }
        final List<Flag> expectedFlags = this.getExpectedFlags();
        if (expectedFlags.size() > 0) {
            for (final Flag f : new CommandArguments(args)) {
                if (!expectedFlags.contains(f)) {
                    if (this.helpNames.containsAll(f.getNames())) {
                        this.showFlagHelp(cs, cmd, label);
                        return true;
                    }
                    cs.sendMessage(MessageColor.NEGATIVE + "Unexpected flag \"" + f.getFirstName() + ".\"");
                    return true;
                }
            }
        }
        try {
            return this.runCommand(cs, cmd, label, args);
        } catch (final Throwable t) {
            this.handleException(cs, cmd, label, args, t);
            return true;
        }
    }

    public static class Flag<T> {

        private final List<String> names = new ArrayList<>();
        private final T value;
        private final Class<T> clazz;

        public Flag(final String... names) {
            Preconditions.checkNotNull(names, "names cannot be null");
            if (names.length < 1) throw new IllegalArgumentException("names cannot be empty");
            this.clazz = null;
            Collections.addAll(this.names, names);
            this.value = null; // used for template flags
        }

        public Flag(final Class<T> clazz, final String... names) {
            Preconditions.checkNotNull(names, "names cannot be null");
            if (names.length < 1) throw new IllegalArgumentException("names cannot be empty");
            this.clazz = clazz;
            Collections.addAll(this.names, names);
            this.value = null; // used for template flags
        }

        public Flag(final Class<T> clazz, final String[] names, final T value) {
            Preconditions.checkNotNull(names, "names cannot be null");
            if (names.length < 1) throw new IllegalArgumentException("names cannot be empty");
            this.clazz = clazz;
            Collections.addAll(this.names, names);
            this.value = value;
        }

        /**
         * A Flag is considered equal to another Flag if any of the names are the same and the value is the same. If the
         * value of this Flag is null, the values will not be checked when checking for equality.
         *
         * @param obj Object to check equality with
         * @return true if equal, false if otherwise
         */
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Flag)) return false;
            final Flag f = (Flag) obj;
            // Like, seriously. There is no way this can really be unchecked (f.getNames().containsAll()).
            //noinspection unchecked
            return !(this.clazz != null && f.getType() != null && !this.clazz.equals(f.getType())) && !(this.value != null && f.getValue() != null && !this.value.equals(f.getValue())) && (f.getNames().containsAll(this.getNames()) || this.getNames().containsAll(f.getNames()));
        }

        @Override
        public String toString() {
            return this.getNames() + " (" + this.getType() + "): " + this.getValue();
        }

        public String getFirstName() {
            if (this.names.size() < 1) return null;
            return this.names.get(0);
        }

        public List<String> getNames() {
            return this.names;
        }

        public Class<T> getType() {
            return this.clazz;
        }

        public T getValue() {
            return this.value;
        }

        public T getValue(final T defaultValue) {
            return this.value == null ? defaultValue : this.value;
        }
    }

    /**
     * A class that contains flags and their parameters, along with extra parameters.
     */
    protected class CommandArguments extends ArrayList<Flag> {

        private String[] extraParameters = new String[0];

        CommandArguments(final String[] givenArguments) {
            this.processArguments(givenArguments);
        }

        CommandArguments(final String givenArguments) {
            this(givenArguments.split(" "));
        }

        /**
         * Attempts to convert the given values to the given class.
         *
         * @param values Values to convert (will be joined if necessary)
         * @param clazz  Class to convert to
         * @param <T>    Type to return (will cast)
         * @return Converted value
         */
        @SuppressWarnings("unchecked")
        private <T> T convertFlag(final String[] values, final Class<T> clazz) {
            final String joined = StringUtils.join(values, ' ');
            if (String[].class.isAssignableFrom(clazz)) {
                return (T) values;
            } else if (String.class.isAssignableFrom(clazz)) {
                return (T) joined;
            } else if (Integer.class.isAssignableFrom(clazz)) {
                return (T) (Integer) Integer.parseInt(joined);
            } else if (Short.class.isAssignableFrom(clazz)) {
                return (T) (Short) Short.parseShort(joined);
            } else if (Long.class.isAssignableFrom(clazz)) {
                return (T) (Long) Long.parseLong(joined);
            } else if (Float.class.isAssignableFrom(clazz)) {
                return (T) (Float) Float.parseFloat(joined);
            }
            return null;
        }

        /**
         * Creates a Flag based on expected flags, the given alias, and the given arguments. If there is an expected
         * flag with a matching alias, the given arguments will be converted to the expected type or to a String if no
         * expected type is given (by using {@link #convertFlag(String[], Class)}.
         *
         * @param alias Alias of the flag
         * @param args  Arguments passed to the flag
         * @return Flag
         */
        private Flag createFlag(final String alias, final String[] args) {
            Preconditions.checkNotNull(args, "args cannot be null");
            final Flag templateFlag = new Flag(alias);
            final Flag realFlag;
            if (BaseCommand.this.expectedFlags.contains(templateFlag)) {
                realFlag = BaseCommand.this.expectedFlags.get(BaseCommand.this.expectedFlags.indexOf(templateFlag));
            } else {
                realFlag = null;
            }
            Object o;
            try {
                o = args.length < 1 ? null : this.convertFlag(args, realFlag == null ? String.class : realFlag.getType());
            } catch (final Exception ex) {
                o = null;
            }
            if (args.length < 1) {
                //noinspection unchecked
                return new Flag(null, new String[]{alias}, o);
            } else {
                //noinspection unchecked
                return new Flag(realFlag == null ? String.class : realFlag.getType(), new String[]{alias}, o);
            }
        }

        /**
         * Gets the name of the given flag. This strips the beginning hyphen or double-hyphen.
         *
         * @param s Flag
         * @return Flag name
         */
        private String getFlagName(final String s) {
            if (!this.isFlag(s)) throw new IllegalArgumentException("Not a flag.");
            return s.substring(s.length() > 2 && s.substring(1).startsWith("-") ? 2 : 1);
        }

        /**
         * Gets if this argument is a flag. This will not match the flag terminator.
         *
         * @param s Argument
         * @return If the argument is a flag
         */
        private boolean isFlag(final String s) {
            return s.startsWith("-") && !this.isFlagTerminator(s);
        }

        /**
         * Gets if the argument is the flag terminator. In this implementation, this can occur multiple times.
         *
         * @param s Argument
         * @return If the argument is the flag terminator
         */
        private boolean isFlagTerminator(final String s) {
            return "--".equals(s);
        }

        /**
         * Gets any parameters that did not belong to a flag.
         *
         * @return Extra parameters
         */
        public String[] getExtraParameters() {
            return this.extraParameters.clone();
        }

        /**
         * Gets the corresponding Flag with values.
         *
         * @param flag Flag to get parameters for
         * @return Flag with values
         */
        public <T> Flag<T> getFlag(final Flag<T> flag) {
            if (!this.contains(flag)) return null;
            //noinspection unchecked
            return this.get(this.indexOf(flag));
        }

        /**
         * Checks to see if the given flag exists and has a value.
         *
         * @param flag Flag to check for
         * @return boolean
         */
        public boolean hasContentFlag(final Flag flag) {
            final Flag storedFlag = this.getFlag(flag);
            return storedFlag != null && storedFlag.getValue() != null;
        }

        /**
         * Checks if the given Flag is set. Useful for boolean expectedFlags.
         *
         * @param flag Flag to check for
         * @return boolean
         */
        public boolean hasFlag(final Flag flag) {
            return this.getFlag(flag) != null;
        }

        /**
         * Processes additional arguments for this instance.
         *
         * @param arguments Arguments to process
         */
        public void processArguments(final String[] arguments) {
            String currentFlagName = null;
            final List<String> parameters = new ArrayList<>();
            final List<String> extraParameters = new ArrayList<>();
                for (String arg : arguments) {
                    if(!expectedFlags.isEmpty()) {
                        if (this.isFlag(arg) || this.isFlagTerminator(arg)) {
                            if (currentFlagName != null) {
                                final Flag f = this.createFlag(currentFlagName, parameters.toArray(new String[parameters.size()]));
                                this.add(f);
                            }
                            parameters.clear();
                            currentFlagName = this.isFlagTerminator(arg) ? null : this.getFlagName(arg);
                            continue;
                        }
                    }
                    arg = arg.replace("\\-", "-");
                    if (currentFlagName != null) parameters.add(arg);
                    else extraParameters.add(arg);
                }
                if (currentFlagName != null) {
                    final Flag f = this.createFlag(currentFlagName, parameters.toArray(new String[parameters.size()]));
                    this.add(f); // last arg can't be neglected
                }

            this.extraParameters = (String[]) ArrayUtils.addAll(this.extraParameters, extraParameters.toArray(new String[extraParameters.size()]));
        }
    }
}
