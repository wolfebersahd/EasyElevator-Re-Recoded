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

    public EEPlayerListener(EasyElevator e)
    {
        this.ee = e;
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

            if (true) {
                this.ee.getLogger().info("A player has right-clicked an elevator sign!");
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if ((pm.has("easyelevator.call.sign")) || (pm.has("easyelevator.call.*"))) {
                    if (e.isFloorSign(sign))
                    {
                        e.Call(sign.getY());
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator has been called");
                        return;
                    }
                }
                if (e.isPlatformSign(sign))
                {
                    e.changeFloor();
                    return;
                }
            }

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if ((pm.has("easyelevator.stop.sign")) || (pm.has("easyelevator.stop.*")))
                {
                    if (e.isPlatformSign(sign))
                    {
                        e.StopAt(Integer.parseInt(e.getPlatform().getSign().getLine(1)));
                        player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "Stopping at floor " + Integer.parseInt(e.getPlatform().getSign().getLine(1)));
                    }
                }
                else {
                    player.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "You don't have permission to do this");
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockRedstoneEvent event) {}
}
