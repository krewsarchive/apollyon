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
import java.lang.reflect.Field;

/**
 * Apollyon
 * The Official Camera Plugin for Morningstar. Credits to John, Beny, Ovflowd, and Alejandro
 * @author Krews.org
 */

public class Main extends HabboPlugin implements EventListener {

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
        incoming.remove(Incoming.CameraRoomThumbnailEvent);
        incoming.remove(Incoming.CameraPublishToWebEvent);
        incoming.remove(Incoming.CameraPurchaseEvent);
        incoming.remove(Incoming.CameraRoomThumbnailEvent);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraRoomPictureEvent, CameraRoomPictureEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraPublishToWebEvent, CameraPublishToWebEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraPurchaseEvent, CameraPurchaseEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraRoomThumbnailEvent, CameraRoomThumbnailEvent.class);
}


    @EventHandler
    public void onEmulatorLoadedEvent(EmulatorLoadedEvent e) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, Exception {

        // Adds missing sqls if they are not found.
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
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraRoomPictureEvent, CameraRoomPictureEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraPublishToWebEvent, CameraPublishToWebEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraPurchaseEvent, CameraPurchaseEvent.class);
        Emulator.getGameServer().getPacketManager().registerHandler(Incoming.CameraRoomThumbnailEvent, CameraRoomThumbnailEvent.class);

        // Send the message to the Emulator that PNGCamera has started.
        Emulator.getLogging().logStart("[Apollyon] Apollyon Preview 1 has hijacked the Arcturus Camera packets, and is ready to use!!");
        Emulator.getLogging().logStart("[Apollyon] Please ensure this plugin was downloaded from Krews.org and is the latest version.");

    }

}
