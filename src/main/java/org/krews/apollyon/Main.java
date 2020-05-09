package org.krews.apollyon;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.PacketManager;
import com.eu.habbo.messages.incoming.Incoming;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import gnu.trove.map.hash.THashMap;
import org.krews.apollyon.incoming.CameraPublishToWebEvent;
import org.krews.apollyon.incoming.CameraPurchaseEvent;
import org.krews.apollyon.incoming.CameraRoomPictureEvent;
import org.krews.apollyon.incoming.CameraRoomThumbnailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;


/**
 * Apollyon
 * The Official Camera Plugin for Morningstar. Credits to John, Beny, Ovflowd, and Alejandro
 * @author Krews.org
 */

public class Main extends HabboPlugin implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Emulator.class);
    @Override
    public void onEnable() throws Exception {
        Emulator.getPluginManager().registerEvents(this, this);

        if(Emulator.isReady && !Emulator.isShuttingDown) {
            this.onEmulatorLoadedEvent(null);
        }
    }

    @Override
    public boolean hasPermission(Habbo habbo, String string) {
        return false;
    }

    @Override
    public void onDisable() throws Exception {
        // put the original packets back
        PacketManager packetManager = Emulator.getGameServer().getPacketManager();
        Field f = PacketManager.class.getDeclaredField("incoming");
        f.setAccessible(true);
        THashMap<Integer, Class<? extends MessageHandler>> incoming = (THashMap<Integer, Class<? extends MessageHandler>>)f.get(packetManager);
        incoming.remove(Incoming.CameraRoomPictureEvent, CameraRoomPictureEvent.class);
        incoming.remove(Incoming.CameraPublishToWebEvent, CameraPublishToWebEvent.class);
        incoming.remove(Incoming.CameraPurchaseEvent, CameraPurchaseEvent.class);
        incoming.remove(Incoming.CameraRoomThumbnailEvent, CameraRoomThumbnailEvent.class);

    }

    @EventHandler
    public void onEmulatorLoadedEvent(EmulatorLoadedEvent e) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Exception {

        // Adds missing sqls if they are not found.
        Emulator.getConfig().register("apollyon.cooldown.amount", "250");
        Emulator.getConfig().register("ftp.enabled", "0");
        Emulator.getConfig().register("ftp.user", "root");
        Emulator.getConfig().register("ftp.password", "password123");
        Emulator.getConfig().register("ftp.host", "example.com");
        Emulator.getConfig().register("camera.url", "http://yourdomain.com/swfdirectory/camera/");
        Emulator.getConfig().register("imager.location.output.camera", "C:\\yourdirectory\\swfdirectory\\camera\\");
        Emulator.getConfig().register("imager.location.output.thumbnail", "C:\\yourdirectory\\swfdirectory\\camera\\thumbnails\\thumbnail_");

        PacketManager packetManager = Emulator.getGameServer().getPacketManager();
        Field f = PacketManager.class.getDeclaredField("incoming");
        f.setAccessible(true);
        THashMap<Integer, Class<? extends MessageHandler>> incoming = (THashMap<Integer, Class<? extends MessageHandler>>)f.get(packetManager);

        // Removes the current arcturus handlers for these packets
        incoming.remove(Incoming.CameraRoomPictureEvent);
        incoming.remove(Incoming.CameraPublishToWebEvent);
        incoming.remove(Incoming.CameraPurchaseEvent);
        incoming.remove(Incoming.CameraRoomThumbnailEvent);

        // Adds the PNGCamera Packet Handlers
        packetManager.registerHandler(Incoming.CameraRoomPictureEvent, CameraRoomPictureEvent.class);
        packetManager.registerHandler(Incoming.CameraPublishToWebEvent, CameraPublishToWebEvent.class);
        packetManager.registerHandler(Incoming.CameraPurchaseEvent, CameraPurchaseEvent.class);
        packetManager.registerHandler(Incoming.CameraRoomThumbnailEvent, CameraRoomThumbnailEvent.class);

        // Send the message to the Emulator that PNGCamera has started.
        LOGGER.info("Official Plugin - Rawr. Apollyon Preview 2 has officially loaded!");

    }

}
