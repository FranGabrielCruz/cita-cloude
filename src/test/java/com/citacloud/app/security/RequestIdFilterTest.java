package com.citacloud.app.security;

import com.citacloud.app.config.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {
    @Test
    void preservesValidRequestIdAndReturnsItInHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "req_valid_12345678");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RequestIdFilter().doFilter(request, response, new MockFilterChain());

        assertThat(request.getAttribute(RequestIdFilter.ATTRIBUTE)).isEqualTo("req_valid_12345678");
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("req_valid_12345678");
    }
}
