package net.doodcraft.cozmyc.sonicboom;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SonicBoom extends AirAbility implements AddonAbility {

    @Attribute(Attribute.COOLDOWN)
    private final long cooldown;

    @Attribute(Attribute.DAMAGE)
    private final double damage;

    @Attribute(Attribute.RANGE)
    private final double maxDistance;

    @Attribute("TravelSpeed")
    private final double travelSpeed;

    @Attribute(Attribute.CHARGE_DURATION) // todo: double check this..
    private int warmupTicks;

    private Location origin;
    private Vector direction;

    private boolean isWarmedup;
    private double currentDistance;

    public SonicBoom(Player player) {
        super(player);

        this.player = player;
        this.direction = player.getLocation().getDirection();
        this.cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Cozmyc.SonicBoom.Cooldown", 10000);
        this.travelSpeed = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.TravelSpeed", 2.0);
        this.damage = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.Damage", 10.0);
        this.warmupTicks = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.WarmupTicks", 20);
        this.maxDistance = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.Range", 32);
        this.currentDistance = 0;

        if (!bPlayer.canBend(this) || CoreAbility.hasAbility(player, SonicBoom.class)) {
            return;
        }

        this.origin = player.getEyeLocation();
        this.isWarmedup = false;

        start();
    }

    @Override
    public void progress() {
        if (!player.isSneaking()) {
            remove();
            return;
        }

        if (!isWarmedup) {
            if (--warmupTicks <= 0) {
                isWarmedup = true;
                origin = player.getEyeLocation();
                direction = player.getLocation().getDirection();
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 1.0f);
            } else {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.2f, 1.0f);
            }
            return;
        }

        currentDistance += travelSpeed;
        if (currentDistance >= maxDistance) {
            remove();
            return;
        }

        Location particleLocation = origin.clone().add(direction.clone().multiply(currentDistance));
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLocation, 1, 0.0, 0.0, 0.0, 0.01, null, true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 0.2f);

        GeneralMethods.getEntitiesAroundPoint(particleLocation, 1).stream()
                .filter(entity -> !(entity instanceof Player) && entity.getUniqueId() != player.getUniqueId())
                .filter(entity -> entity instanceof LivingEntity)
                .forEach(entity -> DamageHandler.damageEntity(entity, player, damage, this, false));
    }

    @Override
    public void remove() {
        bPlayer.addCooldown(this);
        super.remove();
    }

    @Override
    public boolean isSneakAbility() {
        return false;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public boolean isIgniteAbility() {
        return false;
    }

    @Override
    public boolean isExplosiveAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return this.cooldown;
    }

    @Override
    public String getName() {
        return "SonicBoom";
    }

    @Override
    public Location getLocation() {
        return origin;
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new SonicBoomListener(), ProjectKorra.plugin);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Cooldown", 10000);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Damage", 10.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Range", 32);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.TravelSpeed", 2.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.WarmupTicks", 20);

        ConfigManager.defaultConfig.save();

        ConfigManager.languageConfig.get().addDefault("Abilities.Air.SonicBoom.DeathMessage", "{victim} was obliterated by {attacker}'s {ability}");
        ConfigManager.languageConfig.get().addDefault("Abilities.Air.SonicBoom.Description", "Amplify your voice into a beam of pure and devastating air pressure.");
        ConfigManager.languageConfig.get().addDefault("Abilities.Air.SonicBoom.Instructions", "Hold shift while aiming at your target.");

        ConfigManager.languageConfig.save();
    }

    @Override
    public void stop() {}

    @Override
    public String getAuthor() {
        return "Cozmyc";
    }

    @Override
    public String getVersion() {
        return "0.0.2";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
