package com.skycat.mystical.common.spell.consequence;

import com.mojang.serialization.Codec;
import com.skycat.mystical.Mystical;
import lombok.NonNull;
import net.minecraft.test.TestContext;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class NoFuseConsequence extends SpellConsequence {
    public static final Factory FACTORY = new Factory();

    public NoFuseConsequence() {
        super(NoFuseConsequence.class, null, 100d);
    }

    @Override
    public @NonNull ConsequenceFactory<? extends SpellConsequence> getFactory() {
        return FACTORY;
    }

    public static class Factory extends ConsequenceFactory<NoFuseConsequence> {
        protected Factory() {
            super("noFuse",
                    "No Fuses",
                    "Creepers don't hesitate anymore.",
                    "Destroyed a fuse",
                    NoFuseConsequence.class,
                    Codec.unit(NoFuseConsequence::new));
        }

        @Override
        public @NotNull NoFuseConsequence make(@NonNull Random random, double points) {
            return new NoFuseConsequence();
        }

        @Override
        public void test(TestContext context) {
            // TODO
        }


        @Override
        public double getWeight() {
            return (Mystical.CONFIG.noFuse.enabled() ? Mystical.CONFIG.noFuse.weight() : 0);
        }
    }
}
