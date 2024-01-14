package com.ethanaa.crudstar.repository.specification;


import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.util.List;

public class JsonSearchFunction implements SQLFunction {

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return StandardBasicTypes.BOOLEAN;
    }

    @Override
    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
        return "jsonb_to_tsvector('English', " + arguments.get(0) + ", '[\"String\"]') " +
                "@@ websearch_to_tsquery('English', " + arguments.get(1) + ")";
    }
}
