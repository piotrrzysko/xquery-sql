package io.github.piotrrzysko;

import io.airlift.slice.Slice;
import io.trino.re2j.Options;
import io.trino.re2j.Pattern;

import static io.trino.re2j.Options.Algorithm.DFA_FALLBACK_TO_NFA;

public class Re2JRegex
{
    private final Pattern re2jPattern;

    public Re2JRegex(String pattern)
    {
        Options options = Options.builder()
                .setAlgorithm(DFA_FALLBACK_TO_NFA)
                .setMaximumNumberOfDFAStates(Integer.MAX_VALUE)
                .setNumberOfDFARetries(5)
                .build();

        re2jPattern = Pattern.compile(pattern, options);
    }

    public boolean matches(Slice source)
    {
        return re2jPattern.find(source);
    }
}
