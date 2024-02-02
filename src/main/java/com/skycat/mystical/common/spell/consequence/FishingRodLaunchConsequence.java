package com.skycat.mystical.common.spell.consequence;

import com.mojang.serialization.Codec;
import com.skycat.mystical.Mystical;
import lombok.NonNull;
import net.minecraft.test.TestFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class FishingRodLaunchConsequence extends SpellConsequence {
    public static final Factory FACTORY = new Factory();

    @Override
    public @NotNull ConsequenceFactory<FishingRodLaunchConsequence> getFactory() {
        return FACTORY;
    }

    public FishingRodLaunchConsequence() {
        super(FishingRodLaunchConsequence.class, null, -5d); // TODO: Scaling
    }

    public static class Factory extends ConsequenceFactory<FishingRodLaunchConsequence> {

        public Factory() {
            super("fishingRodLaunch",
                    "Fishing Rod Launch",
                    "Hehe. Rod make cow go zoom.",
                    "Fishing rod power multiplied",
                    FishingRodLaunchConsequence.class,
                    Codec.unit(FishingRodLaunchConsequence::new));
        }

        @Override
        public @NotNull FishingRodLaunchConsequence make(@NonNull Random random, double points) {
            return new FishingRodLaunchConsequence();
        }

        @Override
        public @Nullable TestFunction getTestFunction() {
            return null; // TODO
        }

        @Override
        public double getWeight() {
            return (Mystical.CONFIG.fishingRodLaunch.enabled() ? Mystical.CONFIG.fishingRodLaunch.weight() : 0);
        }
    }
}
