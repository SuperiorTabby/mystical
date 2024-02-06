package com.skycat.mystical.common.spell.consequence;

import com.mojang.serialization.Codec;
import com.skycat.mystical.common.spell.Spells;
import com.skycat.mystical.common.util.Utils;
import com.skycat.mystical.test.Testable;
import lombok.Getter;
import lombok.NonNull;
import net.minecraft.test.TestContext;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class ConsequenceFactory<T extends SpellConsequence> implements Testable {
    public static final Codec<ConsequenceFactory<?>> FACTORY_CODEC = Codec.STRING.xmap(Spells::getFactory, ConsequenceFactory::getShortName);
    public static final String CONSEQUENCE_TRANSLATION_PREFIX = "text.mystical.consequence.";
    @Getter public final String shortName;
    @Getter public final String longName;
    @Getter public final String description;
    @Getter public final String firedMessage;
    @Getter public final Class<T> consequenceType;
    @Getter public final Codec<T> codec;

    protected ConsequenceFactory(String shortName, String longName, String description, String firedMessage, Class<T> consequenceType, Codec<T> codec) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
        this.firedMessage = firedMessage;
        this.consequenceType = consequenceType;
        this.codec = codec;
    }

    public String getDescriptionKey() {
        return translationKey() + ".description";
    }

    /**
     * This is a player-readable description of the consequence.
     * Override this if you have parameters to add to the translation.
     *
     * @param consequence The consequence to get the description of. Used for parameterized translations.
     * @throws IllegalArgumentException If consequence.class != consequenceType
     */
    public MutableText getDescriptionText(SpellConsequence consequence) throws IllegalArgumentException {
        if (consequence.getClass() != consequenceType) {
            throw new IllegalArgumentException(consequence.getClass() + " != " + consequenceType); // TODO: Logging instead of crashing
        }
        return Utils.translatable(getDescriptionKey());
    }

    public String getLongNameKey() {
        return translationKey() + ".longName";
    }

    public MutableText getLongNameText() {
        return Utils.translatable(getLongNameKey());
    }

    public String getShortNameKey() {
        return translationKey() + ".shortName";
    }

    public MutableText getShortNameText() {
        return Utils.translatable(getShortNameKey());
    }

    /**
     * Return the config option's chance for the consequence, or 0 if it's disabled
     * Probably an awful way to do this
     *
     * @return the config option's chance for the consequence, or 0 if it's disabled
     */
    public abstract double getWeight();

    /**
     * Make a new consequence of class {@link T}
     *
     * @param random The random to use to generate anything that should be randomized
     * @param points The point target to aim for.
     * @return A new {@link T}.
     */
    @NotNull
    public abstract T make(@NonNull Random random, double points);

    /**
     * Get the base translation "path" for this consequence
     *
     * @return The base part of the translations for this spell
     * @implNote Returns {@link ConsequenceFactory#CONSEQUENCE_TRANSLATION_PREFIX} + getShortName(). There is probably not a translation at this key - this is just a base "path"
     */
    public String translationKey() {
        return CONSEQUENCE_TRANSLATION_PREFIX + getShortName();
    }

    @Override
    public String getTemplatePath() {
        return "mysticaltests.spell." + shortName;
    }

    /**
     * Tests {@link T}. <br>
     * Make sure to test in and out of havens, as well as with/without spell being active.
     *
     * @param context The context to test in.
     */
    public abstract void test(TestContext context);
}
