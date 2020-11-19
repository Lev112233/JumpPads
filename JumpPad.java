import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import me.JumpPad;
import me.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class JumpPad implements Listener {
  private static ArrayList<JumpPad> jumpPads = new ArrayList<>();
  
  private HashMap<Player, ArmorStand> isJumping = new HashMap<>();
  
  private String jumpPadName;
  
  private String neededPermission;
  
  private String permissionMessage;
  
  private Location flyToLocation;
  
  private int pos1X;
  
  private int pos1Z;
  
  private int pos2X;
  
  private int pos2Z;
  
  private int relativePos1;
  
  private int relativePos2;
  
  private FileConfiguration config;
  
  private File file;
  
  private BukkitTask particles;
  
  @EventHandler
  public void onTeleport(PlayerTeleportEvent e) {
    if (!e.getFrom().getWorld().equals(e.getTo().getWorld()) || e.getFrom().distance(e.getTo()) > 3.0D) {
      if (this.isJumping.containsKey(e.getPlayer()))
        ((ArmorStand)this.isJumping.get(e.getPlayer())).remove(); 
      this.isJumping.remove(e.getPlayer());
    } 
  }
  
  @EventHandler
  public void onDismount(EntityDismountEvent e) {
    if (e.getEntity() instanceof Player && 
      this.isJumping.containsKey(e.getEntity()))
      Bukkit.getScheduler().runTask((Plugin)Main.plugin, () -> e.getDismounted().setPassenger(e.getEntity())); 
  }
  
  public static JumpPad getJumpPadByName(String name) {
    for (JumpPad jumpPad : jumpPads) {
      if (jumpPad.jumpPadName.equalsIgnoreCase(name))
        return jumpPad; 
    } 
    return null;
  }
  
  public static void getList(Player p) {
    for (JumpPad pad : jumpPads)
      p.sendMessage(Main.chat(" &a- " + pad.jumpPadName)); 
  }
  
  public void getInfo(Player p) {
    p.sendMessage(Main.chat("&7&m----------&f " + this.jumpPadName + " &7&m----------"));
    p.sendMessage(Main.chat("&7Needed Permission: &f" + ((this.neededPermission == null) ? "&cNone" : this.neededPermission)));
    p.sendMessage(Main.chat("&7Permission Message: &f" + ((this.permissionMessage == null) ? "&cNone" : this.permissionMessage)));
    p.sendMessage(Main.chat("&7Fly To Location: &f" + ((this.flyToLocation == null) ? "&cNot set" : (this.flyToLocation.getBlockX() + ", " + this.flyToLocation.getBlockY() + ", " + this.flyToLocation.getBlockZ()))));
    p.sendMessage(Main.chat("&7Pos1 X/Z: &f" + ((this.pos1X == 0) ? "&cNot set" : (this.pos1X + "/" + this.pos1Z))));
    p.sendMessage(Main.chat("&7Pos2 X/Z: &f" + ((this.pos2X == 0) ? "&cNot set" : (this.pos2X + "/" + this.pos1Z))));
  }
  
  public JumpPad(Player p, File fromFile, String name) {
    if (fromFile == null) {
      this.jumpPadName = name;
      File newFile = new File(Main.plugin.getDataFolder(), "jumppads/" + name + ".yml");
      try {
        newFile.createNewFile();
        this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(newFile);
        this.pos1X = 0;
        this.pos1Z = 0;
        this.pos2X = 0;
        this.pos2Z = 0;
        this.relativePos1 = 0;
        this.relativePos2 = 0;
        jumpPads.add(this);
        this.file = newFile;
        this.config.set("name", this.jumpPadName);
        Bukkit.getPluginManager().registerEvents(this, (Plugin)Main.plugin);
        p.sendMessage(Main.chat("&aJump pad created!"));
      } catch (Exception x) {
        p.sendMessage(Main.chat("&cThat jump pad already exists!"));
      } 
    } else {
      this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(fromFile);
      this.jumpPadName = this.config.getString("name");
      this.neededPermission = this.config.getString("needed-permission");
      this.flyToLocation = Main.locationFromBase64(this.config.getString("fly-to-location"));
      this.pos1X = this.config.getInt("pos1X");
      this.pos1Z = this.config.getInt("pos1Z");
      this.pos2X = this.config.getInt("pos2X");
      this.pos2Z = this.config.getInt("pos2Z");
      this.relativePos1 = this.config.getInt("relativePos1");
      this.relativePos2 = this.relativePos1 + 1;
      this.permissionMessage = this.config.getString("permission-message");
      jumpPads.add(this);
      this.file = fromFile;
      Bukkit.getPluginManager().registerEvents(this, (Plugin)Main.plugin);
      spawnParticles();
    } 
  }
  
  public void delete() {
    this.file.delete();
    this.jumpPadName = null;
    this.config = null;
    HandlerList.unregisterAll(this);
    jumpPads.remove(this);
    if (this.particles != null)
      this.particles.cancel(); 
  }
  
  public void spawnParticles() {
    if (this.flyToLocation == null || this.pos1X == 0 || this.pos2X == 0)
      return; 
    Location loc = new Location(this.flyToLocation.getWorld(), ((this.pos2X + this.pos1X) / 2), this.relativePos2, ((this.pos2Z + this.pos1Z) / 2));
    if (this.particles != null)
      this.particles.cancel(); 
    this.particles = Bukkit.getScheduler().runTaskTimerAsynchronously((Plugin)Main.plugin, () -> {
          PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.VILLAGER_HAPPY, true, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0.5F, 0.0F, 0.5F, 0.0F, 10, new int[0]);
          for (Player p : loc.getWorld().getPlayers())
            (((CraftPlayer)p).getHandle()).playerConnection.sendPacket((Packet)packet); 
        }10L, 10L);
  }
  
  @EventHandler
  public void onSlime(PlayerMoveEvent e) {
    Location from = e.getFrom();
    Location to = e.getTo();
    if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
      int x = to.getBlockX();
      int z = to.getBlockZ();
      if (this.pos1X <= x && this.pos2X >= x && this.pos1Z <= z && this.pos2Z >= z && to.getBlockY() == this.relativePos2 && 
        !this.isJumping.containsKey(e.getPlayer()))
        launch(e.getPlayer()); 
    } 
  }
  
  private double yCalculate(double a, double b, double c, double x) {
    return a * x * x + x * b + c;
  }
  
  private void moveToward(Entity player, double speed, double yC) {
    Location loc = player.getLocation();
    double x = loc.getX() - this.flyToLocation.getX();
    double y = loc.getY() - this.flyToLocation.getY() - ((yC > 0.0D) ? yC : 0.0D);
    double z = loc.getZ() - this.flyToLocation.getZ();
    Vector velocity = (new Vector(x, y, z)).normalize().multiply(-speed);
    player.setVelocity(velocity);
  }
  
  private void launch(Player p) {
    if (this.flyToLocation == null) {
      p.sendMessage(Main.chat("&cDestination of jump pad not set."));
      return;
    } 
    if (!this.flyToLocation.getWorld().equals(p.getWorld()))
      return; 
    if (this.neededPermission == null || p.hasPermission(this.neededPermission)) {
      ArmorStand am = (ArmorStand)p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
      am.setVisible(false);
      am.setPassenger((Entity)p);
      this.isJumping.put(p, am);
      Main.spawnExplosion(p);
      (new Object(this, p, am))
        
        .runTaskTimerAsynchronously((Plugin)Main.plugin, 1L, 1L);
    } else if (this.permissionMessage != null) {
      p.sendMessage(this.permissionMessage);
    } 
  }
  
  public void setPos1(Location loc) {
    this.pos1X = loc.getBlockX();
    this.pos1Z = loc.getBlockZ();
    this.relativePos1 = loc.getBlockY();
    this.relativePos2 = this.relativePos1 + 1;
    if (this.pos2X != 0) {
      if (this.pos2X < this.pos1X) {
        int aux = this.pos2X;
        this.pos2X = this.pos1X;
        this.pos1X = aux;
      } 
      if (this.pos2Z < this.pos1Z) {
        int aux = this.pos2Z;
        this.pos2Z = this.pos1Z;
        this.pos1Z = aux;
      } 
    } 
    if (this.pos2X - this.pos1X > 5 || this.pos2Z - this.pos1Z > 5) {
      this.pos2X = this.pos1X;
      this.pos2Z = this.pos1Z;
    } 
    this.config.set("pos1X", Integer.valueOf(this.pos1X));
    this.config.set("pos1Z", Integer.valueOf(this.pos1Z));
    this.config.set("pos2X", Integer.valueOf(this.pos2X));
    this.config.set("pos2Z", Integer.valueOf(this.pos2Z));
    this.config.set("relativePos1", Integer.valueOf(this.relativePos1));
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    spawnParticles();
  }
  
  public void setPos2(Location loc) {
    this.pos2X = loc.getBlockX();
    this.pos2Z = loc.getBlockZ();
    this.relativePos1 = loc.getBlockY();
    this.relativePos2 = this.relativePos1 + 1;
    if (this.pos1X != 0) {
      if (this.pos2X < this.pos1X) {
        int aux = this.pos2X;
        this.pos2X = this.pos1X;
        this.pos1X = aux;
      } 
      if (this.pos2Z < this.pos1Z) {
        int aux = this.pos2Z;
        this.pos2Z = this.pos1Z;
        this.pos1Z = aux;
      } 
    } 
    if (this.pos2X - this.pos1X > 5 || this.pos2Z - this.pos1Z > 5) {
      this.pos1X = this.pos2X;
      this.pos1Z = this.pos2Z;
    } 
    this.config.set("pos1X", Integer.valueOf(this.pos1X));
    this.config.set("pos1Z", Integer.valueOf(this.pos1Z));
    this.config.set("pos2X", Integer.valueOf(this.pos2X));
    this.config.set("pos2Z", Integer.valueOf(this.pos2Z));
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    spawnParticles();
  }
  
  public void setFlyLocation(Location loc) {
    this.flyToLocation = loc;
    this.config.set("fly-to-location", Main.locationToBase64(loc));
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
    spawnParticles();
  }
  
  public void setPermission(String perm) {
    if (perm.equalsIgnoreCase("none"))
      this.neededPermission = null; 
    this.neededPermission = perm;
    this.config.set("needed-permission", this.neededPermission);
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void setPermissionMessage(String message) {
    if (message == null) {
      this.permissionMessage = null;
    } else {
      this.permissionMessage = Main.chat(message.replace("_", " "));
    } 
    this.config.set("permission-message", this.permissionMessage);
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
}
