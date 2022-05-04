/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Borrowed from https://github.com/kernitus/BukkitOldCombatMechanics
package org.royaldev.royalcommands.attribute.util;


public enum ClassType {

    NMS {
        @Override
        public String qualifyClassName(String partialName){
            if(Reflector.versionIsNewerOrEqualAs(1, 17, 0)){
                System.out.print("net.minecraft." + partialName);
                return "net.minecraft." + partialName;
            }
            // FIXME: Assumes class names are upper case and trims off the preceding package name
            // entity.foo.Bar.Test
            // ^^^^^^^^^^ ^^^^^^^^^
            //   Group 1   Group 2
            String className = partialName.replaceAll("([a-z.]+)\\.([a-zA-Z.]+)", "$2");
            System.out.print("net.minecraft.server." + Reflector.getVersion() + "." + className);
            return "net.minecraft.server." + Reflector.getVersion() + "." + className;
        }
    },
    CRAFTBUKKIT {
        @Override
        public String qualifyClassName(String partialName){
            System.out.printf("%s.%s.%s", "org.bukkit.craftbukkit", Reflector.getVersion(), partialName);
            return String.format("%s.%s.%s", "org.bukkit.craftbukkit", Reflector.getVersion(), partialName);
        }
    };

    public abstract String qualifyClassName(String partialName);
}