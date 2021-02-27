package org.krews.apollyon.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraURLComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.util.ReferenceCountUtil;
import org.krews.apollyon.ftp.FTPUploadService;
import org.krews.apollyon.utils.PngSignatureChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.IllegalArgumentException;

public class CameraRoomPictureEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraRoomPictureEvent.class);

    @Override
    public void handle() {
        if (!this.client.getHabbo().hasPermission("acc_camera")) {
            this.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("camera.permission")));
            return;
        }

        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        final int count = this.packet.readInt();

        ByteBuf image = this.packet.getBuffer().readBytes(count);
        ByteBuf imageCopy = image.copy();

        if (image == null)
            return;

        try {
            byte[] imageBytes = new byte[image.readableBytes()];
            image.readBytes(imageBytes);

            if (PngSignatureChecker.isPngFile(imageBytes)) {
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
                CameraPurchaseEvent lol = new CameraPurchaseEvent();
                lol.lastRanTimestamps.put(this.client.getHabbo(), Emulator.getIntUnixTimestamp());

                try {
                    if (Emulator.getConfig().getInt("ftp.enabled") == 1) {
                        FTPUploadService.uploadImage(imageBytes, Emulator.getConfig().getValue("imager.location.output.camera") + URL);
                        FTPUploadService.uploadImage(imageBytes, Emulator.getConfig().getValue("imager.location.output.camera") + URL_small);
                    } else {
                        BufferedImage theImage = ImageIO.read(new ByteBufInputStream(imageCopy));
                        ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL));
                        ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL_small));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    LOGGER.error("[Apollyon] You are using a Habbo.swf that has not been patched to work with Apollyon. Please read the read me on a guide to patching your swf, or download a prepatched one on our git at:");
                    LOGGER.error("[Apollyon] https://git.krews.org/morningstar/apollyon");
                }

                this.client.sendResponse(new CameraURLComposer(URL));
            }
        } finally {
            ReferenceCountUtil.release(image);
            ReferenceCountUtil.release(imageCopy);
        }
    }
}
