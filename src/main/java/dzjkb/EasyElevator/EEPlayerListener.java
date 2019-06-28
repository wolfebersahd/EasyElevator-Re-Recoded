package dzjkb.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
// import org.bukkit.event.block.BlockRedstoneEvent;
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

    public void updateConfig(EEConfiguration newCfg) {
        this.config = newCfg;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Block clicked = event.getClickedBlock();

        if (clicked != null && clicked.getType() == Material.WALL_SIGN) {
            Player player = event.getPlayer();
            EEPermissionManager pm = new EEPermissionManager(player);
            Sign sign = (Sign)clicked.getState();
            Elevator e = this.ee.getElevators().getElevator(sign);

            if (e == null)
                return;

            this.ee.dbg("A player has clicked an elevator sign!");

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                this.ee.dbg("It's a right click!");
                if ((pm.has("easyelevator.call.sign")) || (pm.has("easyelevator.call.*"))) {
                    if (e.isFloorSign(sign)) {
                        this.ee.dbg("Calling elvator");
                        e.call(sign.getY());
                        playerMsg(player, "The Elevator has been called");
                        return;
                    }
                }
                if (e.isPlatformSign(sign)) {
                    e.changeFloor();
                }

                return;
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                this.ee.dbg("It's a left click!");
                if ((pm.has("easyelevator.stop.sign")) || (pm.has("easyelevator.stop.*"))) {
                    if (e.isPlatformSign(sign)) {
                        int stop = Integer.parseInt(e.getPlatform().getSign().getLine(1));
                        this.ee.dbg("Stopping at " + String.valueOf(stop));
                        e.stopAt(stop);
                        playerMsg(player, "Stopping at floor " + stop);
                    }
                }
                else {
                    playerMsg(player, "You don't have permission to do this");
                }

                return;
            }
        }
    }

    private void playerMsg(Player player, String msg) {
        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + msg);
    }

    // @EventHandler
    // public void onBlockPlace(BlockRedstoneEvent event) {}
}
