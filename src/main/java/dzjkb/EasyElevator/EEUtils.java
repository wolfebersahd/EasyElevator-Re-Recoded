package dzjkb.EasyElevator;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class EEUtils {

    private final static String EE_SIGN_STRING = "[EElevator]";

    public static boolean isEESign(Sign s) {
        return s.getLine(0).equals(EE_SIGN_STRING) || s.getLine(0).equals(ChatColor.DARK_GRAY + EE_SIGN_STRING);
    }

    public static void playerMsg(Player p, String msg) {
        p.sendMessage(ChatColor.DARK_GRAY + "[EElevator] " + ChatColor.GRAY + msg);
    }
}
