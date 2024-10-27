package WolfeBersahd.EasyElevator;

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
    public String blockOutputFloor = "ORANGE_WOOL";
    public boolean debug = false;

    public EEConfiguration() {}

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("maxPerimeter = " + String.valueOf(this.maxPerimeter) + "\n");
        sb.append("maxFloors = " + String.valueOf(this.maxFloors) + "\n");
        sb.append("playArrivalSound = " + String.valueOf(this.playArrivalSound) + "\n");
        sb.append("sendArrivalMessage = " + String.valueOf(this.sendArrivalMessage) + "\n");
        sb.append("blockBorder = " + String.valueOf(this.blockBorder) + "\n");
        sb.append("blockFloor = " + String.valueOf(this.blockFloor) + "\n");
        sb.append("blockOutputFloor = " + String.valueOf(this.blockOutputFloor) + "\n");
        sb.append("debug = " + String.valueOf(this.debug) + "\n");

        return sb.toString();
    }
}