package com.lld.ratelimiter;

import com.lld.ratelimiter.model.RateLimitResult;

import com.lld.ratelimiter.config.EndpointDefinition;
import com.lld.ratelimiter.config.EndpointPlanPolicy;
import com.lld.ratelimiter.config.TokenBucketParams;
import com.lld.ratelimiter.model.ClientEndpointPlan;
import com.lld.ratelimiter.model.ClientPlan;
import com.lld.ratelimiter.model.RateLimitAlgorithmType;
import com.lld.ratelimiter.facade.RateLimiterFacade;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        RateLimiterFacade rateLimiterFacade = RateLimiterFacade.getInstance();

        rateLimiterFacade.registerEndpoint(
                new EndpointDefinition("/orders", RateLimitAlgorithmType.TOKEN_BUCKET)
        );

        rateLimiterFacade.registerEndpoint(
                new EndpointDefinition("DEFAULT", RateLimitAlgorithmType.TOKEN_BUCKET)
        );

        rateLimiterFacade.registerEndpointPlanPolicy(
                new EndpointPlanPolicy(
                        "/orders",
                        ClientPlan.DEFAULT,
                        new TokenBucketParams(5, 1)
                )
        );

        rateLimiterFacade.registerEndpointPlanPolicy(
                new EndpointPlanPolicy(
                        "/orders",
                        ClientPlan.PREMIUM,
                        new TokenBucketParams(10, 5)
                )
        );

        rateLimiterFacade.registerEndpointPlanPolicy(
                new EndpointPlanPolicy(
                        "DEFAULT",
                        ClientPlan.DEFAULT,
                        new TokenBucketParams(2, 1)
                )
        );

        rateLimiterFacade.registerClientEndpointPlan(
                new ClientEndpointPlan("clientA", "/orders", ClientPlan.DEFAULT)
        );

        rateLimiterFacade.registerClientEndpointPlan(
                new ClientEndpointPlan("clientB", "/orders", ClientPlan.PREMIUM)
        );

        for (int i = 1; i <= 8; i++) {
            RateLimitResult clientAResult =
                    rateLimiterFacade.allow("clientA", "/orders");

            System.out.println(
                    "clientA request " + i
                            + " allowed=" + clientAResult.isAllowed()
                            + " remaining=" + clientAResult.getRemaining()
                            + " retryAfterMs=" + clientAResult.getRetryAfterMs()
            );

            RateLimitResult clientBResult =
                    rateLimiterFacade.allow("clientB", "/orders");

            System.out.println(
                    "clientB request " + i
                            + " allowed=" + clientBResult.isAllowed()
                            + " remaining=" + clientBResult.getRemaining()
                            + " retryAfterMs=" + clientBResult.getRetryAfterMs()
            );

            Thread.sleep(100);
        }
    }
}
