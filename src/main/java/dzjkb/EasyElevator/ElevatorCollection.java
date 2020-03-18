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

    public void updateConfig(EEConfiguration newCfg) {
        for (Elevator e : this.elevators) {
            e.updateConfig(newCfg);
        }
    }

    public Sign getSurroundingElevatorSign(Player player)
    {
        Location loc = player.getLocation();

        // TODO what does this actually do
        Location l1 = loc;
        Location l2 = loc;

        if (!l1.equals(l2)) this.plugin.dbg("What the f");

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
                    if (b.getType().equals(Material.SPRUCE_WALL_SIGN) ||
                        b.getType().equals(Material.OAK_WALL_SIGN) ||
                        b.getType().equals(Material.LEGACY_WALL_SIGN) ||
                        b.getType().equals(Material.JUNGLE_WALL_SIGN) ||
                        b.getType().equals(Material.BIRCH_WALL_SIGN) ||
                        b.getType().equals(Material.ACACIA_WALL_SIGN) ||
                        b.getType().equals(Material.DARK_OAK_WALL_SIGN) ||
                        b.getType().equals(Material.SPRUCE_SIGN) ||
                        b.getType().equals(Material.OAK_SIGN) ||
                        b.getType().equals(Material.LEGACY_SIGN) ||
                        b.getType().equals(Material.JUNGLE_SIGN) ||
                        b.getType().equals(Material.BIRCH_SIGN) ||
                        b.getType().equals(Material.ACACIA_SIGN) ||
                        b.getType().equals(Material.DARK_OAK_SIGN))
                    { 
                        Sign sign = (Sign)b.getState();
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
            if (s.equals(e.getPlatform().getSign())) {
                return true;
            }
        }

        return false;
    }

    public Elevator getElevator(Sign sign) {
        this.plugin.dbg("Entering getElevator() with sign at: " + sign.getLocation().toString());
        if (EEUtils.isEESign(sign)) {
            Elevator e = null;
            org.bukkit.material.Sign signData = (org.bukkit.material.Sign)sign.getData();
            Block attached = sign.getBlock().getRelative(signData.getAttachedFace());

            for (int i = 0; i < this.elevators.size(); i++) {
                if (this.elevators.get(i).isPartOfElevator(attached.getLocation())) {
                    if ((this.elevators.get(i).isFloorSign(sign)) || (this.elevators.get(i).isPlatformSign(sign))) {
                        e = this.elevators.get(i);
                        i = this.elevators.size();
                    }
                }
            }
            if (e == null) {
                this.plugin.dbg("No elevator found, initializing new one");
                e = new Elevator(this.plugin, this.plugin.getEEConfig(), sign);
            }
            if (e != null) {
                if (e.isInitialized()) {
                    if (!this.elevators.contains(e)) {
                        this.elevators.add(e);
                    }
                    return e;
                } else {
                    this.plugin.dbg("Elevator not initialized, returning null");
                }
            }
        }
        return null;
    }

    public List<Elevator> getElevators() {
        return this.elevators;
    }
}