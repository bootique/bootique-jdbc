package io.bootique.jdbc.test.dataset;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GroupDataSetTest {

    @Test
    public void testPersist() {
        DataSet ds1 = mock(DataSet.class);
        DataSet ds2 = mock(DataSet.class);

        new GroupDataSet(ds1, ds2).persist();
        verify(ds1).persist();
        verify(ds2).persist();
    }
}
