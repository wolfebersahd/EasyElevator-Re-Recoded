package dzjkb.EasyElevator;

import java.util.ArrayList;
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
    private int highestPoint;
    private int lowestPoint;
    private int xLow;
    private int xHigh;
    private int zLow;
    private int zHigh;
    private int maxFloors = -1;
    private int maxPerimeter = -1;

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

        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)s.getData();
        this.attached = s.getBlock().getRelative(signData.getAttachedFace());

        initializeLift();
    }

    private void initializeLift()
    {
        int count = 0;

        int low = -1;
        int high = -1;
        for (int i = this.sign.getY(); i >= 0; i--) {
            Block b = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            if (isBorder(b)) {
                low = i;
                break;
            }
        }

        for (int i = this.sign.getY(); i < this.world.getMaxHeight(); i++) {
            Block b = this.world.getBlockAt(this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            if (isBorder(b)) {
                high = i;
                break;
            }
        }

        if ((low == -1) || (high == -1)) {
            return;
        }

        this.highestPoint = high;
        this.lowestPoint = low;

        Block b1 = null;
        Block b2 = null;
        for (int i = low; i < high; i++)
        {
            Location currLoc = new Location(this.world, this.attached.getLocation().getBlockX(), i, this.attached.getLocation().getBlockZ());
            Block target = this.world.getBlockAt(currLoc);
            if (isFloor(target))
            {
                int dirChange = 0;

                String dir = "";

                List<Block> blocks = new ArrayList<Block>();
                Block start = target;
                Block t = null;

                b2 = null;
                b1 = null;
                do
                {
                    Block temp = null;
                    if (t == null)
                    {
                        if (temp == null)
                        {
                            temp = checkForIron(start, start.getRelative(0, 0, 1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "East")) {
                                    dirChange++;
                                }
                                dir = "East";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, start.getRelative(0, 0, -1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "West")) {
                                    dirChange++;
                                }
                                dir = "West";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, start.getRelative(1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "North")) {
                                    dirChange++;
                                }
                                dir = "North";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, start.getRelative(-1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "South")) {
                                    dirChange++;
                                }
                                dir = "South";
                            }
                        }
                    }
                    else if (t != null)
                    {
                        if (temp == null)
                        {
                            temp = checkForIron(start, t.getRelative(0, 0, 1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "East")) {
                                    dirChange++;
                                }
                                dir = "East";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, t.getRelative(0, 0, -1), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "West")) {
                                    dirChange++;
                                }
                                dir = "West";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, t.getRelative(1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "North")) {
                                    dirChange++;
                                }
                                dir = "North";
                            }
                        }
                        if (temp == null)
                        {
                            temp = checkForIron(start, t.getRelative(-1, 0, 0), blocks);
                            if (temp != null)
                            {
                                t = temp;
                                if (dirChanged(dir, "South")) {
                                    dirChange++;
                                }
                                dir = "South";
                            }
                        }
                    }
                    if (temp == null) {
                        return;
                    }
                    if (dirChange == 1) {
                        if (b1 == null) {
                            b1 = (Block)blocks.get(blocks.size() - 1);
                        }
                    }
                    if (dirChange == 3) {
                        if (b2 == null) {
                            b2 = (Block)blocks.get(blocks.size() - 1);
                        }
                    }
                    blocks.add(temp);
                } while (!start.equals(t));
                if (blocks.size() > this.maxPerimeter) {
                    return;
                }
                if (blocks.contains(target))
                {
                    if ((b1 == null) || (b2 == null)) {
                        return;
                    }
                    if ((dirChange != 4) && (dirChange != 3)) {
                        return;
                    }
                    Sign callSign = getCallSign(b1.getLocation(), b2.getLocation());
                    if (callSign != null)
                    {
                        Floor floor = new Floor(this, b1.getLocation(), b2.getLocation(), callSign, count + 1);
                        this.floors.add(floor);
                        count++;
                    }
                }
                else
                {
                    return;
                }
            }
        }
        if (this.floors.size() > this.maxFloors) {
            return;
        }
        this.platform = new Platform(this.plugin, b1.getLocation(), b2.getLocation(), ((Floor)this.floors.get(0)).getHeight(), ((Floor)this.floors.get(this.floors.size() - 1)).getHeight());
        if (!this.platform.isInitialized()) {
            return;
        }
        this.isInitialized = true;
        System.out.println("[EasyElevator] An elevator has been initialized");
    }

    private Sign getCallSign(Location l1, Location l2)
    {
        BlockFace[] faces = new BlockFace[4];
        faces[0] = BlockFace.NORTH;
        faces[1] = BlockFace.EAST;
        faces[2] = BlockFace.SOUTH;
        faces[3] = BlockFace.WEST;

        int x1 = l1.getBlockX();
        int z1 = l1.getBlockZ();
        int x2 = l2.getBlockX();
        int z2 = l2.getBlockZ();

        int xStart = Math.min(x1, x2) - 1;
        int xEnd = Math.max(x1, x2) + 1;
        int zStart = Math.min(z1, z2) - 1;
        int zEnd = Math.max(z1, z2) + 1;

        this.xLow = xStart;
        this.xHigh = xEnd;
        this.zLow = zStart;
        this.zHigh = zEnd;

        for (int y = l1.getBlockY() + 2; y <= l1.getBlockY() + 3; y++) {
            for (int x = xStart; x <= xEnd; x++) {
                for (int z = zStart; z <= zEnd; z++)
                {
                    Block b = this.world.getBlockAt(x, y, z);
                    if (
                        b.getType().equals(Material.WALL_SIGN) &&
                        ((x == xStart || x == xEnd) || (z == zStart || z == zEnd))
                    ) {
                        return (Sign)b.getState();
                    }
                }
            }
        }
        return null;
    }

    private boolean dirChanged(String dir, String newDir) {
        return !dir.equals("") && !dir.equals(newDir);
    }

    private Block checkForIron(Block start, Block t, List<Block> blocks)
    {
        // if (isFloor(t) || isOutputDoor(t) || isOutputFloor(t))
        // {
        //     if (start.equals(t) && blocks.size() <= 4) {
        //         return null;
        //     }
        //     if (!blocks.contains(t)) {
        //         return t;
        //     }
        // }
        // return null;

        if (
            (isFloor(t) || isOutputDoor(t) || isOutputFloor(t)) &&
            !blocks.contains(t) &&
            !(start.equals(t) && blocks.size() <= 4)
        )
            return t;

        return null;
    }

    public void addStops(int floor)
    {
        int height = -1;
        for (int i = 0; i < this.floors.size(); i++) {
            if (((Floor)this.floors.get(i)).getFloor() == floor) {
                height = ((Floor)this.floors.get(i)).getHeight();
            }
        }
        addStopsFromHeight(height);
    }

    public void addStopsFromHeight(int height)
    {
        if (height != -1) {
            if (!this.stops.contains(Integer.valueOf(height)))
            {
                this.stops.add(Integer.valueOf(height));
                if (!this.isMoving)
                {
                    this.isMoving = true;
                    run();
                }
            }
        }
    }

    public void Call(int height)
    {
        boolean hasHeight = false;
        Floor f = null;
        for (int i = 0; i < this.floors.size(); i++)
        {
            f = (Floor)this.floors.get(i);
            if (f.getSignHeight() == height)
            {
                hasHeight = true;
                f.setCalled(true);
                i = this.floors.size();
            }
        }
        if (hasHeight) {
            addStopsFromHeight(f.getHeight());
        }
    }

    public void StopAt(int floor)
    {
        for (Floor f : this.floors) {
            if (f.getFloor() == floor)
            {
                Call(f.getSignHeight());
                return;
            }
        }
    }

    int lcount = 0;

    public void run()
    {
        if (this.lcount == 6) {
            this.lcount = 0;
        }
        updateDirection();
        updateFloorIndicator();
        if (!this.hasOpenDoor)
        {
            if (!this.platform.isStuck())
            {
                if (this.stops.contains(Integer.valueOf(this.platform.getHeight())))
                {
                    for (Floor f : this.floors) {
                        if (f.getHeight() == this.platform.getHeight()) {
                            this.currentFloor = f;
                        }
                    }
                    if (this.currentFloor != null)
                    {
                        if (this.cfg.playArrivalSound) {
                            this.currentFloor.playOpenSound();
                        }
                        this.currentFloor.switchRedstoneFloorOn(true);
                        this.currentFloor.OpenDoor();
                        this.hasOpenDoor = true;
                        this.currentFloor.setCalled(false);
                        this.platform.stopTeleport();
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 100L);
                    }
                }
                else
                {
                    if (!this.direction.equals("")) {
                        if (this.currentFloor != null)
                        {
                            this.currentFloor.switchRedstoneFloorOn(false);
                            this.currentFloor = null;
                        }
                    }
                    if (this.direction.equals("DOWN"))
                    {
                        this.platform.moveDown(this.lcount);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                        this.lcount += 1;
                    }
                    else if (!this.direction.equals("UP"))
                    {
                        this.isMoving = false;
                        return;
                    }
                    if (this.direction.equals("UP"))
                    {
                        this.platform.moveUp(this.lcount);
                        this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 1L);
                        this.lcount += 1;
                    }
                    else if (!this.direction.equals("DOWN"))
                    {
                        this.isMoving = false;
                    }
                }
            }
            else
            {
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
            }
        }
        else if (this.currentFloor != null)
        {
            this.currentFloor.CloseDoor();
            this.hasOpenDoor = false;
            removeCurrentFloor();
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, this, 5L);
        }
    }

    public void changeFloor()
    {
        int curr = Integer.parseInt(this.platform.getSign().getLine(1));
        int next = curr + 1;
        if (next > this.floors.size()) {
            next = 1;
        }
        this.platform.writeSign(1, String.valueOf(next));
    }

    public int getFloorNumberFromHeight(int hight)
    {
        int floor = -1;
        for (Floor f : this.floors) {
            if (f.getHeight() == hight) {
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
                    int t = ((Integer)this.stops.get(i)).intValue();
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
                    int t = ((Integer)this.stops.get(i)).intValue();
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
            if (((Integer)this.stops.get(i)).intValue() == this.platform.getHeight()) {
                this.stops.remove(i);
            }
        }
    }

    private void updateDirection()
    {
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

    private void updateFloorIndicator()
    {
        int curr = getCurrentFloor();
        for (int i = 0; i < this.floors.size(); i++) {
            if (curr != -1)
            {
                ((Floor)this.floors.get(i)).writeSign(2, ""+curr);
            }
            else
            {
                if (this.direction.equals("UP")) {
                    ((Floor)this.floors.get(i)).writeSign(2, "/\\");
                }
                if (this.direction.equals("DOWN")) {
                    ((Floor)this.floors.get(i)).writeSign(2, "\\/");
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
                if (this.platform.getHeight() == ((Floor)this.floors.get(i)).getHeight()) {
                    return ((Floor)this.floors.get(i)).getFloor();
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
            (y > this.lowestPoint) &&
            (y < this.highestPoint) &&
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
            if (floorHeight == ((Floor)this.floors.get(i)).getHeight()) {
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
