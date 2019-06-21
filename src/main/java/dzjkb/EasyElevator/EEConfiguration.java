package dzjkb.EasyElevator;

/* Although unenforcable without loads of unnecessary boilerplate,
 * this class is to be treated as an immutable named tuple.
 */
public class EEConfiguration {

    public int maxPerimeter = 24;
    public int maxFloors = 10;
    public boolean playArrivalSound = true;
    public boolean sendArrivalMessage = true;
    public String blockBorder = "GOLD_BLOCK";
    public String blockFloor = "IRON_BLOCK";
    public String blockOutputDoor = "RED_WOOL";
    public String blockOutputFloor = "ORANGE_WOOL";
    public boolean debug = true;

    public EEConfiguration() {}
}