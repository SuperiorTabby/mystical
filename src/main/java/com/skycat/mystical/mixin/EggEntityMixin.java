package com.skycat.mystical.mixin;

import com.skycat.mystical.MysticalTags;
import com.skycat.mystical.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.world.World;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin extends ThrownItemEntity {
    /**
     * Dummy method. Don't call this.
     */
    @Contract("_,_->fail")
    private EggEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
        throw new AssertionError("DON'T CALL THIS YOU SILLY PIE");
    }

    @ModifyArg(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    public Entity mystical_onEggCollision(Entity chicken) { // TODO: Depend on spell
        EntityType<?> randomType = Utils.getRandomEntryFromTag(Registries.ENTITY_TYPE, MysticalTags.RANDOM_EGG_SPAWNABLE);
        if (randomType != null) {
            Entity randomEntity = randomType.create(chicken.getWorld());
            if (randomEntity != null) {
                randomEntity.refreshPositionAndAngles(getX(), getY(), getZ(), getYaw(), 0);
                chicken.discard();
                return randomEntity;
            }
        }
        Utils.log("Unable to spawn a random egg mob."); // TODO: Translate
        return chicken;
    }
}
