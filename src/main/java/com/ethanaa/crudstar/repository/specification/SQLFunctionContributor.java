package com.ethanaa.crudstar.repository.specification;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;

public class SQLFunctionContributor implements MetadataBuilderContributor {

    @Override
    public void contribute(MetadataBuilder metadataBuilder) {

        metadataBuilder.applySqlFunction("json_search_function", new JsonSearchFunction());
    }
}
