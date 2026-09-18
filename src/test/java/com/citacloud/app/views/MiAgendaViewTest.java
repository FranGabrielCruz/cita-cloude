package com.citacloud.app.views;

import com.vaadin.flow.component.grid.Grid;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class MiAgendaViewTest {
    @Test
    void agendaGridAdjustsItsHeightToVisibleRows() throws ReflectiveOperationException {
        MiAgendaView view = new MiAgendaView(null, null);

        Field field = MiAgendaView.class.getDeclaredField("grid");
        field.setAccessible(true);
        Grid<?> grid = (Grid<?>) field.get(view);

        assertThat(grid.getElement().getProperty("allRowsVisible")).isEqualTo("true");
    }
}
