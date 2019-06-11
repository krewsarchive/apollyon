package org.krews.apollyon.incoming;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPurchaseSuccesfullComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;

public class CameraPurchaseEvent extends MessageHandler
{
    @Override
    public void handle() {
        if (Emulator.getIntUnixTimestamp() - this.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
            this.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
            if (ShutdownEmulator.timestamp > 0) {
                this.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            } else if (this.client.getHabbo().getHabboInfo().getCredits() < Emulator.getConfig().getInt("camera.price.credits") || this.client.getHabbo().getHabboInfo().getCurrencyAmount(0) < Emulator.getConfig().getInt("camera.price.points")) {
                this.client.sendResponse(new NotEnoughPointsTypeComposer(this.client.getHabbo().getHabboInfo().getCredits() < Emulator.getConfig().getInt("camera.price.credits"), this.client.getHabbo().getHabboInfo().getCurrencyAmount(0) < Emulator.getConfig().getInt("camera.price.points"), 0));
            } else if (this.client.getHabbo().getHabboInfo().getPhotoTimestamp() != 0) {
                HabboItem photoItem = Emulator.getGameEnvironment().getItemManager().createItem(this.client.getHabbo().getHabboInfo().getId(), Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("camera.item_id")), 0, 0, this.client.getHabbo().getHabboInfo().getPhotoJSON());

                if (photoItem != null) {
                    photoItem.setExtradata(photoItem.getExtradata().replace("%id%", photoItem.getId() + ""));
                    photoItem.needsUpdate(true);
                    this.client.getHabbo().getInventory().getItemsComponent().addItem(photoItem);

                    this.client.sendResponse(new CameraPurchaseSuccesfullComposer());
                    this.client.sendResponse(new AddHabboItemComposer(photoItem));
                    this.client.sendResponse(new InventoryRefreshComposer());

                    this.client.getHabbo().giveCredits(-Emulator.getConfig().getInt("camera.price.credits"));
                    this.client.getHabbo().givePixels(-Emulator.getConfig().getInt("camera.price.points"));

                    AchievementManager.progressAchievement(this.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("CameraPhotoCount"));
                }
            }
        }
        else
        {
            this.client.sendResponse(new AlertPurchaseFailedComposer(AlertPurchaseFailedComposer.SERVER_ERROR).compose());
        }
    }
}