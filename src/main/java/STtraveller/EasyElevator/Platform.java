package STtraveller.EasyElevator;

import java.util.ArrayList;
import java.util.List;

import java.lang.Math;

import STtraveller.EasyElevator.EasyElevator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
    private Block platformSign = null;
    private String platformMsg[] = new String[4];
    private byte platformSignData;
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
                    // this.plugin.dbg("Checking at " + String.valueOf(x) +
                                    // " " + String.valueOf(i) +
                                    // " " + String.valueOf(z) +
                                    // ", type " + tempBlock.getType().toString());
                    if (tempBlock.getType() == Material.DOUBLE_STEP) {
                        this.platform.add(tempBlock);
                        if ((signBlock.getState() instanceof Sign)) {
                            this.plugin.dbg(
                                "Found sign at " + String.valueOf(x) +
                                ", " + String.valueOf(i + 2) +
                                ", " + String.valueOf(z));

                            // this.platformSign = (Sign)signBlock.getState();
                            this.platformSign = signBlock;
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

        this.platformMsg[0] = "[EElevator]";
        this.platformMsg[1] = "1";
        this.platformMsg[2] = "";
        this.platformMsg[3] = "";

        // TODO add check to guarantee this condition, extract this into some function
        if (this.platformSign.getState() instanceof Sign) {
            Sign signState = (Sign)this.platformSign.getState();
            signState.setLine(0, ChatColor.DARK_GRAY + "[EElevator]");
            signState.setLine(1, "1");
            this.platformSignData = signState.getData().getData();
            signState.update();
        } 
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
            if (z >= this.zmin && z <= this.zmax && x >= this.xmin && x <= this.xmax) {
                return true;
            }
        }
        return false;
    }

    private void updateSign(int height)
    {
        Block newSignBlock = this.world.getBlockAt(this.platformSign.getX(), height, this.platformSign.getZ());

        newSignBlock.setType(Material.WALL_SIGN);
        Sign newSign = (Sign)newSignBlock.getState();
        newSign.getData().setData(this.platformSignData);
        newSign.setLine(0, this.platformMsg[0]);
        newSign.setLine(1, this.platformMsg[1]);
        newSign.setLine(2, this.platformMsg[2]);
        newSign.setLine(3, this.platformMsg[3]);
        if (newSignBlock.getRelative(((org.bukkit.material.Sign)newSign.getData()).getAttachedFace()).getType() == Material.AIR) {
            newSignBlock.setType(Material.AIR);
        } else {
            newSign.update();
        }

        this.platformSign.setType(Material.AIR);
        this.platformSign = newSignBlock;
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
        if (this.platformSign.getState() instanceof Sign) {
            return (Sign)this.platformSign.getState();
        }
        
        return null;
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
        this.platformMsg[line] = message;
        if (this.platformSign.getState() instanceof Sign) {
            Sign s = (Sign)this.platformSign.getState();
            s.setLine(line, message);
            s.update();
        }
    }
}
