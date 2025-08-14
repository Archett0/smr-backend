package com.team12.listingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team12.listingservice.controller.PropertyController;
import com.team12.listingservice.model.Property;
import com.team12.listingservice.model.PropertyDto;
import com.team12.listingservice.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropertyControllerStandaloneTest {

    private MockMvc mockMvc;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private PropertyController propertyController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(propertyController).build();
    }

    @Test
    @DisplayName("GET /listing - 200 + []")
    void getAll() throws Exception {
        when(propertyService.getAllPropertiesWithAgentInfo()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/listing"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(propertyService).getAllPropertiesWithAgentInfo();
    }

    @Nested
    class GetById {
        @Test
        @DisplayName("GET /listing/{id} - found -> 200")
        void found() throws Exception {
            when(propertyService.getPropertyById(1L))
                    .thenReturn(Optional.of(new PropertyDto()));

            mockMvc.perform(get("/listing/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

            verify(propertyService).getPropertyById(1L);
        }

        @Test
        @DisplayName("GET /listing/{id} - not found -> 404")
        void notFound() throws Exception {
            when(propertyService.getPropertyById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/listing/{id}", 999L))
                    .andExpect(status().isNotFound());

            verify(propertyService).getPropertyById(999L);
        }
    }

    @Test
    @DisplayName("POST /listing - 200")
    void create() throws Exception {
        Property body = new Property();
        Property saved = new Property();
        when(propertyService.createProperty(any(Property.class))).thenReturn(saved);

        mockMvc.perform(post("/listing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(propertyService).createProperty(any(Property.class));
    }

    @Test
    @DisplayName("PUT /listing/{id} - found -> 200")
    void update_found() throws Exception {
        Property body = new Property();
        Property updated = new Property();
        when(propertyService.updateProperty(eq(1L), any(Property.class))).thenReturn(updated);

        mockMvc.perform(put("/listing/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(propertyService).updateProperty(eq(1L), any(Property.class));
    }

    @Test
    @DisplayName("PUT /listing/{id} - not found -> 404")
    void update_notFound() throws Exception {
        Property body = new Property();
        when(propertyService.updateProperty(eq(999L), any(Property.class))).thenReturn(null);

        mockMvc.perform(put("/listing/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        verify(propertyService).updateProperty(eq(999L), any(Property.class));
    }

    @Test
    @DisplayName("DELETE /listing/{id} - 204")
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/listing/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(propertyService).deleteProperty(7L);
    }
}
