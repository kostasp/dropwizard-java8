package io.dropwizard.java8.jersey;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

public class OptionalParamFeature implements Feature {
    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new OptionalParamBinder());
        return true;
    }
}
