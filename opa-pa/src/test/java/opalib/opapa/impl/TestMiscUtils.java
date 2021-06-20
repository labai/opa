package opalib.opapa.impl;

import org.junit.Test;

import static opalib.opapa.impl.MiscUtils.trimLastSlash;
import static org.junit.Assert.assertEquals;

/**
 * @author Augustus
 * created on 2021.06.19
 */
public class TestMiscUtils {

    @Test
    public void testTrimLastSlash() {
        assertEquals("a", trimLastSlash("a/"));
        assertEquals("a", trimLastSlash("a"));
        assertEquals("/a", trimLastSlash("/a/"));
        assertEquals("", trimLastSlash(""));
        assertEquals(null, trimLastSlash(null));
    }
}
