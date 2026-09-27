package com.learning.stock.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.stock.exception.DuplicateProductNameException;
import com.learning.stock.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        productRequest = new ProductRequest();
        productRequest.setName("Laptop");
        productRequest.setSku("LAP101");
        productRequest.setDescription("High-performance laptop");
        productRequest.setPrice(BigDecimal.valueOf(1200.00));
        productRequest.setQuantity(10);

        productResponse = new ProductResponse(1L, "Laptop", "LAP101", "High-performance laptop", BigDecimal.valueOf(1200.00), 10);
    }

    @Test
    void createProduct_shouldReturnCreatedProduct_whenValidRequest() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProduct_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        productRequest.setName(""); // Invalid name
        
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Le nom du produit est obligatoire"));

        verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProduct_shouldReturnConflict_whenDuplicateProductName() throws Exception {
        when(productService.createProduct(any(ProductRequest.class)))
                .thenThrow(new DuplicateProductNameException("Un produit avec le nom 'Laptop' existe déjà."));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Un produit avec le nom 'Laptop' existe déjà."));

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    void getAllProducts_shouldReturnListOfProducts() throws Exception {
        ProductResponse product2 = new ProductResponse(2L, "Mouse", "MOU202", "Wireless mouse", BigDecimal.valueOf(25.00), 50);
        List<ProductResponse> products = Arrays.asList(productResponse, product2);

        when(productService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Mouse"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getAllProducts_shouldReturnEmptyList_whenNoProductsExist() throws Exception {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() throws Exception {
        when(productService.getProductById(1L)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop"));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    void getProductById_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        when(productService.getProductById(99L))
                .thenThrow(new ProductNotFoundException("Produit non trouvé avec l'ID: 99"));

        mockMvc.perform(get("/api/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produit non trouvé avec l'ID: 99"));

        verify(productService, times(1)).getProductById(99L);
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct_whenValidRequest() throws Exception {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Laptop Pro");
        updateRequest.setSku("LAP101-PRO");
        updateRequest.setDescription("Upgraded laptop");
        updateRequest.setPrice(BigDecimal.valueOf(1500.00));
        updateRequest.setQuantity(8);

        ProductResponse updatedResponse = new ProductResponse(1L, "Laptop Pro", "LAP101-PRO", "Upgraded laptop", BigDecimal.valueOf(1500.00), 8);

        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Laptop Pro"));

        verify(productService, times(1)).updateProduct(anyLong(), any(ProductRequest.class));
    }

    @Test
    void updateProduct_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName(""); // Invalid name
        updateRequest.setSku("LAP101-PRO");
        updateRequest.setDescription("Upgraded laptop");
        updateRequest.setPrice(BigDecimal.valueOf(1500.00));
        updateRequest.setQuantity(8);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Le nom du produit est obligatoire"));

        verify(productService, never()).updateProduct(anyLong(), any(ProductRequest.class));
    }

    @Test
    void updateProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new ProductNotFoundException("Produit non trouvé avec l'ID: 99"));

        mockMvc.perform(put("/api/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produit non trouvé avec l'ID: 99"));

        verify(productService, times(1)).updateProduct(anyLong(), any(ProductRequest.class));
    }

    @Test
    void updateProduct_shouldReturnConflict_whenDuplicateProductName() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new DuplicateProductNameException("Un produit avec le nom 'Existing Product' existe déjà."));

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Un produit avec le nom 'Existing Product' existe déjà."));

        verify(productService, times(1)).updateProduct(anyLong(), any(ProductRequest.class));
    }

    @Test
    void deleteProduct_shouldReturnNoContent_whenProductDeletedSuccessfully() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void deleteProduct_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        doThrow(new ProductNotFoundException("Produit non trouvé avec l'ID: 99"))
                .when(productService).deleteProduct(99L);

        mockMvc.perform(delete("/api/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Produit non trouvé avec l'ID: 99"));

        verify(productService, times(1)).deleteProduct(99L);
    }
}
