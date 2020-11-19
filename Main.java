import com.google.common.base.Charsets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import me.Commands;
import me.JumpPad;
import me.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class Main extends JavaPlugin {
  public static Main plugin;
  
  static String chat(String s) {
    if (s == null)
      return ChatColor.translateAlternateColorCodes('&', "&cConfig Missing Text"); 
    byte[] temp = s.getBytes(Charsets.UTF_8);
    return ChatColor.translateAlternateColorCodes('&', new String(temp, Charsets.UTF_8));
  }
  
  static String locationToBase64(Location loc) {
    if (loc == null)
      return null; 
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
      dataOutput.writeObject(loc);
      dataOutput.close();
      return Base64Coder.encodeLines(outputStream.toByteArray());
    } catch (Exception e) {
      return null;
    } 
  }
  
  static Location locationFromBase64(String data) {
    if (data == null)
      return null; 
    try {
      ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
      BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      Location loc = (Location)dataInput.readObject();
      dataInput.close();
      return loc;
    } catch (Exception e) {
      return null;
    } 
  }
  
  static void spawnExplosion(Player player) {
    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.EXPLOSION_HUGE, true, player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ(), 0.0F, 0.0F, 0.0F, 0.0F, 2, new int[0]);
    for (Player p : player.getLocation().getWorld().getPlayers())
      (((CraftPlayer)p).getHandle()).playerConnection.sendPacket((Packet)packet); 
  }
  
  public void onEnable() {
    this;
    plugin = this;
    if (!getDataFolder().exists())
      getDataFolder().mkdir(); 
    File folder = new File(getDataFolder(), "jumppads");
    if (!folder.exists())
      folder.mkdir(); 
    for (File file : folder.listFiles())
      new JumpPad(null, file, null); 
    new Commands();
  }
}
