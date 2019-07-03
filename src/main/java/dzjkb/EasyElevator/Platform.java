package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.List;

import java.lang.Math;

import dzjkb.EasyElevator.EasyElevator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
// import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
// import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
// import org.bukkit.material.MaterialData;
// import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Platform
{
    private final EasyElevator plugin;
    private World world;
    private int ymin = -1;
    private int ymax = -1;
    private int xmin;
    private int zmin;
    private int xmax;
    private int zmax;
    private boolean isInitialized = false;
    private boolean isStuck = false;
    private Sign platformSign = null;
    private Location lowCorner;
    private Location highCorner;
    private List<Block> platform = new ArrayList<>();

    public Platform(EasyElevator plugin, Location l1, Location l2, int min, int max) {
        this.plugin = plugin;
        this.ymin = min;
        this.ymax = max;
        this.world = l1.getWorld();

        initializePlatform(l1, l2);
    }

    private void initializePlatform(Location l1, Location l2)
    {
        this.plugin.dbg("Initializing platform at " + l1.toString() + " and " + l2.toString());
        int x1 = l1.getBlockX();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int z2 = l2.getBlockZ();

        int xStart = Math.min(x1, x2) + 1;
        int xEnd = Math.max(x1, x2) - 1;
        int zStart = Math.min(z1, z2) + 1;
        int zEnd = Math.max(z1, z2) - 1;

        this.xmin = xStart;
        this.zmin = zStart;
        this.xmax = xEnd;
        this.zmax = zEnd;

        this.lowCorner = this.world.getBlockAt(xStart, l1.getBlockY(), zStart).getLocation();
        this.highCorner = this.world.getBlockAt(xEnd, l1.getBlockY(), zEnd).getLocation();
        for (int i = this.ymin; i <= this.ymax; i++) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int z = zStart; z <= zEnd; z++) {
                    Block tempBlock = this.world.getBlockAt(x, i, z);
                    Block signBlock = this.world.getBlockAt(x, i + 2, z);
                    this.plugin.dbg("Checking at " + String.valueOf(x) +
                                    " " + String.valueOf(i) +
                                    " " + String.valueOf(z) +
                                    ", type " + tempBlock.getType().toString());
                    if (tempBlock.getType() == Material.DOUBLE_STEP) {
                        this.platform.add(tempBlock);
                        if ((signBlock.getState() instanceof Sign)) {
                            this.plugin.dbg(
                                "Found sign at " + String.valueOf(x) +
                                ", " + String.valueOf(i + 2) +
                                ", " + String.valueOf(z));

                            this.platformSign = (Sign)signBlock.getState();
                        }
                        this.lowCorner.setY(i);
                        this.highCorner.setY(i);
                    }
                    else if (this.platform.size() != 0) {
                        this.plugin.dbg("incomplete platform found, aborting initialization");
                        return;
                    }
                }
            }
            if (this.platform.size() != 0)
                break;
        }
        if (this.platform.size() == 0) {
            this.plugin.dbg("No platform blocks found");
            return;
        }
        if (this.platformSign == null) {
            this.plugin.dbg("No sign found, platform initialization failed");
            return;
        }
        this.isInitialized = true;
        this.platformSign.setLine(0, ChatColor.DARK_GRAY + "[EElevator]");
        this.platformSign.setLine(1, "1");
        this.platformSign.update();
    }

    private void move(int lcount, boolean up)
    {
        int heightDelta = up ? 1 : -1;
        if (canMove(this.lowCorner.getBlockY() + heightDelta))
        {
            this.isStuck = false;
            if (lcount == 5)
            {
                for (int i = 0; i < this.platform.size(); i++)
                {
                    Block b = (Block)this.platform.get(i);
                    b.setType(Material.AIR);
                    b = this.world.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() + heightDelta, b.getLocation().getBlockZ());
                    b.setType(Material.DOUBLE_STEP);
                    // BlockState bs = b.getState();
                    // MaterialData bd = bs.getData();
                    // Slab s = (Slab)bd;
                    // s.setType(Slab.Type.DOUBLE);
                    // bs.setBlockData(s);
                    // bs.update();
                    this.platform.remove(i);
                    this.platform.add(i, b);
                    this.lowCorner.setY(b.getLocation().getBlockY());
                    this.highCorner.setY(b.getLocation().getBlockY());
                }
                updateSign(this.platformSign.getY() + heightDelta);
            }
            List<Player> players = this.world.getPlayers();
            for (Player player : players) {
                if (hasPlayer(player))
                {
                    double yVelocity = 0.17;
                    if (!up) {
                        yVelocity *= -1;
                    }

                    player.setVelocity(new Vector(0.0, yVelocity, 0.0));
                    player.setFallDistance(0.0F);

                    if (up) {
                        moveUpCorrection(player);
                    }
                }
            }
        }
        else
        {
            this.isStuck = true;
        }
    }

    public void moveDown(int lcount) {
        move(lcount, false);
    }

    public void moveUp(int lcount) {
        move(lcount, true);
    }

    // public void moveDown(int lcount)
    // {
    //     if (canMove(this.lowCorner.getBlockY() - 1))
    //     {
    //         this.isStuck = false;
    //         if (lcount == 5)
    //         {
    //             for (int i = 0; i < this.platform.size(); i++)
    //             {
    //                 Block b = (Block)this.platform.get(i);
    //                 b.setType(Material.AIR);
    //                 b = this.world.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() - 1, b.getLocation().getBlockZ());
    //                 b.setType(Material.STONE_SLAB);
    //                 BlockState bs = b.getState();
    //                 BlockData bd = bs.getBlockData();
    //                 Slab s = (Slab) bd;
    //                 s.setType(Slab.Type.DOUBLE);
    //                 bs.setBlockData(s);
    //                 bs.update();
    //                 this.platform.remove(i);
    //                 this.platform.add(i, b);
    //                 this.lowCorner.setY(b.getLocation().getBlockY());
    //                 this.highCorner.setY(b.getLocation().getBlockY());
    //             }
    //             updateSign(this.platformSign.getY() - 1);
    //         }
    //         List<Player> players = this.world.getPlayers();
    //         for (Player player : players) {
    //             if (hasPlayer(player))
    //             {
    //                 player.setVelocity(new Vector(0.0D, -0.17D, 0.0D));
    //                 player.setFallDistance(0.0F);
    //             }
    //         }
    //     }
    //     else
    //     {
    //         this.isStuck = true;
    //     }
    // }

    // public void moveUp(int lcount)
    // {
    //     if (canMove(this.lowCorner.getBlockY() + 3))
    //     {
    //         if (lcount == 5)
    //         {
    //             for (int i = 0; i < this.platform.size(); i++)
    //             {
    //                 Block b = (Block)this.platform.get(i);
    //                 b.setType(Material.AIR);
    //                 b = this.world.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() + 1, b.getLocation().getBlockZ());
    //                 b.setType(Material.STONE_SLAB);
    //                 BlockState bs = b.getState();
    //                 BlockData bd = bs.getBlockData();
    //                 Slab s = (Slab) bd;
    //                 s.setType(Slab.Type.DOUBLE);
    //                 bs.setBlockData(s);
    //                 bs.update();
    //                 this.platform.remove(i);
    //                 this.platform.add(i, b);
    //                 this.lowCorner.setY(b.getLocation().getBlockY());
    //                 this.highCorner.setY(b.getLocation().getBlockY());
    //             }
    //             updateSign(this.platformSign.getY() + 1);
    //         }
    //         List<Player> players = this.world.getPlayers();
    //         for (Player player : players) {
    //             if (hasPlayer(player))
    //             {
    //                 player.setVelocity(new Vector(0.0D, 0.17D, 0.0D));
    //                 player.setFallDistance(0.0F);
    //                 moveUpCorrection(player);
    //             }
    //         }
    //         this.isStuck = false;
    //     }
    //     else
    //     {
    //         this.isStuck = true;
    //     }
    // }

    private void moveUpCorrection(Player player)
    {
        Location pLoc = player.getLocation();
        if (pLoc.getBlockY() <= this.lowCorner.getBlockY() - 0.5D)
        {
            pLoc.setY(pLoc.getBlockY() + 2);
            player.teleport(pLoc);
        }
    }

    public boolean hasPlayer(Player player)
    {
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        if ((y >= this.ymin + 5) || (y <= this.ymax + 2)) {
            if ((z >= this.zmin) && (z <= this.zmax) && (x >= this.xmin) && (x <= this.ymax)) {
                return true;
            }
        }
        return false;
    }

    private void updateSign(int height)
    {
        Block signBlock = this.world.getBlockAt(this.platformSign.getX(), height, this.platformSign.getZ());

        signBlock.setType(Material.WALL_SIGN);
        Sign nSign = (Sign)signBlock.getState();
        // This fix doesn't work lmao, y u do this
        // if (signBlock.getRelative(((org.bukkit.material.Sign)nSign.getData()).getAttachedFace()).getType() == Material.AIR) {
        nSign.getData().setData(this.platformSign.getData().getData());
        nSign.setLine(0, this.platformSign.getLine(0));
        nSign.setLine(1, this.platformSign.getLine(1));
        nSign.setLine(2, this.platformSign.getLine(2));
        nSign.setLine(3, this.platformSign.getLine(3));
        nSign.update();
        // }

        this.platformSign.getBlock().setType(Material.AIR);
        this.platformSign = nSign;
    }

    public boolean canMove(int height)
    {
        int x1 = this.lowCorner.getBlockX();
        int z1 = this.lowCorner.getBlockZ();

        int x2 = this.highCorner.getBlockX();
        int z2 = this.highCorner.getBlockZ();

        int xStart = Math.min(x1, x2);
        int xEnd = Math.max(x1, x2);
        int zStart = Math.min(z1, z2);
        int zEnd = Math.max(z1, z2);
        for (int x = xStart; x <= xEnd; x++) {
            for (int z = zStart; z <= zEnd; z++)
            {
                Block tempBlock = this.world.getBlockAt(x, height, z);
                if (!tempBlock.getType().equals(Material.AIR)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void sendMessage(String message)
    {
        List<Player> players = this.world.getPlayers();
        for (Player player : players) {
            if (hasPlayer(player)) {
                player.sendMessage(message);
            }
        }
    }

    public void stopTeleport()
    {
        List<Player> players = this.world.getPlayers();
        for (Player player : players) {
            if (hasPlayer(player))
            {
                Location loc = player.getLocation();
                loc.setY(getHeight() + 1);
                player.teleport(loc);
            }
        }
    }

    public Sign getSign()
    {
        return this.platformSign;
    }

    public boolean isInitialized()
    {
        return this.isInitialized;
    }

    public boolean isStuck()
    {
        return this.isStuck;
    }

    public void setStuck(boolean b)
    {
        this.isStuck = b;
    }

    public int getHeight()
    {
        return this.lowCorner.getBlockY();
    }

    public void writeSign(int line, String message)
    {
        this.platformSign.setLine(line, message);
        this.platformSign.update();
    }
}
