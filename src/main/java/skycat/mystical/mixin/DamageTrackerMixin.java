package skycat.mystical.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skycat.mystical.Mystical;
import skycat.mystical.spell.consequence.EnderTypeChangeConsequence;
import skycat.mystical.spell.consequence.SkeletonTypeChangeConsequence;

@Mixin(DamageTracker.class)
public abstract class DamageTrackerMixin {

    @Shadow @Final private LivingEntity entity;

    @Inject(method = "onDamage", at = @At("HEAD"))
    public void onDamage(DamageSource damageSource, float originalHealth, float damage, CallbackInfo ci) {
        if (entity instanceof AbstractSkeletonEntity) {
            if (Mystical.SPELL_HANDLER.isConsequenceActive(SkeletonTypeChangeConsequence.class) && // Spell is active
                    !damageSource.isOutOfWorld() && // Damage from normal source
                    Mystical.RANDOM.nextFloat(0, 100) >= Mystical.CONFIG.skeletonTypeChange.chance()) { // Roll the dice
                float totalDamage = (entity.getMaxHealth() - originalHealth) + damage;
                Mystical.LOGGER.info("total: " + totalDamage + " max: " + entity.getMaxHealth() + " original: " + originalHealth + " damage: " + damage);
                // Convert
                ((AbstractSkeletonEntity) entity).convertTo(Util.getRandom(SkeletonTypeChangeConsequence.SKELETON_TYPES, Mystical.MC_RANDOM), true)
                        .damage(DamageSource.OUT_OF_WORLD, totalDamage); // Do the damage TODO check for null (shouldn't happen though)
            }
        } else {
            if (entity instanceof EndermanEntity || entity instanceof EndermiteEntity) {
                if (Mystical.SPELL_HANDLER.isConsequenceActive(EnderTypeChangeConsequence.class) && // Spell is active
                        !damageSource.isOutOfWorld() && // Damage from normal source
                        Mystical.RANDOM.nextFloat(0, 100) >= Mystical.CONFIG.enderTypeChange.chance()) { // Roll the dice
                    float totalDamage = (entity.getMaxHealth() - originalHealth) + damage;
                    Mystical.LOGGER.info("total: " + totalDamage + " max: " + entity.getMaxHealth() + " original: " + originalHealth + " damage: " + damage);
                    // Convert
                    EntityType<? extends MobEntity> convertToType = EntityType.ENDERMITE;
                    if (entity instanceof EndermiteEntity) { // If it's an endermite, turn it into an enderman instead.
                        convertToType = EntityType.ENDERMAN;
                    }
                    ((MobEntity) entity).convertTo(convertToType, true)
                            .damage(DamageSource.OUT_OF_WORLD, totalDamage); // Do the damage TODO check for null (shouldn't happen though)
                }
            }
        }
    }
}