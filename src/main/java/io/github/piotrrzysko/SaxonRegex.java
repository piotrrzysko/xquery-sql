package io.github.piotrrzysko;

import io.airlift.slice.Slice;
import net.sf.saxon.regex.ARegularExpression;
import net.sf.saxon.str.StringView;
import net.sf.saxon.trans.XPathException;

public class SaxonRegex
{
    private final ARegularExpression saxon;

    public SaxonRegex(String pattern, String flags)
    {
        try {
            saxon = new ARegularExpression(StringView.of(pattern), flags, "XP31/XSD11", null, null);
        }
        catch (XPathException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matches(Slice input)
    {
        String stringUtf8 = input.toStringUtf8();
        return saxon.containsMatch(StringView.of(stringUtf8));
    }
}
