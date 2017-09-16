package io.bootique.jdbc.test.dataset;

import java.util.Collection;

import static java.util.Arrays.asList;

/**
 * A DataSet that can persist multiple underlying datasets in a specific order.
 *
 * @since 0.24
 */
public class GroupDataSet implements DataSet {

    private Collection<DataSet> dataSets;

    public GroupDataSet(DataSet... dataSets) {
        this(asList(dataSets));
    }

    public GroupDataSet(Collection<DataSet> dataSets) {
        this.dataSets = dataSets;
    }

    @Override
    public void persist() {
        dataSets.forEach(DataSet::persist);
    }
}
