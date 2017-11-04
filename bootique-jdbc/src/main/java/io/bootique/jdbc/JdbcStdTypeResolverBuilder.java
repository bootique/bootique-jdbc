package io.bootique.jdbc;

import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @since 0.25
 */
public class JdbcStdTypeResolverBuilder extends StdTypeResolverBuilder {

    @Override
    public Class<?> getDefaultImpl() {
        ServiceLoader<CPDataSourceFactory> serviceLoader = ServiceLoader.load(CPDataSourceFactory.class);

        List<CPDataSourceFactory> factoryList = new ArrayList<>();
        for (CPDataSourceFactory factory : serviceLoader) {
            factoryList.add(factory);
        }

        if (factoryList.size() == 1) {
            CPDataSourceFactory factory = factoryList.get(0);
            _defaultImpl = factory.getClass();
        }

        return _defaultImpl;
    }
}
