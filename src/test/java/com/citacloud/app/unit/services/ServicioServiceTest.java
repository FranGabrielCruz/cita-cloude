package com.citacloud.app.unit.services;

import com.citacloud.app.models.Servicio;
import com.citacloud.app.repositories.ServicioRepository;
import com.citacloud.app.services.ServicioService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServicioServiceTest {
    private final UUID empresa = UUID.randomUUID();

    @Test
    void createsActiveServiceWithSequentialCode() {
        ServicioService service = new ServicioService(repository());

        Servicio created = service.guardar(empresa, null, null, "Consulta cardiología", "", new BigDecimal("1500"), true);

        assertThat(created.getEmpresaId()).isEqualTo(empresa);
        assertThat(created.getCodigo()).isEqualTo("SER-0001");
        assertThat(created.getNombre()).isEqualTo("Consulta cardiología");
        assertThat(created.getPrecio()).isEqualByComparingTo("1500");
        assertThat(created.getActivo()).isTrue();
    }

    @Test
    void rejectsEmptyNameAndNegativePrice() {
        ServicioService service = new ServicioService(repository());

        assertThatThrownBy(() -> service.guardar(empresa, null, null, " ", "", BigDecimal.ONE, true))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.guardar(empresa, null, null, "Consulta", "", new BigDecimal("-1"), true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ServicioRepository repository() {
        return (ServicioRepository) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ServicioRepository.class}, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "findByEmpresaIdOrderByNombre" -> List.of();
                case "existsByEmpresaIdAndCodigoIgnoreCase" -> false;
                case "save" -> args[0];
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
    }
}
