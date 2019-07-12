package dzjkb.EasyElevator;

import org.bukkit.entity.Player;

import dzjkb.EasyElevator.EEPermissions;

public class EEPermissionManager
{
    Player player;

    public EEPermissionManager(Player p)
    {
        this.player = p;
    }

    public boolean has(String permission)
    {
        if (isAdmin()) {
            return true;
        }
        return this.player.hasPermission(permission);
    }

    private boolean isAdmin()
    {
        if (this.player.isOp()) {
            return true;
        }
        return this.player.hasPermission(EEPermissions.ADMIN);
    }
}
