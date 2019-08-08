package org.krews.apollyon.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraURLComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.krews.apollyon.ftp.FTPUploadService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.lang.IllegalArgumentException;

public class CameraRoomPictureEvent extends MessageHandler
{
    @Override
    public void handle() {
        if (Emulator.getIntUnixTimestamp() - this.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
            this.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
            if (!this.client.getHabbo().hasPermission("acc_camera")) {
                this.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("camera.permission")));
                return;
            }

            Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

            if (room == null)
                return;

            final int count = this.packet.readInt();

            ByteBuf image = this.packet.getBuffer().readBytes(count);

            if (image == null)
                return;
            this.packet.readString();
            this.packet.readString();
            this.packet.readInt();
            this.packet.readInt();
            int timestamp = Emulator.getIntUnixTimestamp();

            String URL = this.client.getHabbo().getHabboInfo().getId() + "_" + timestamp + ".png";
            String URL_small = this.client.getHabbo().getHabboInfo().getId() + "_" + timestamp + "_small.png";
            String base = Emulator.getConfig().getValue("camera.url");
            String json = Emulator.getConfig().getValue("camera.extradata").replace("%timestamp%", timestamp + "").replace("%room_id%", room.getId() + "").replace("%url%", base + URL);
            this.client.getHabbo().getHabboInfo().setPhotoURL(base + URL);
            this.client.getHabbo().getHabboInfo().setPhotoTimestamp(timestamp);
            this.client.getHabbo().getHabboInfo().setPhotoRoomId(room.getId());
            this.client.getHabbo().getHabboInfo().setPhotoJSON(json);

            try {
                BufferedImage theImage = ImageIO.read(new ByteBufInputStream(image));
                if(Emulator.getConfig().getInt("ftp.enabled") == 1) {
                    byte[] imageBytes = ((DataBufferByte) theImage.getData().getDataBuffer()).getData();
                    FTPUploadService.uploadImage(imageBytes, Emulator.getConfig().getValue("imager.location.output.camera") + URL);
                    FTPUploadService.uploadImage(imageBytes, Emulator.getConfig().getValue("imager.location.output.camera") + URL_small);
                }
                else {
                    ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL));
                    ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL_small));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.out.println("[Apollyon] You are using a Habbo.swf that has not been patched to work with Apollyon. Please read the read me on a guide to patching your swf, or download a prepatched one on our git at:");
                System.out.println("[Apollyon] https://git.krews.org/morningstar/apollyon");
            }

            this.client.sendResponse(new CameraURLComposer(URL));
        }
    }
}