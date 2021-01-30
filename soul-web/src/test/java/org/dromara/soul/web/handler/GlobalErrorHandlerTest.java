package org.dromara.soul.web.handler;

import org.dromara.soul.plugin.api.result.DefaultSoulResult;
import org.dromara.soul.plugin.api.result.SoulResult;
import org.dromara.soul.plugin.base.utils.SpringBeanUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * test for GlobalErrorHandler.
 *
 * @author yangxing
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalErrorHandlerTest {

    private GlobalErrorHandler globalErrorHandler;

    @Before
    public void setUp() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        SpringBeanUtils.getInstance().setCfgContext(context);
        when(context.getBean(SoulResult.class)).thenReturn(new DefaultSoulResult() { });

        ErrorAttributes errorAttributes = new DefaultErrorAttributes();
        ResourceProperties resourceProperties = mock(ResourceProperties.class);
        ErrorProperties serverProperties = mock(ErrorProperties.class);
        ApplicationContext applicationContext = new AnnotationConfigReactiveWebApplicationContext();
        ViewResolver viewResolver = mock(ViewResolver.class);

        globalErrorHandler = new GlobalErrorHandler(errorAttributes, resourceProperties, serverProperties, applicationContext);
        globalErrorHandler.setViewResolvers(Collections.singletonList(viewResolver));
    }

    @Test
    public void getErrorAttributes() {
        ServerWebExchange webExchange =
                MockServerWebExchange.from(MockServerHttpRequest
                        .post("http://localhost:8080/favicon.ico"));
        MockServerRequest serverRequest = MockServerRequest.builder()
                .exchange(webExchange)
                .attribute("org.springframework.boot.web.reactive.error.DefaultErrorAttributes.ERROR", new NullPointerException("errorMessage"))
                .build();

        Map<String, Object> response = globalErrorHandler.getErrorAttributes(serverRequest, false);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.get("code"), (long) HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assert.assertEquals(response.get("message"), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        Assert.assertEquals(response.get("data"), "errorMessage");
    }

    @Test
    public void getRoutingFunction() {
        final ErrorAttributes errorAttributes = mock(DefaultErrorAttributes.class);
        RouterFunction<ServerResponse> routerFunction = globalErrorHandler.getRoutingFunction(errorAttributes);
        Assert.assertNotNull(routerFunction);
    }

    @Test
    public void getHttpStatus() {
        final Map<String, Object> errorAttributes = new LinkedHashMap<>();
        int status = globalErrorHandler.getHttpStatus(errorAttributes);
        Assert.assertEquals(status, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
