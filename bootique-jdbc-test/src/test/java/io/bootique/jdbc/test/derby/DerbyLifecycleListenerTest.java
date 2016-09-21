package io.bootique.jdbc.test.derby;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by andrus on 9/21/16.
 */
public class DerbyLifecycleListenerTest {

    @Test
    public void testGetDbDir() {

        assertFalse(DerbyLifecycleListener.getDbDir("jdbc:mysql:").isPresent());
        assertFalse(DerbyLifecycleListener.getDbDir("jdbc:derby:").isPresent());
        assertEquals("dir1/dir2", DerbyLifecycleListener.getDbDir("jdbc:derby:dir1/dir2").get());
        assertEquals("dir1/dir2", DerbyLifecycleListener.getDbDir("jdbc:derby:dir1/dir2;create=true").get());

    }
}
