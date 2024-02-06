package com.skycat.mystical.common.spell.consequence;

import com.mojang.serialization.Codec;
import com.skycat.mystical.Mystical;
import lombok.NonNull;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ZombieTypeChangeConsequence extends SpellConsequence {
    public static final Factory FACTORY = new Factory();

    @Override
    public @NotNull ConsequenceFactory<ZombieTypeChangeConsequence> getFactory() {
        return FACTORY;
    }

    public ZombieTypeChangeConsequence() {
        super(ZombieTypeChangeConsequence.class, null, 50d); // TODO: Scaling
    }

    public static class Factory extends ConsequenceFactory<ZombieTypeChangeConsequence> {

        public Factory() {
            super("zombieTypeChange",
                    "Zombie Type Change",
                    "Zombies are having a wardrobe crisis",
                    "Zombie type changed.",
                    ZombieTypeChangeConsequence.class,
                    Codec.unit(ZombieTypeChangeConsequence::new));
        }

        @Override
        public @NotNull ZombieTypeChangeConsequence make(@NonNull Random random, double points) {
            return new ZombieTypeChangeConsequence();
        }

        @Override
        public void test(TestContext context) {
            // TODO
        }


        @Override
        public double getWeight() {
            return (Mystical.CONFIG.zombieTypeChange.enabled() ? Mystical.CONFIG.zombieTypeChange.weight() : 0);
        }
    }
}
