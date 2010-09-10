package net.refractions.udig.community.jody.tile.internal;

import java.io.File;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

public class TileServiceExtentionTest extends TestCase {

    public void testURLMadness() throws Exception {
        File file = new File("C:\\Documents and Settings\\jgarnett\\Desktop\\data\\eathimages.xml");
        URL before = file.toURL();
        URI after = TileServiceExtention.toSafeURI( before );
        assertNotNull( after );
        assertTrue( after.toString().indexOf(" ") == -1 );                
    }
}
