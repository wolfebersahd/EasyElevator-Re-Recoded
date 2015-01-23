package me;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

public class Platform
{
    private final EasyElevator plugin;
    private final Server server;
    private World world;
    private int level = -1;
    private int min = -1;
    private int max = -1;
    private int xmin;
    private int zmin;
    private int xmax;
    private int zmax;
    private boolean isInitialized = false;
    private boolean isStuck = false;
    private Sign platformSign = null;
    private Location l1;
    private Location l2;
    private List<Block> platform = new ArrayList();

    public Platform(EasyElevator plugin, Location l1, Location l2, int min, int max)
    {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.min = min;
        this.max = max;
        this.world = l1.getWorld();



        initializePlatform(l1, l2);
    }

    private void initializePlatform(Location l1, Location l2)
    {
        int x1 = l1.getBlockX();
        int z1 = l1.getBlockZ();

        int x2 = l2.getBlockX();
        int z2 = l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int zStart = 0;int zEnd = 0;
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
        xStart++;
        xEnd--;

        zStart++;
        zEnd--;

        this.xmin = xStart;
        this.zmin = zStart;
        this.xmax = xEnd;
        this.zmax = zEnd;

        this.l1 = this.world.getBlockAt(xStart, l1.getBlockY(), zStart).getLocation();
        this.l2 = this.world.getBlockAt(xEnd, l1.getBlockY(), zEnd).getLocation();
        for (int i = this.min; i <= this.max; i++)
        {
            for (int x = xStart; x <= xEnd; x++) {
                for (int z = zStart; z <= zEnd; z++)
                {
                    Block tempBlock = this.world.getBlockAt(x, i, z);
                    Block signBlock = this.world.getBlockAt(x, i + 2, z);
                    if (tempBlock.getTypeId() == 43)
                    {
                        this.platform.add(tempBlock);
                        if ((signBlock.getState() instanceof Sign)) {
                            this.platformSign = ((Sign)signBlock.getState());
                        }
                        this.l1.setY(i);
                        this.l2.setY(i);
                    }
                    else if (this.platform.size() != 0)
                    {
                        return;
                    }
                }
            }
            if (this.platform.size() != 0) {
                i = this.max + 1;
            }
        }
        if (this.platform.size() == 0) {
            return;
        }
        if (this.platformSign == null) {
            return;
        }
        this.isInitialized = true;
        this.platformSign.setLine(0, ChatColor.DARK_GRAY + "[EElevator]");
        this.platformSign.setLine(1, "1");
        this.platformSign.update();
    }

    public void moveDown(int lcount)
    {
        if (canMove(this.l1.getBlockY() - 1))
        {
            this.isStuck = false;
            if (lcount == 5)
            {
                for (int i = 0; i < this.platform.size(); i++)
                {
                    Block b = (Block)this.platform.get(i);
                    b.setTypeId(0);
                    b = this.world.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() - 1, b.getLocation().getBlockZ());
                    b.setTypeId(43);
                    b.setData((byte)0);
                    this.platform.remove(i);
                    this.platform.add(i, b);
                    this.l1.setY(b.getLocation().getBlockY());
                    this.l2.setY(b.getLocation().getBlockY());
                }
                updateSign(this.platformSign.getY() - 1);
            }
            List<Player> players = this.world.getPlayers();
            for (Player player : players) {
                if (hasPlayer(player))
                {
                    player.setVelocity(new Vector(0.0D, -0.17D, 0.0D));
                    player.setFallDistance(0.0F);
                }
            }
        }
        else
        {
            this.isStuck = true;
        }
    }

    public void moveUp(int lcount)
    {
        if (canMove(this.l1.getBlockY() + 3))
        {
            if (lcount == 5)
            {
                for (int i = 0; i < this.platform.size(); i++)
                {
                    Block b = (Block)this.platform.get(i);
                    b.setTypeId(0);
                    b = this.world.getBlockAt(b.getLocation().getBlockX(), b.getLocation().getBlockY() + 1, b.getLocation().getBlockZ());
                    b.setTypeId(43);
                    b.setData((byte)0);
                    this.platform.remove(i);
                    this.platform.add(i, b);
                    this.l1.setY(b.getLocation().getBlockY());
                    this.l2.setY(b.getLocation().getBlockY());
                }
                updateSign(this.platformSign.getY() + 1);
            }
            List<Player> players = this.world.getPlayers();
            for (Player player : players) {
                if (hasPlayer(player))
                {
                    player.setVelocity(new Vector(0.0D, 0.17D, 0.0D));
                    player.setFallDistance(0.0F);
                    moveUpCorrection(player);
                }
            }
            this.isStuck = false;
        }
        else
        {
            this.isStuck = true;
        }
    }

    private void moveUpCorrection(Player player)
    {
        Location pLoc = player.getLocation();
        if (pLoc.getBlockY() <= this.l1.getBlockY() - 0.5D)
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
        if ((y >= this.min + 5) || (y <= this.max + 2)) {
            if ((z >= this.zmin) && (z <= this.zmax) && (x >= this.xmin) && (x <= this.xmax)) {
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
        nSign.getData().setData(this.platformSign.getData().getData());
        nSign.setLine(0, this.platformSign.getLine(0));
        nSign.setLine(1, this.platformSign.getLine(1));
        nSign.setLine(2, this.platformSign.getLine(2));
        nSign.setLine(3, this.platformSign.getLine(3));
        nSign.update();
        this.platformSign.getBlock().setTypeId(0);
        this.platformSign = nSign;
    }

    public boolean canMove(int height)
    {
        int x1 = this.l1.getBlockX();
        int z1 = this.l1.getBlockZ();

        int x2 = this.l2.getBlockX();
        int z2 = this.l2.getBlockZ();

        int xStart = 0;int xEnd = 0;int zStart = 0;int zEnd = 0;
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

    public void isStuck(boolean b)
    {
        this.isStuck = b;
    }

    public int getHeight()
    {
        return this.l1.getBlockY();
    }

    public void writeSign(int line, String message)
    {
        this.platformSign.setLine(line, message);
        this.platformSign.update();
    }
}
