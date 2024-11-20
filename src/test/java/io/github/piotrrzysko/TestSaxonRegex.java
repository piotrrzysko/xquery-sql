package io.github.piotrrzysko;

import io.airlift.slice.Slice;

public class TestSaxonRegex
        extends BaseRegexTest
{
    @Override
    protected boolean matches(String pattern, String flags, Slice input)
    {
        SaxonRegex regex = new SaxonRegex(pattern, flags);
        return regex.matches(input);
    }
}
