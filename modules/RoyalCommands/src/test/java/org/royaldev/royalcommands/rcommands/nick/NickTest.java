package org.royaldev.royalcommands.rcommands.nick;

import org.junit.Test;
import org.royaldev.royalcommands.RoyalCommands;
import org.royaldev.royalcommands.wrappers.player.MemoryRPlayer;
import org.royaldev.royalcommands.wrappers.player.RPlayer;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class NickTest {

    private final Nick nick = new Nick(this.makeRPlayer());

    private RPlayer makeRPlayer() {
        RoyalCommands.dataFolder = new File(System.getProperty("java.io.tmpdir"));
        final RPlayer rp = mock(MemoryRPlayer.class);
        when(rp.getUUID()).thenReturn(UUID.randomUUID());
        when(rp.getPlayerConfiguration()).thenCallRealMethod();
        return rp;
    }

    @Test
    public void testClear() throws Exception {
        this.nick.set("Yo");
        this.nick.clear();
        assertNull(this.nick.get());
    }

    @Test
    public void testGetAndSet() throws Exception {
        final String nick = "Hallo.";
        this.nick.set(nick);
        assertEquals(nick, this.nick.get());
    }

    @Test
    public void testGetLastUpdate() throws Exception {
        final long last = System.currentTimeMillis();
        this.nick.set("Oh god");
        assertEquals(last, this.nick.getLastUpdate(), 5);
    }

    @Test
    public void testSetLastUpdate() throws Exception {
        final long last = System.currentTimeMillis();
        this.nick.setLastUpdate();
        assertEquals(last, this.nick.getLastUpdate());
    }

    @Test
    public void testSetLastUpdate1() throws Exception {
        this.nick.setLastUpdate(0L);
        assertEquals(0L, this.nick.getLastUpdate());
    }
}
