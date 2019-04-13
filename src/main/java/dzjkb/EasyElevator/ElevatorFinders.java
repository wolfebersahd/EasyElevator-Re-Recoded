package dzjkb.EasyElevator;

import java.util.List;

import org.bukkit.ChatColor
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import dzjkb.EasyElevator.Elevator;

public class ElevatorFinders {

    public static Sign getSurroundingElevatorSign(List<Elevator> elvs, Player player)
    {
        Block tempBlock = null;
        World world = player.getWorld();
        Location loc = player.getLocation();

        Location l1 = null;Location l2 = null;
        l1 = loc;
        l2 = loc;

        int z = 0;

        int x1 = l1.getBlockX();
        int y1 = l1.getBlockY();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int y2 = l2.getBlockY();
        int z2 = l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int yStart = 0;int yEnd = 0;int zStart = 0;int zEnd = 0;
        if (x1 < x2)
        {
            xStart = x1;
            xEnd = x2;
        }
        if (x1 > x2)
        {
            xStart = x2;
            xEnd = x1;
        }
        if (x1 == x2)
        {
            xStart = x1;
            xEnd = x1;
        }
        if (z1 < z2)
        {
            zStart = z1;
            zEnd = z2;
        }
        if (z1 > z2)
        {
            zStart = z2;
            zEnd = z1;
        }
        if (z1 == z2)
        {
            zStart = z1;
            zEnd = z1;
        }
        if (y1 < y2)
        {
            yStart = y1;
            yEnd = y2;
        }
        if (y1 > y2)
        {
            yStart = y2;
            yEnd = y1;
        }
        if (y1 == y2)
        {
            yStart = y1;
            yEnd = y1;
        }
        xStart -= 5;yStart += 0;zStart -= 5;xEnd += 5;yEnd += 2;zEnd += 5;
        for (int i = xStart; i <= xEnd; i++)
        {
            int x = i;
            for (int j = yStart; j <= yEnd; j++)
            {
                int y = j;
                for (int k = zStart; k <= zEnd; k++)
                {
                    z = k;

                    tempBlock = world.getBlockAt(x, y, z);
                    if ((tempBlock.getType().equals(Material.WALL_SIGN)) || (tempBlock.getType().equals(Material.SIGN) ))
                    { 
                        org.bukkit.block.Sign sign = (org.bukkit.block.Sign)tempBlock.getState();
                        if (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]"))
                        {
                            boolean isPS = false;
                            for (Elevator e : elvs)
                            {
                                if (e.getPlatform().getSign().equals(sign))
                                {
                                    isPS = true;
                                }
                            }
                            if (!isPS)
                            {
                                return (org.bukkit.block.Sign)tempBlock.getState();
                            }
                        }
                    }
                    tempBlock = null;
                }
            }
        }
        return null;
    }

    public Elevator getElevator(List<Elevator> elvs, Sign sign)
    {
        if ((sign.getLine(0).equals("[EElevator]")) || (sign.getLine(0).equals(ChatColor.DARK_GRAY + "[EElevator]")))
        {
            Elevator e = null;
            for (int i = 0; i < elvs.size(); i++)
            {
                org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
                Block attached = sign.getBlock().getRelative(signData.getAttachedFace());
                if (((Elevator)elvs.get(i)).isPartOfElevator(attached.getLocation())) {
                    if ((((Elevator)elvs.get(i)).isFloorSign(sign)) || (((Elevator)elvs.get(i)).isPlatformSign(sign)))
                    {
                        e = (Elevator)elvs.get(i);
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