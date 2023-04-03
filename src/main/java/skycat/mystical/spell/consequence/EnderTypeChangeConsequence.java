package skycat.mystical.spell.consequence;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class EnderTypeChangeConsequence extends SpellConsequence {
    public static final Factory FACTORY = new Factory();

    protected EnderTypeChangeConsequence() {
        super(EnderTypeChangeConsequence.class, EnderTypeChangeConsequence.class, "enderTypeChange", "Ender Type Change", "Of mites and men");
    }

    public static class Factory implements ConsequenceFactory<EnderTypeChangeConsequence> {
        @Override
        public @NotNull EnderTypeChangeConsequence make(@NonNull Random random, double points) {
            return new EnderTypeChangeConsequence();
        }
    }

}