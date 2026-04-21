package com.opsflow.opsflow_backend.api.catalog;

import com.opsflow.opsflow_backend.application.catalog.CatalogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class CatalogControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CatalogService catalogService;

    @Test
    void getByCategory_shouldReturn200() throws Exception {
        when(catalogService.getByCategory("hardware"))
                .thenReturn(List.of(
                        new CatalogResponseDto(1L, "A1", "hardware", "Portatil", true)
                ));

        mvc.perform(get("/catalogs/hardware"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("A1"))
                .andExpect(jsonPath("$[0].category").value("hardware"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(catalogService.create(new CreateCatalogDto("A1", "hardware", "Portatil", true)))
                .thenReturn(new CatalogResponseDto(1L, "A1", "hardware", "Portatil", true));

        mvc.perform(post("/catalogs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "A1",
                                  "category": "hardware",
                                  "description": "Portatil",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/catalogs/hardware"))
                .andExpect(jsonPath("$.code").value("A1"));
    }
}