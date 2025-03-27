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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SonicBoom extends AirAbility implements AddonAbility {

    private Location origin;
    private Vector direction;
    private boolean isWarmedup;
    private double currentDistance;

    @Attribute(Attribute.COOLDOWN)
    private final long cooldown;
    @Attribute(Attribute.DAMAGE)
    private final double damage;
    @Attribute(Attribute.RANGE)
    private final double maxDistance;
    @Attribute(Attribute.KNOCKBACK)
    private final double knockbackStrength;
    @Attribute(Attribute.RADIUS)
    private final double entityCollisionRadius;
    @Attribute("TravelSpeed")
    private final double travelSpeed;
    @Attribute("NauseaTicks")
    private final int nauseaDuration;
    @Attribute("DarknessTicks")
    private final int darknessDuration;
    @Attribute("SwapWhileCharging")
    private final boolean allowChargeSwapping;
    @Attribute(Attribute.CHARGE_DURATION)
    private int warmupTicks;

    public SonicBoom(Player player) {
        super(player);

        this.cooldown = ConfigManager.defaultConfig.get().getLong("ExtraAbilities.Cozmyc.SonicBoom.Cooldown", 11000);
        this.travelSpeed = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.TravelSpeed", 3.0);
        this.damage = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.Damage", 7.0);
        this.warmupTicks = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.WarmupTicks", 40);
        this.maxDistance = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.Range", 32);
        this.darknessDuration = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.DarknessTicks", 80);
        this.entityCollisionRadius = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.EntityCollisionRadius", 1.0);
        this.knockbackStrength = ConfigManager.defaultConfig.get().getDouble("ExtraAbilities.Cozmyc.SonicBoom.KnockbackStrength", 3.5);
        this.nauseaDuration = ConfigManager.defaultConfig.get().getInt("ExtraAbilities.Cozmyc.SonicBoom.NauseaTicks", 140);
        this.allowChargeSwapping = ConfigManager.defaultConfig.get().getBoolean("ExtraAbilities.Cozmyc.SonicBoom.SwapWhileCharging", false);

        this.player = player;
        this.direction = player.getLocation().getDirection();
        this.currentDistance = 0;

        if (!bPlayer.canBend(this) || CoreAbility.hasAbility(player, SonicBoom.class)) {
            return;
        }

        this.origin = player.getEyeLocation();
        this.isWarmedup = false;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.2f, 1.0f);

        start();
    }

    @Override
    public void progress() {
        CoreAbility boundAbility = bPlayer.getBoundAbility();

        if (!player.isSneaking() || (!(boundAbility instanceof SonicBoom) && !allowChargeSwapping)) {
            remove();
            bPlayer.addCooldown(this);
            return;
        }

        if (!isWarmedup) {
            if (--warmupTicks <= 0) {
                isWarmedup = true;
                origin = player.getEyeLocation();
                direction = player.getLocation().getDirection();
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 1.0f);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.2f);
            }
            return;
        }

        bPlayer.addCooldown(this);

        currentDistance += travelSpeed;
        if (currentDistance >= maxDistance) {
            remove();
            return;
        }

        Location particleLocation = origin.clone().add(direction.clone().multiply(currentDistance));
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, particleLocation, 1, 0.0, 0.0, 0.0, 0.01, null, true);
        player.getWorld().spawnParticle(Particle.DUST_PLUME, particleLocation, 6, Math.random()/3, Math.random()/3, Math.random()/3, 0.03, null, true);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 2.0f, 0.2f);

        GeneralMethods.getEntitiesAroundPoint(particleLocation, entityCollisionRadius).stream()
                .filter(entity -> entity.getUniqueId() != player.getUniqueId())
                .forEach(entity -> {
                    entity.setVelocity(direction.clone().normalize().multiply(knockbackStrength));

                    if (entity instanceof LivingEntity) {
                        DamageHandler.damageEntity(entity, player, damage, this, false);
                    }

                    if (entity instanceof Player affectedPlayer) {
                        if (nauseaDuration > 0) {
                            affectedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, nauseaDuration, 7));
                        }

                        if (darknessDuration > 0) {
                            affectedPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, darknessDuration, 0));
                        }
                    }
                });
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
    public String getDescription() {
        return ConfigManager.languageConfig.get().getString("Abilities.Air.SonicBoom.Description", "Missing Description. Check PK's lang.yml.");
    }

    @Override
    public String getInstructions() {
        return ConfigManager.languageConfig.get().getString("Abilities.Air.SonicBoom.Instructions", "Missing Instructions. Check PK's lang.yml.");
    }

    @Override
    public Location getLocation() {
        return origin;
    }

    @Override
    public void load() {
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new SonicBoomListener(), ProjectKorra.plugin);

        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Cooldown", 11000);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Damage", 7.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.DarknessTicks", 80);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.EntityCollisionRadius", 1.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.KnockbackStrength", 3.5);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.NauseaTicks",  140);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.Range", 32);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.SwapWhileCharging", false);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.TravelSpeed", 3.0);
        ConfigManager.defaultConfig.get().addDefault("ExtraAbilities.Cozmyc.SonicBoom.WarmupTicks", 40);

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
        return "1.0.0";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
