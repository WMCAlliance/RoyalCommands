/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.royaldev.royalcommands.rcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.royaldev.royalcommands.MessageColor;
import org.royaldev.royalcommands.RUtils;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.exceptions.InvalidItemNameException;

@ReflectCommand
public class CmdRecipe extends TabCommand {

    private final Map<String, Integer> tasks = new HashMap<>();

    public CmdRecipe(final RoyalCommands instance, final String name) {
        super(instance, name, true, new Short[]{CompletionType.ITEM_ALIAS.getShort()});
        this.plugin.getServer().getPluginManager().registerEvents(new WorkbenchCloseListener(), this.plugin);
    }

    private void cancelTask(final Player p) {
        if (!this.tasks.containsKey(p.getName())) return;
        final int taskID = this.tasks.get(p.getName());
        if (taskID != -1) this.plugin.getServer().getScheduler().cancelTask(taskID);
        this.tasks.remove(p.getName());
    }

    private void scheduleRecipeTask(final Player p, ItemStack is) {
        final List<Recipe> rs = this.plugin.getServer().getRecipesFor(is);
        if (rs.size() < 1) {
            p.sendMessage(MessageColor.NEGATIVE + "No recipes for that item!");
            return;
        }
        final List<Inventory> workbenches = new ArrayList<>();
        for (final Recipe r : rs) {
            final Inventory i;
            if (r instanceof ShapedRecipe) {
                i = this.plugin.getServer().createInventory(new RecipeHolder(), InventoryType.WORKBENCH);
                final ShapedRecipe sr = (ShapedRecipe) r;
                final Map<Character, ItemStack> im = sr.getIngredientMap();
                final String[] lines = sr.getShape();
                for (int lineNum = 0; lineNum < lines.length; lineNum++) {
                    final String line = lines[lineNum];
                    for (int slot = 1; slot <= 3; slot++) {
                        if (slot > line.length()) continue;
                        final ItemStack slotItem = im.get(line.charAt(slot - 1));
                        if (slotItem == null) continue;
                        i.setItem(slot + (lineNum * 3), slotItem);
                    }
                }
                i.setItem(0, sr.getResult());
            } else if (r instanceof ShapelessRecipe) {
                i = this.plugin.getServer().createInventory(new RecipeHolder(), InventoryType.WORKBENCH);
                final ShapelessRecipe sr = (ShapelessRecipe) r;
                final List<ItemStack> ingredients = sr.getIngredientList();
                for (int slot = 1; slot <= ingredients.size(); slot++) {
                    if (slot > ingredients.size()) continue;
                    i.setItem(slot, ingredients.get(slot - 1));
                }
                i.setItem(0, sr.getResult());
            } else if (r instanceof FurnaceRecipe) {
                i = this.plugin.getServer().createInventory(new RecipeHolder(), InventoryType.FURNACE);
                final FurnaceRecipe fr = (FurnaceRecipe) r;
                i.setItem(0, fr.getInput());
                i.setItem(2, fr.getResult());
            } else continue;
            for (ItemStack ist : i.getContents()) {
                if (ist == null) continue;
                Damageable im = (Damageable) ist.getItemMeta();
                if (im.getDamage() == (short) 32767) {
                    this.plugin.getLogger().log(Level.WARNING, "RECIPE: Durability of item {0} is invalid.", ist.getType());
                    im.setDamage((short) 0);
                }
            }
            workbenches.add(i);
        }
        final Runnable r = new Runnable() {
            private int currentRecipe = 0;
            private boolean display = true;

            private void setClosing(boolean closing) {
                final InventoryHolder ih = p.getOpenInventory().getTopInventory().getHolder();
                if (!(ih instanceof RecipeHolder)) return;
                final RecipeHolder rh = (RecipeHolder) ih;
                rh.setClosing(closing);
            }

            @Override
            public void run() {
                // let's not open new workbenches, as that can cause the items to disappear
                if (!this.display) return;
                if (!CmdRecipe.this.tasks.containsKey(p.getName())) return;
                if (this.currentRecipe >= workbenches.size()) this.currentRecipe = 0;
                this.setClosing(true);
                p.openInventory(workbenches.get(this.currentRecipe));
                this.setClosing(false);
                this.currentRecipe++;
                if (workbenches.size() == 1) this.display = false;
            }
        };
        int taskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, r, 0L, 30L);
        if (taskID == -1) {
            p.sendMessage(MessageColor.NEGATIVE + "Could not schedule task!");
            return;
        }
        this.cancelTask(p);
        this.tasks.put(p.getName(), taskID);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(MessageColor.NEGATIVE + "This command is only available to players!");
            return true;
        }
        final Player p = (Player) cs;
        if (eargs.length < 1 && p.getInventory().getItemInMainHand().getType() == Material.AIR) {
            cs.sendMessage(cmd.getDescription());
            return false;
        }
        ItemStack is;
        if (eargs.length < 1 || eargs[0].equalsIgnoreCase("hand")) {
            is = p.getInventory().getItemInMainHand();
        } else {
            try {
                is = RUtils.getItemFromAlias(eargs[0], 1);
            } catch (InvalidItemNameException e) {
                is = RUtils.getItem(eargs[0], 1);
            } catch (NullPointerException e) {
                cs.sendMessage(MessageColor.NEGATIVE + "ItemNameManager was not loaded. Let an administrator know.");
                return true;
            }
        }
        if (is == null) {
            cs.sendMessage(MessageColor.NEGATIVE + "Invalid item name!");
            return true;
        }
        this.scheduleRecipeTask(p, is);
        return true;
    }


    private class WorkbenchCloseListener implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void workbenchClick(InventoryClickEvent e) {
            if (!(e.getWhoClicked() instanceof Player)) return;
            final ItemStack is = e.getCurrentItem();
            if (is == null || is.getType() == Material.AIR) return;
            final InventoryType it = e.getInventory().getType();
            if (it != InventoryType.WORKBENCH && it != InventoryType.FURNACE) return;
            if (!(e.getInventory().getHolder() instanceof RecipeHolder)) return;
            e.setCancelled(true);
            if (!(e.getWhoClicked() instanceof Player)) return;
            final Player p = (Player) e.getWhoClicked();
            CmdRecipe.this.scheduleRecipeTask(p, is);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void workbenchClose(InventoryCloseEvent e) {
            if (!(e.getPlayer() instanceof Player)) return;
            final Player p = (Player) e.getPlayer();
            final InventoryType it = e.getInventory().getType();
            if (it != InventoryType.WORKBENCH && it != InventoryType.FURNACE) return;
            if (!CmdRecipe.this.tasks.containsKey(p.getName())) return;
            if (!(e.getInventory().getHolder() instanceof RecipeHolder)) return;
            final RecipeHolder rh = (RecipeHolder) e.getInventory().getHolder();
            if (rh.isClosing()) return;
            CmdRecipe.this.cancelTask(p);
        }
    }

    private class RecipeHolder implements InventoryHolder {

        private boolean closing = false;

        private boolean isClosing() {
            return this.closing;
        }

        private void setClosing(boolean closing) {
            this.closing = closing;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
