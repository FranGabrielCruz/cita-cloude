package com.citacloud.app.config;

import com.citacloud.app.dto.ApiErrorResponse;
import com.citacloud.app.exceptions.BusinessRuleException;
import com.citacloud.app.exceptions.ConflictException;
import com.citacloud.app.exceptions.ForbiddenException;
import com.citacloud.app.exceptions.NotFoundException;
import com.citacloud.app.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void errorsControlledUseExpectedStatusAndSafeContract() {
        assertControlled(new UnauthorizedException(), HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        assertControlled(new ForbiddenException(), HttpStatus.FORBIDDEN, "FORBIDDEN");
        assertControlled(new NotFoundException("SERVICE_NOT_FOUND", "El servicio solicitado no existe."), HttpStatus.NOT_FOUND, "SERVICE_NOT_FOUND");
        assertControlled(new ConflictException("PAYMENT_ALREADY_CANCELLED", "Este pago ya se encuentra anulado."), HttpStatus.CONFLICT, "PAYMENT_ALREADY_CANCELLED");
        assertControlled(new BusinessRuleException("INVALID_AMOUNT", "El monto no es válido."), HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_AMOUNT");
    }

    @Test
    void unexpectedErrorsDoNotExposeTechnicalMessage() {
        MockHttpServletRequest request = request();
        var response = handler.unexpected(new IllegalStateException("password=secret and SQL details"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().error().code()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().error().message()).doesNotContain("secret");
        assertThat(response.getBody().requestId()).isEqualTo("req_test_12345678");
    }

    private void assertControlled(RuntimeException exception, HttpStatus status, String code) {
        var response = handler.application((com.citacloud.app.exceptions.ApplicationException) exception, request());
        ApiErrorResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(body.success()).isFalse();
        assertThat(body.error().code()).isEqualTo(code);
        assertThat(body.requestId()).isEqualTo("req_test_12345678");
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.setAttribute(RequestIdFilter.ATTRIBUTE, "req_test_12345678");
        return request;
    }
}
