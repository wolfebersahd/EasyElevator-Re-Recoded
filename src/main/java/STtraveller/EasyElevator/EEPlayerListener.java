package STtraveller.EasyElevator;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;

import STtraveller.EasyElevator.EEUtils;

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
                if (pm.has(EEPermissions.CALL_SIGN) || pm.has(EEPermissions.CALL_ALL) || pm.has(EEPermissions.CALL)) {
                    if (e.isFloorSign(sign)) {
                        this.ee.dbg("Calling elvator");
                        e.call(sign.getY());
                        EEUtils.playerMsg(player, "The Elevator has been called");
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
                if (pm.has(EEPermissions.STOP_SIGN) || pm.has(EEPermissions.STOP_ALL) || pm.has(EEPermissions.STOP)) {
                    if (e.isPlatformSign(sign)) {
                        int stop = Integer.parseInt(e.getPlatform().getSign().getLine(1));
                        this.ee.dbg("Stopping at " + String.valueOf(stop));
                        e.stopAt(stop);
                        EEUtils.playerMsg(player, "Stopping at floor " + stop);
                    }
                }
                else {
                    EEUtils.playerMsg(player, "You don't have permission to do this");
                }

                return;
            }
        }
    }
}
