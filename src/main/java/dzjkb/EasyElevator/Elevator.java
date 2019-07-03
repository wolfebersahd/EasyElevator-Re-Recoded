package dzjkb.EasyElevator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import dzjkb.EasyElevator.EEConfiguration;

public class Elevator
        implements Runnable
{
    // public EasyElevator plugin;
    private EEConfiguration cfg;
    private EasyElevator plugin;
    private World world;

    private Sign sign;
    private Block attached;

    private int maxFloors;
    private int maxPerimeter;
    private boolean debug;

    private int xLow;
    private int xHigh;
    private int yLow;
    private int yHigh;
    private int zLow;
    private int zHigh;

    private List<Floor> floors = new ArrayList<Floor>();
    public Floor currentFloor = null;
    private Platform platform;

    private List<Integer> stops = new ArrayList<Integer>();
    private String direction = "";
    private boolean isMoving = false;
    private boolean hasOpenDoor = false;
    private boolean isInitialized = false;

    public Elevator(EasyElevator p, EEConfiguration cfg, Sign s)
    {
        this.cfg = cfg;
        this.plugin = p;
        this.world = s.getWorld();
        this.sign = s;
        this.maxFloors = cfg.maxFloors;
        this.maxPerimeter = cfg.maxPerimeter;
        this.debug = cfg.debug;

        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)s.getData();
        this.attached = s.getBlock().getRelative(signData.getAttachedFace());

        initializeLift();
    }

    private void initFailure(String msg) {
        throw new RuntimeException("Failed to initialize an elevator: " + msg);
    }

    private void detectDimensions() {
        int low = getLowPoint();
        int high = getHighPoint();

        int[] lowDims = getEndpoints(low);
        int[] highDims = getEndpoints(high);

        for (int xd : lowDims) this.plugin.dbg(String.valueOf(xd));
        for (int xd : highDims) this.plugin.dbg(String.valueOf(xd));

        if (!Arrays.equals(lowDims, highDims)) {
            initFailure("unaligned upper and lower border");
        }

        this.yHigh = high;
        this.yLow = low;

        this.xLow = lowDims[0];
        this.xHigh = lowDims[1];
        this.zLow = lowDims[2];
        this.zHigh = lowDims[3];

        if (this.xHigh - this.xLow > this.maxPerimeter) {
            initFailure("too long on the x dimension");
        }
        if (this.zHigh - this.zLow > this.maxPerimeter) {
            initFailure("too long on the z dimension");
        }

        this.plugin.dbg("xLow: " + String.valueOf(this.xLow));
        this.plugin.dbg("xHigh: " + String.valueOf(this.xHigh));
        this.plugin.dbg("yLow: " + String.valueOf(this.yLow));
        this.plugin.dbg("yHigh: " + String.valueOf(this.yHigh));
        this.plugin.dbg("zLow: " + String.valueOf(this.zLow));
        this.plugin.dbg("zHigh: " + String.valueOf(this.zHigh));
    }

    private int getLowPoint() {
        Location l = this.attached.getLocation();
        int low = -1;

        for (int i = this.sign.getY(); i >= 0; --i) {
            l.setY(i);
            Block b = this.world.getBlockAt(l);
            if (isBorder(b)) {
                low = i;
                break;
            }
        }

        if (low == -1)
            initFailure("no lower border");
        return low;
    }

    private int getHighPoint() {
        Location l = this.attached.getLocation();
        int high = -1;

        for (int i = this.sign.getY(); i < this.world.getMaxHeight(); ++i) {
            l.setY(i);
            Block b = this.world.getBlockAt(l);
            if (isBorder(b)) {
                high = i;
                break;
            }
        }

        if (high == -1)
            initFailure("no upper border");
        return high;
    }

    private int[] getEndpoints(int y) {
        int x = this.attached.getLocation().getBlockX();
        int z = this.attached.getLocation().getBlockZ();

        int xEnd = x;
        for (Block b = this.world.getBlockAt(xEnd, y, z); isBorder(b); b = this.world.getBlockAt(++xEnd, y, z));
        int xStart = x;
        for (Block b = this.world.getBlockAt(xStart, y, z); isBorder(b); b = this.world.getBlockAt(--xStart, y, z));
        int zEnd = z;
        for (Block b = this.world.getBlockAt(x, y, zEnd); isBorder(b); b = this.world.getBlockAt(x, y, ++zEnd));
        int zStart = z;
        for (Block b = this.world.getBlockAt(x, y, zStart); isBorder(b); b = this.world.getBlockAt(x, y, --zStart));

        int[] ret = {xStart + 1, xEnd - 1, zStart + 1, zEnd - 1};
        return ret;
    }

    private void detectFloors() {
        int floorCount = 0;
        for (int i = this.yLow + 1; i < this.yHigh; ++i) {
            Block b = this.world.getBlockAt(this.xLow, i, this.zLow);
            if (isFloor(b)) {
                addFloor(i, ++floorCount);
                this.plugin.dbg("added floor " + String.valueOf(floorCount));
            }
        }

        if (floorCount > this.maxFloors) {
            initFailure("too many floors");
        }
    }

    private void addFloor(int y, int floorCount) {
        // check for proper floor border blocks
        this.plugin.dbg("Adding floor no. " + String.valueOf(floorCount) + " at y = " + String.valueOf(y));
        for (int x = this.xLow; x <= this.xHigh; ++x) {
            for (int z = this.zLow; z <= this.zHigh; ++z) {
                if ((x == this.xLow || x == this.xHigh || z == this.zLow || z == this.zHigh) &&
                    !isFloor(this.world.getBlockAt(x, y, z))
                ) {
                    initFailure("incomplete floor border");
                }
            }
        }

        Location lowCorner = new Location(this.world, this.xLow, y, this.zLow);
        Location highCorner = new Location(this.world, this.xHigh, y, this.zHigh);
        Sign callSign = getCallSign(lowCorner, highCorner);
        Floor f = new Floor(this, lowCorner, highCorner, callSign, floorCount);
        this.floors.add(f);
    }

    private void initPlatform() {
        Location lowCorner = new Location(this.world, this.xLow, this.yLow, this.zLow);
        Location highCorner = new Location(this.world, this.xHigh, this.yLow, this.zHigh);
        this.platform = new Platform(this.plugin, lowCorner, highCorner, this.floors.get(0).getHeight(), this.floors.get(this.floors.size() - 1).getHeight());
        if (!this.platform.isInitialized()) {
            initFailure("failed to initialize platform");
        }
    }

    private void initializeLift() {
        this.plugin.dbg("Initializing new elevator");

        int floorCount = 0;
        detectDimensions();
        detectFloors();
        initPlatform();

        this.isInitialized = true;
        this.plugin.getLogger().info("An elevator has been initialized");
    }

    private Sign getCallSign(Location lowCorner, Location highCorner) {
        this.plugin.dbg("entering getCallSign()");
        int xStart = lowCorner.getBlockX();
        int xEnd = highCorner.getBlockX();
        int zStart = lowCorner.getBlockZ();
        int zEnd = highCorner.getBlockZ();

        for (int y = lowCorner.getBlockY() + 2; y <= lowCorner.getBlockY() + 3; ++y) {
            for (int x = xStart - 1; x <= xEnd + 1; ++x) {
                for (int z = zStart - 1; z <= zEnd + 1; ++z) {
                    Block b = this.world.getBlockAt(x, y, z);
                    // this.dbg("Checking block at " + String.valueOf(x) +
                    //          " " + String.valueOf(y) +
                    //          " " + String.valueOf(z) +
                    //          " of type " + b.getType().toString());
                    if (
                        b.getType().equals(Material.WALL_SIGN) &&
                        (x == xStart - 1 || x == xEnd + 1 || z == zStart - 1 || z == zEnd + 1)
                    ) {
                        return (Sign)b.getState();
                    }
                }
            }
        }
        return null;
    }

    // private boolean dirChanged(String dir, String newDir) {
    //     return !dir.equals("") && !dir.equals(newDir);
    // }

    // private Block checkForIron(Block start, Block t, List<Block> blocks) {
    //     // if (isFloor(t) || isOutputDoor(t) || isOutputFloor(t))
    //     // {
    //     //     if (start.equals(t) && blocks.size() <= 4) {
    //     //         return null;
    //     //     }
    //     //     if (!blocks.contains(t)) {
    //     //         return t;
    //     //     }
    //     // }
    //     // return null;

    //     if (
    //         (isFloor(t) || isOutputDoor(t) || isOutputFloor(t)) &&
    //         !blocks.contains(t) &&
    //         !(start.equals(t) && blocks.size() <= 4)
    //     )
    //         return t;

    //     return null; 
    // }

    public void addStops(int floor) {
        int height = -1;
        for (int i = 0; i < this.floors.size(); i++) {
            // TODO just do floors.get(floor)?
            if (this.floors.get(i).getFloor() == floor) {
                height = this.floors.get(i).getHeight();
            }
        }
        addStopsFromHeight(height);
    }

    public void addStopsFromHeight(int height) {
        if (height != -1) {
            if (!this.stops.contains(height)) {
                this.stops.add(height);
                if (!this.isMoving) {
                    this.isMoving = true;
                    run();
                }
            }
        }
    }

    // height here refers to the absolute height of the floor sign ???
    public void call(int height) {
        boolean hasHeight = false;
        Floor f = null;
        for (int i = 0; i < this.floors.size(); i++) {
            f = this.floors.get(i);
            if (f.getSignHeight() == height) {
                hasHeight = true;
                f.setCalled(true);
                break;
            }
        }
        if (hasHeight) {
            addStopsFromHeight(f.getHeight());
        }
    }

    public void stopAt(int floor) {
        for (Floor f : this.floors) {
            if (f.getFloor() == floor)
            {
                call(f.getSignHeight());
                return;
            }
        }
    }

    int lcount = 0;

    public void run() {
        this.plugin.dbg("Running elevator");

        if (this.lcount == 6) {
            this.lcount = 0;
        }

        updateDirection();
        updateFloorIndicators();

        if (this.hasOpenDoor && this.currentFloor != null) {
            this.plugin.dbg("Door is open, closing");
            this.currentFloor.closeDoor();
            this.hasOpenDoor = false;
            removeCurrentFloor();
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 5L);
        } else if (!this.hasOpenDoor) {
            this.plugin.dbg("Door is closed, trying to move");

            if (this.platform.isStuck()) {
                this.plugin.dbg("Platform stuck");
                if (this.direction.equals("UP")) {
                    this.direction = "DOWN";
                } else {
                    this.direction = "UP";
                }
                this.stops.clear();
                addStops(getFloorNumberFromHeight(getNextFloorHeight_2()));
                this.platform.setStuck(false);
                this.platform.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator is stuck. Resetting...");
                this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 50L);
            } else {
                doElevatorAction();
            }
        }
    }

    private void doElevatorAction() {
        this.plugin.dbg("Trying to move");
        if (this.stops.contains(this.platform.getHeight())) {
            for (Floor f : this.floors) {
                if (f.getHeight() == this.platform.getHeight()) {
                    this.currentFloor = f;
                    this.plugin.dbg("Stopping at floor " + String.valueOf(this.currentFloor.getFloor()));
                    doFloor();
                }
            }
        } else {
            switch (this.direction) {
                case "UP":
                    this.plugin.dbg("Moving up");
                    this.platform.moveUp(this.lcount);
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                    this.lcount += 1;
                break;

                case "DOWN":
                    this.plugin.dbg("Moving down");
                    this.platform.moveDown(this.lcount);
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                    this.lcount += 1;
                break;

                default:
                    this.plugin.dbg("Not moving");
                    this.isMoving = false;
            }
        }
    }

    private void doFloor() {
        if (this.currentFloor == null)
            return;

        if (this.cfg.playArrivalSound) {
            this.currentFloor.playOpenSound();
        }
        // this.currentFloor.switchRedstoneFloorOn(true);
        this.currentFloor.openDoor();
        this.hasOpenDoor = true;
        this.currentFloor.setCalled(false);
        this.platform.stopTeleport();
        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 100L);
    }

    // public void run() {
    //     if (this.lcount == 6) {
    //         this.lcount = 0;
    //     }
    //     updateDirection();
    //     updateFloorIndicators();
    //     if (!this.hasOpenDoor)
    //     {
    //         if (!this.platform.isStuck())
    //         {
    //             if (this.stops.contains(this.platform.getHeight()))
    //             {
    //                 for (Floor f : this.floors) {
    //                     if (f.getHeight() == this.platform.getHeight()) {
    //                         this.currentFloor = f;
    //                     }
    //                 }
    //                 if (this.currentFloor != null)
    //                 {
    //                     if (this.cfg.playArrivalSound) {
    //                         this.currentFloor.playOpenSound();
    //                     }
    //                     // this.currentFloor.switchRedstoneFloorOn(true);
    //                     this.currentFloor.openDoor();
    //                     this.hasOpenDoor = true;
    //                     this.currentFloor.setCalled(false);
    //                     this.platform.stopTeleport();
    //                     this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 100L);
    //                 }
    //             }
    //             else
    //             {
    //                 if (!this.direction.equals("")) {
    //                     if (this.currentFloor != null)
    //                     {
    //                         // this.currentFloor.switchRedstoneFloorOn(false);
    //                         this.currentFloor = null;
    //                     }
    //                 }
    //                 if (this.direction.equals("DOWN"))
    //                 {
    //                     this.platform.moveDown(this.lcount);
    //                     this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
    //                     this.lcount += 1;
    //                 }
    //                 else if (!this.direction.equals("UP"))
    //                 {
    //                     this.isMoving = false;
    //                     return;
    //                 }
    //                 if (this.direction.equals("UP"))
    //                 {
    //                     this.platform.moveUp(this.lcount);
    //                     this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
    //                     this.lcount += 1;
    //                 }
    //                 else if (!this.direction.equals("DOWN"))
    //                 {
    //                     this.isMoving = false;
    //                 }
    //             }
    //         }
    //         else
    //         {
    //             if (this.direction.equals("UP")) {
    //                 this.direction = "DOWN";
    //             } else {
    //                 this.direction = "UP";
    //             }
    //             this.stops.clear();
    //             addStops(getFloorNumberFromHeight(getNextFloorHeight_2()));
    //             this.platform.setStuck(false);
    //             this.platform.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + "The Elevator is stuck. Resetting...");
    //             this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 50L);
    //         }
    //     }
    //     else if (this.currentFloor != null)
    //     {
    //         this.currentFloor.closeDoor();
    //         this.hasOpenDoor = false;
    //         removeCurrentFloor();
    //         this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 5L);
    //     }
    // }

    public void changeFloor()
    {
        int curr = Integer.parseInt(this.platform.getSign().getLine(1));
        int next = curr + 1;
        if (next > this.floors.size()) {
            next = 1;
        }
        this.platform.writeSign(1, String.valueOf(next));
    }

    public int getFloorNumberFromHeight(int height)
    {
        int floor = -1;
        for (Floor f : this.floors) {
            if (f.getHeight() == height) {
                return f.getFloor();
            }
        }
        return floor;
    }

    public int getNextFloorHeight_2()
    {
        int next = -1;
        int current = this.platform.getHeight();
        if (this.direction.equals("UP"))
        {
            for (int i = 0; i < this.floors.size(); i++)
            {
                int t = ((Floor)this.floors.get(i)).getHeight();
                if ((next == -1) &&
                        (t > current)) {
                    next = t;
                }
                if ((t > current) && (t < next)) {
                    next = t;
                }
            }
            return next;
        }
        if (this.direction.equals("DOWN"))
        {
            for (int i = 0; i < this.floors.size(); i++)
            {
                int t = ((Floor)this.floors.get(i)).getHeight();
                if ((next == -1) &&
                        (t < current)) {
                    next = t;
                }
                if ((t < current) && (t > next)) {
                    next = t;
                }
            }
            return next;
        }
        if (this.direction.equals("")) {
            return this.platform.getHeight();
        }
        return -1;
    }

    public int getNextFloorHeight()
    {
        if (this.currentFloor != null)
        {
            int next = -1;
            int current = this.currentFloor.getHeight();
            if (this.direction.equals("UP")) {
                for (int i = 0; i < this.stops.size(); i++)
                {
                    int t = this.stops.get(i);
                    if ((next == -1) &&
                            (t > current)) {
                        next = t;
                    }
                    if ((t > current) && (t < next)) {
                        next = t;
                    }
                }
            }
            if (this.direction.equals("DOWN")) {
                for (int i = 0; i < this.stops.size(); i++)
                {
                    int t = this.stops.get(i);
                    if ((next == -1) &&
                            (t < current)) {
                        next = t;
                    }
                    if ((t < current) && (t > next)) {
                        next = t;
                    }
                }
            }
            return next;
        }
        return -1;
    }

    public Platform getPlatform()
    {
        return this.platform;
    }

    public Floor getMainFloor()
    {
        return (Floor)this.floors.get(0);
    }

    public boolean isInitialized()
    {
        return this.isInitialized;
    }

    private void removeCurrentFloor()
    {
        for (int i = 0; i < this.stops.size(); i++) {
            if (this.stops.get(i) == this.platform.getHeight()) {
                this.stops.remove(i);
            }
        }
    }

    private void updateDirection()
    {
        // this function updates the direction as follows:
        // - If the current direction is up and there is a stop above our current height, return
        // - If the current direction is down and there is a stop below, return
        // - If the current direction is none, set direction to first stop in this.stops
        // - Else if any stops left, flip direction
        // - Else set direction to none

        int height = this.platform.getHeight();
        for (Iterator<Integer> localIterator = this.stops.iterator(); localIterator.hasNext();)
        {
            int i = localIterator.next().intValue();
            if (this.direction.equals("DOWN")) {
                if (i < height) {
                    return;
                }
            }
            if (this.direction.equals("UP")) {
                if (i > height) {
                    return;
                }
            }
            if (this.direction.equals(""))
            {
                if (i > height) {
                    this.direction = "UP";
                } else {
                    this.direction = "DOWN";
                }
                return;
            }
        }
        if (this.stops.size() > 0)
        {
            if (this.direction.equals("DOWN"))
            {
                this.direction = "UP";
                return;
            }
            if (this.direction.equals("UP")) {
                this.direction = "DOWN";
            }
        }
        else
        {
            this.direction = "";
        }
    }

    private void updateFloorIndicators()
    {
        int curr = getCurrentFloor();
        for (int i = 0; i < this.floors.size(); i++) {
            if (curr != -1)
            {
                this.floors.get(i).writeSign(2, ""+curr);
            }
            else
            {
                if (this.direction.equals("UP")) {
                    this.floors.get(i).writeSign(2, "/\\");
                }
                if (this.direction.equals("DOWN")) {
                    this.floors.get(i).writeSign(2, "\\/");
                }
            }
        }
        if (curr != -1)
        {
            this.platform.writeSign(2, ""+ curr);
        }
        else
        {
            if (this.direction.equals("UP")) {
                this.platform.writeSign(2, "/\\");
            }
            if (this.direction.equals("DOWN")) {
                this.platform.writeSign(2, "\\/");
            }
        }
        int next = getFloorNumberFromHeight(getNextFloorHeight());
        if (next != -1) {
            this.platform.writeSign(3,""+ next);
        } else {
            this.platform.writeSign(3, "-");
        }
    }

    public int getCurrentFloor()
    {
        if (isFloor(this.platform.getHeight())) {
            for (int i = 0; i < this.floors.size(); i++) {
                if (this.platform.getHeight() == this.floors.get(i).getHeight()) {
                    return this.floors.get(i).getFloor();
                }
            }
        }
        return -1;
    }

    public boolean isPartOfElevator(Location loc)
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        return
            (y > this.yLow) &&
            (y < this.yHigh) &&
            (x >= this.xLow) &&
            (x <= this.xHigh) &&
            (z >= this.zLow) &&
            (z <= this.zHigh);
    }

    public boolean isFloorSign(org.bukkit.block.Sign sign)
    {
        for (int i = 0; i < this.floors.size(); i++)
        {
            Floor f = (Floor)this.floors.get(i);
            if (f.getSign().equals(sign)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlatformSign(org.bukkit.block.Sign sign)
    {
        if (this.platform.getSign().equals(sign)) {
            return true;
        }
        return false;
    }

    public boolean isInElevator(Player player)
    {
        return this.platform.hasPlayer(player);
    }

    public boolean isFloor(int floorHeight)
    {
        for (int i = 0; i < this.floors.size(); i++) {
            if (floorHeight == this.floors.get(i).getHeight()) {
                return true;
            }
        }
        return false;
    }

    public List<Floor> getFloors()
    {
        return this.floors;
    }

    public boolean isBorder(Block b) {
        return checkMaterial(b, this.cfg.blockBorder);
    }

    public boolean isFloor(Block b) {
        return checkMaterial(b, this.cfg.blockFloor);
    }

    public boolean isOutputFloor(Block b) {
        return checkMaterial(b, this.cfg.blockOutputFloor);
    }

    public boolean isOutputDoor(Block b) {
        return checkMaterial(b, this.cfg.blockOutputDoor);
    }

    private boolean checkMaterial(Block b, String material) {
        try
        {
            Material m = Material.getMaterial(material);
            if (m != null && m == b.getType()) {
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
