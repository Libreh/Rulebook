package me.libreh.rulebook.services;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerService {
    private final Set<UUID> joinedPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> rulebookPlayers = ConcurrentHashMap.newKeySet();

    public void onPlayerJoin(UUID playerUuid) {
        joinedPlayers.add(playerUuid);
    }

    public boolean hasPlayerJoined(UUID playerUuid) {
        return joinedPlayers.contains(playerUuid);
    }

    public void addRulebookPlayer(UUID playerUuid) {
        rulebookPlayers.add(playerUuid);
    }

    public boolean isPlayerViewingRulebook(UUID playerUuid) {
        return rulebookPlayers.contains(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        joinedPlayers.remove(playerUuid);
        rulebookPlayers.remove(playerUuid);
    }
} 