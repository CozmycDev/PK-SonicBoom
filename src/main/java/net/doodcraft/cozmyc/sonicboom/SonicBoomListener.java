package net.doodcraft.cozmyc.sonicboom;

import com.projectkorra.projectkorra.BendingPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SonicBoomListener implements Listener {

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) return;
        if (event.isSneaking()) {
            if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SONICBOOM")) {
                new SonicBoom(player);
            }
        }
    }
}
