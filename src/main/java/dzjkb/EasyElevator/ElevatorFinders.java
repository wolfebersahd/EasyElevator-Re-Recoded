package dzjkb.EasyElevator;

import java.util.List;
import java.lang.Math;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import dzjkb.EasyElevator.Elevator;
import dzjkb.EasyElevator.EEUtils;

public class ElevatorFinders {

    public static Sign getSurroundingElevatorSign(List<Elevator> elvs, Player player)
    {
        Location loc = player.getLocation();

        // TODO what does this actually do
        Location l1 = loc;
        Location l2 = loc;

        if (!l1.equals(l2)) System.out.println("[EasyElevator] What the f");

        int x1 = l1.getBlockX();
        int y1 = l1.getBlockY();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int y2 = l2.getBlockY();
        int z2 = l2.getBlockZ();

        int xStart = Math.min(x1, x2) - 5;
        int xEnd = Math.max(x1, x2) + 5;
        int yStart = Math.min(y1, y2);
        int yEnd = Math.max(y1, y2) + 2;
        int zStart = Math.min(z1, z2) - 5;
        int zEnd = Math.max(z1, z2) + 5;
        // TODO end

        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                for (int z = zStart; z <= zEnd; z++) {
                    Block b = player.getWorld().getBlockAt(x, y, z);
                    if ((b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN)))
                    { 
                        Sign sign = (Sign)b.getState();
                        // TODO replace with isEESign?
                        // if (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]") &&
                        if (EEUtils.isEESign(sign) && !isAnyPlatformSign(elvs, sign)) {
                            return sign;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isAnyPlatformSign(List<Elevator> es, Sign s) {
        for (Elevator e : es) {
            if (e.getPlatform().getSign().equals(s)) {
                return true;
            }
        }

        return false;
    }

    public static Elevator getElevator(List<Elevator> elvs, Sign sign)
    {
        if (EEUtils.isEESign(sign))
        {
            Elevator e = null;
            for (int i = 0; i < elvs.size(); i++)
            {
                org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
                Block attached = sign.getBlock().getRelative(signData.getAttachedFace());
                if (((Elevator)elvs.get(i)).isPartOfElevator(attached.getLocation())) {
                    if ((((Elevator)elvs.get(i)).isFloorSign(sign)) || (((Elevator)elvs.get(i)).isPlatformSign(sign)))
                    {
                        e = elvs.get(i);
                        i = elvs.size();
                    }
                }
            }
            if (e == null) {
                e = new Elevator(this.ee, sign);
            }
            if (e != null) {
                if (e.isInitialized())
                {
                    if (!elvs.contains(e)) {
                        elvs.add(e);
                    }
                    return e;
                }
            }
        }
        return null;
    }
}