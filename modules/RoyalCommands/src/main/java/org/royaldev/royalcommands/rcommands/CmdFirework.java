/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import static org.royaldev.royalcommands.Converters.toInt;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RoyalCommands;

@ReflectCommand
public class CmdFirework extends TabCommand {

    private final Flag<String> fadeFlag = new Flag<>(String.class, "fade");
    private final Flag<Integer> powerFlag = new Flag<>(Integer.class, "power", "p");
    private final Flag<String> colorFlag = new Flag<>(String.class, "color", "c");
    private final Flag<String> shapeFlag = new Flag<>(String.class, "shape", "s", "type", "t");
    private final Flag flickerFlag = new Flag("flicker", "sparkle", "twinkle");
    private final Flag trailFlag = new Flag("trail");

    public CmdFirework(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{});
        this.addExpectedFlag(this.fadeFlag);
        this.addExpectedFlag(this.powerFlag);
        this.addExpectedFlag(this.colorFlag);
        this.addExpectedFlag(this.shapeFlag);
        this.addExpectedFlag(this.flickerFlag);
        this.addExpectedFlag(this.trailFlag);
    }

    /**
     * Takes an array of tags and applies them to a FireworkMeta.
     *
     * @param args Array of tags
     * @param fm   FireworkMeta to apply tags to
     * @return Modified FireworkMeta
     * @throws IllegalArgumentException If one tag is invalid (message is error to be displayed to user)
     */
    private FireworkMeta applyEffect(String[] args, FireworkMeta fm) throws IllegalArgumentException {
        Builder feb = FireworkEffect.builder();
        final CommandArguments ca = this.getCommandArguments(args);
        if (ca.hasFlag(this.fadeFlag)) {
            final Flag<String> fadeFlag = ca.getFlag(this.fadeFlag);
            final Color c = this.getColor(fadeFlag.getValue());
            if (c == null) throw new IllegalArgumentException("Invalid fade color!");
            feb.withFade(c);
        }
        if (ca.hasFlag(this.powerFlag)) {
            final Flag<Integer> powerFlag = ca.getFlag(this.powerFlag);
            final Integer power = powerFlag.getValue();
            if (power == null) throw new IllegalArgumentException("Invalid power!");
            if (power < 0 || power > 128) throw new IllegalArgumentException("Power must be between 0 and 128!");
            fm.setPower(power);
        }
        if (ca.hasFlag(this.colorFlag)) {
            final Flag<String> colorFlag = ca.getFlag(this.colorFlag);
            final Color c = this.getColor(colorFlag.getValue());
            if (c == null) throw new IllegalArgumentException("Invalid color!");
            feb.withColor(c);
        }
        if (ca.hasFlag(this.shapeFlag)) {
            final Flag<String> shapeFlag = ca.getFlag(this.shapeFlag);
            final Type t = this.getShape(shapeFlag.getValue());
            if (t == null) throw new IllegalArgumentException("Invalid shape!");
            feb.with(t);
        }
        if (ca.hasFlag(this.flickerFlag)) feb.withFlicker();
        if (ca.hasFlag(this.trailFlag)) feb.withTrail();
        final FireworkEffect fe;
        try {
            fe = feb.build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error: " + MessageColor.NEUTRAL + e.getMessage());
        }
        fm.addEffect(fe);
        return fm;
    }

    private Color getColor(String c) {
        if (c == null) throw new IllegalArgumentException("You must provide a color.");
        if (c.startsWith("rgb:")) {
            c = c.substring(4);
            if (c.contains(",")) {
                String[] colors = c.split(",");
                if (colors.length < 3) return Color.WHITE;
                int r = toInt(colors[0]);
                int g = toInt(colors[1]);
                int b = toInt(colors[2]);
                if (r < 0 || g < 0 || b < 0) {
                    return null;
                }
                return Color.fromRGB(r, g, b);
            }
            return Color.fromRGB(toInt(c.substring(4)));
        } else if (c.startsWith("bgr:")) {
            c = c.substring(4);
            if (c.contains(",")) {
                String[] colors = c.split(",");
                if (colors.length < 3) return Color.WHITE;
                int b = toInt(colors[0]);
                int g = toInt(colors[1]);
                int r = toInt(colors[2]);
                if (r < 0 || g < 0 || b < 0) {
                    return null;
                }
                return Color.fromBGR(b, g, r);
            }
            return Color.fromBGR(toInt(c.substring(4)));
        }
        if (c.equalsIgnoreCase("blue")) {
            return Color.BLUE;
        } else if (c.equalsIgnoreCase("red")) {
            return Color.RED;
        } else if (c.equalsIgnoreCase("green")) {
            return Color.GREEN;
        } else if (c.equalsIgnoreCase("white")) {
            return Color.WHITE;
        } else if (c.equalsIgnoreCase("yellow")) {
            return Color.YELLOW;
        } else if (c.equalsIgnoreCase("aqua")) {
            return Color.AQUA;
        } else if (c.equalsIgnoreCase("black")) {
            return Color.BLACK;
        } else if (c.equalsIgnoreCase("teal")) {
            return Color.TEAL;
        } else if (c.equalsIgnoreCase("purple")) {
            return Color.PURPLE;
        } else if (c.equalsIgnoreCase("silver")) {
            return Color.SILVER;
        } else if (c.equalsIgnoreCase("fuchsia")) {
            return Color.FUCHSIA;
        } else if (c.equalsIgnoreCase("orange")) {
            return Color.ORANGE;
        } else if (c.equalsIgnoreCase("gray")) {
            return Color.GRAY;
        } else if (c.equalsIgnoreCase("olive")) {
            return Color.OLIVE;
        } else if (c.equalsIgnoreCase("maroon")) {
            return Color.MAROON;
        } else if (c.equalsIgnoreCase("lime")) {
            return Color.LIME;
        } else if (c.equalsIgnoreCase("navy")) {
            return Color.NAVY;
        }
        return null;
    }

    private Type getShape(String c) {
        if (c == null) throw new IllegalArgumentException("You must provide a shape.");
        if (c.equalsIgnoreCase("ball") || c.equalsIgnoreCase("small_ball") || c.equalsIgnoreCase("ball_small")) {
            return Type.BALL;
        } else if (c.equalsIgnoreCase("large_ball") || c.equalsIgnoreCase("ball_large") || c.equalsIgnoreCase("big_ball") || c.equalsIgnoreCase("ball_big")) {
            return Type.BALL_LARGE;
        } else if (c.equalsIgnoreCase("star")) {
            return Type.STAR;
        } else if (c.equalsIgnoreCase("head") || c.equalsIgnoreCase("creeper") || c.equalsIgnoreCase("creeper_head")) {
            return Type.CREEPER;
        } else if (c.equalsIgnoreCase("burst")) {
            return Type.BURST;
        }
        return null;
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, CommandArguments ca) {
        if (args.length < 1) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        Player p = (Player) cs;
        ItemStack is = p.getInventory().getItemInMainHand();
        if (is.getType() != Material.FIREWORK_ROCKET) {
            cs.sendMessage(MessageColor.NEGATIVE + "The item in hand is not a firework!");
            return true;
        }
        FireworkMeta fm = (FireworkMeta) is.getItemMeta();
        if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("remove")) {
            if (args.length > 1) {
                int effectToRemove;
                try {
                    effectToRemove = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    cs.sendMessage(MessageColor.NEGATIVE + "The specified effect was not a number!");
                    return true;
                }
                effectToRemove--; // the first effect is really 0, but users will enter 1, so remove 1 from user input
                if (effectToRemove < 0 || effectToRemove >= fm.getEffectsSize()) {
                    cs.sendMessage(MessageColor.NEGATIVE + "No such effect!");
                    return true;
                }
                fm.removeEffect(effectToRemove);
                is.setItemMeta(fm);
                cs.sendMessage(MessageColor.POSITIVE + "Removed effect " + MessageColor.NEUTRAL + (effectToRemove + 1) + MessageColor.POSITIVE + ".");
                return true;
            }
            fm.clearEffects();
            is.setItemMeta(fm);
            cs.sendMessage(MessageColor.POSITIVE + "Cleared all firework effects.");
            return true;
        }
        try {
            fm = applyEffect(args, fm);
        } catch (IllegalArgumentException e) {
            cs.sendMessage(MessageColor.NEGATIVE + e.getMessage());
            return true;
        }
        is.setItemMeta(fm);
        cs.sendMessage(MessageColor.POSITIVE + "Added effect to firework!");
        return true;
    }
}
