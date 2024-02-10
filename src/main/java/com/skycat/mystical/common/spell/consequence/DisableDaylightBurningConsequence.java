package com.skycat.mystical.common.spell.consequence;

import com.mojang.serialization.Codec;
import com.skycat.mystical.Mystical;
import com.skycat.mystical.test.TestUtils;
import lombok.NonNull;
import net.minecraft.entity.EntityType;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class DisableDaylightBurningConsequence extends SpellConsequence { // TODO: Config
    public static Factory FACTORY = new Factory();

    protected DisableDaylightBurningConsequence() {
        super(DisableDaylightBurningConsequence.class, null, 15d); // TODO: Scaling
    }

    @Override
    public @NotNull ConsequenceFactory<? extends SpellConsequence> getFactory() {
        return FACTORY;
    }

    public static class Factory extends ConsequenceFactory<DisableDaylightBurningConsequence> {

        protected Factory() {
            super("disableDaylightBurning",
                    "Disable Daylight Burning",
                    "Mobs bought some sunscreen",
                    "Said no this mob doesn't burn",
                    DisableDaylightBurningConsequence.class,
                    Codec.unit(DisableDaylightBurningConsequence::new));
        }

        @Override
        public @NotNull DisableDaylightBurningConsequence make(@NonNull Random random, double points) {
            return new DisableDaylightBurningConsequence();
        }


        @Override
        public double getWeight() {
            return (Mystical.CONFIG.disableDaylightBurning.enabled() ? Mystical.CONFIG.disableDaylightBurning.weight() : 0);
        }

        @GameTest(templateName = TestUtils.BORDERED_BARRIER_BOX)
        public void testHaven(TestContext context) { // TODO: Test
            TestUtils.resetMystical(context);
            context.killAllEntities();
            context.setTime(1000); // Make it day
            context.setHealthLow(context.spawnMob(EntityType.ZOMBIE, 2, 2, 2));
            context.waitAndRun(75, () -> {
                context.dontExpectEntity(EntityType.ZOMBIE);
            });
            context.complete();
        }

        @GameTest(templateName = TestUtils.BORDERED_BARRIER_BOX)
        public void testHavenAndSpell(TestContext context) {
            TestUtils.resetMystical(context);
            context.killAllEntities();
            context.setTime(1000); // Make it day
            context.setHealthLow(context.spawnMob(EntityType.ZOMBIE, 2, 2, 2));
            context.waitAndRun(75, () -> {
                context.dontExpectEntity(EntityType.ZOMBIE);
            });
            context.complete();
        }

        @GameTest(templateName = TestUtils.BORDERED_BARRIER_BOX)
        public void testSpell(TestContext context) {
            TestUtils.resetMystical(context);
            context.killAllEntities();
            context.setTime(1000); // Make it day
            context.setHealthLow(context.spawnMob(EntityType.ZOMBIE, 2, 2, 2));
            context.waitAndRun(75, () -> {
                context.expectEntity(EntityType.ZOMBIE);
            });
            context.complete();
        }

        @GameTest(templateName = TestUtils.BORDERED_BARRIER_BOX)
        public void testVanilla(TestContext context) {
            TestUtils.resetMystical(context);
            context.killAllEntities();
            context.setTime(1000); // Make it day
            context.setHealthLow(context.spawnMob(EntityType.ZOMBIE, 2, 2, 2));
            context.waitAndRun(75, () -> {
                context.dontExpectEntity(EntityType.ZOMBIE);
            });
            context.complete();
        }
    }
}
