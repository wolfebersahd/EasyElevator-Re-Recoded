package WolfeBersahd.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;

public class EEUtils {

    private final static String EE_SIGN_STRING = "[EElevator]";

    public static boolean isEESign(Sign s) {
        return s.getLine(0).equals(EE_SIGN_STRING) || s.getLine(0).equals(ChatColor.DARK_GRAY + EE_SIGN_STRING);
    }

    public static void playerMsg(Player p, String msg) {
        p.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + msg);
    }

    public static boolean isSign(Block b) {
        return b.getBlockData() instanceof org.bukkit.block.data.type.Sign;
    }

    public static boolean isWallSign(Block b) {
        return b.getBlockData() instanceof WallSign;
    }
}
