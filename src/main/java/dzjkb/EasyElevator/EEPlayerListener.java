package dzjkb.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;

public class EEPlayerListener
        implements Listener
{
    EasyElevator ee;
    private EEConfiguration config;

    public EEPlayerListener(EasyElevator e, EEConfiguration cfg)
    {
        this.ee = e;
        this.config = cfg;
    }

    private void dbg(String msg) {
        if (this.config.debug) {
            this.ee.getLogger().info(msg);
        }
    }

    public void updateConfig(EEConfiguration newCfg) {
        this.config = newCfg;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Block clicked = event.getClickedBlock();

        if (clicked != null && clicked.getType() == Material.SIGN) {
            Player player = event.getPlayer();
            EEPermissionManager pm = new EEPermissionManager(player);
            Sign sign = (Sign)clicked.getState();
            Elevator e = this.ee.getElevators().getElevator(sign);

            if (e == null)
                return;

            dbg("A player has clicked an elevator sign!");

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                dbg("It's a right click!");
                if ((pm.has("easyelevator.call.sign")) || (pm.has("easyelevator.call.*"))) {
                    if (e.isFloorSign(sign)) {
                        dbg("Calling elvator");
                        e.call(sign.getY());
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
                        return;
                    }
                }
                if (e.isPlatformSign(sign)) {
                    e.changeFloor();
                    return;
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                dbg("It's a left click!");
                if ((pm.has("easyelevator.stop.sign")) || (pm.has("easyelevator.stop.*"))) {
                    if (e.isPlatformSign(sign)) {
                        int stop = Integer.parseInt(e.getPlatform().getSign().getLine(1));
                        dbg("Stopping at " + String.valueOf(stop));
                        e.stopAt(stop);
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + stop);
                    }
                }
                else {
                    player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
                }
            }
        }
    }

    // @EventHandler
    // public void onBlockPlace(BlockRedstoneEvent event) {}
}
