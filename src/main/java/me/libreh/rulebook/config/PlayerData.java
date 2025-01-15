package me.libreh.rulebook.config;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.JsonDataStorage;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerData {
    public static final PlayerDataStorage<PlayerData> STORAGE = new JsonDataStorage<>("rulebook", PlayerData.class);
    public boolean hasAccepted = false;


    public static PlayerData get(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return new PlayerData();
        }

        var data = PlayerDataApi.getCustomDataFor(serverPlayer, STORAGE);
        if (data == null) {
            data = new PlayerData();
            PlayerDataApi.setCustomDataFor(serverPlayer, STORAGE, data);
        }

        return data;
    }
}
