package io.github.piotrrzysko;

import io.airlift.jcodings.specific.NonStrictUTF8Encoding;
import io.airlift.joni.Matcher;
import io.airlift.joni.Option;
import io.airlift.joni.Regex;
import io.airlift.joni.Syntax;
import io.airlift.slice.Slice;

public class JoniRegex
{
    private final Regex regex;

    public JoniRegex(String pattern)
    {
        regex = new Regex(pattern.getBytes(), 0, pattern.length(), Option.DEFAULT, NonStrictUTF8Encoding.INSTANCE, Syntax.Java);
    }

    public boolean matches(Slice input)
    {
        int offset = input.byteArrayOffset();
        Matcher matcher = regex.matcher(input.byteArray(), offset, offset + input.length());
        return matcher.search(offset, offset + input.length(), Option.DEFAULT) != -1;
    }
}
