package io.bootique.jdbc.test.derby;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DerbyLifecycleListenerTest {

    @Test
    public void testGetDbDir() {

        assertFalse(DerbyListener.getDbDir(Optional.empty()).isPresent());
        assertFalse(DerbyListener.getDbDir(Optional.of("jdbc:mysql:")).isPresent());
        assertFalse(DerbyListener.getDbDir(Optional.of("jdbc:mysql:mydb")).isPresent());
        assertFalse(DerbyListener.getDbDir(Optional.of("jdbc:derby:")).isPresent());
        assertEquals("dir1/dir2", DerbyListener.getDbDir(Optional.of("jdbc:derby:dir1/dir2")).get());
        assertEquals("dir1/dir2", DerbyListener.getDbDir(Optional.of("jdbc:derby:dir1/dir2;create=true")).get());
    }
}
