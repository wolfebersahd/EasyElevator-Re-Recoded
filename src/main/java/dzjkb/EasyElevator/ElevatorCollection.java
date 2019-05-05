package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import dzjkb.EasyElevator.Elevator;
import dzjkb.EasyElevator.EasyElevator;
import dzjkb.EasyElevator.EEUtils;

public class ElevatorCollection
{

    private List<Elevator> elevators = new ArrayList<Elevator>();
    private EasyElevator plugin;

    public ElevatorCollection(EasyElevator p) {
        this.plugin = p;
        return;
    }

    public Sign getSurroundingElevatorSign(Player player)
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
                        if (EEUtils.isEESign(sign) && !isAnyPlatformSign(sign)) {
                            return sign;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isAnyPlatformSign(Sign s) {
        for (Elevator e : this.elevators) {
            if (e.getPlatform().getSign().equals(s)) {
                return true;
            }
        }

        return false;
    }

    public Elevator getElevator(Sign sign)
    {
        if (EEUtils.isEESign(sign))
        {
            Elevator e = null;
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
            Block attached = sign.getBlock().getRelative(signData.getAttachedFace());

            for (int i = 0; i < this.elevators.size(); i++)
            {
                if (this.elevators.get(i).isPartOfElevator(attached.getLocation())) {
                    if ((this.elevators.get(i).isFloorSign(sign)) || (this.elevators.get(i).isPlatformSign(sign)))
                    {
                        e = this.elevators.get(i);
                        i = this.elevators.size();
                    }
                }
            }
            if (e == null) {
                e = new Elevator(this.plugin, this.plugin.getEEConfig(), sign);
            }
            if (e != null) {
                if (e.isInitialized())
                {
                    if (!this.elevators.contains(e)) {
                        this.elevators.add(e);
                    }
                    return e;
                }
            }
        }
        return null;
    }

    public List<Elevator> getElevators() {
        return this.elevators;
    }
}