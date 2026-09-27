package com.learning.stock.product;

import com.learning.stock.exception.DuplicateProductNameException;
import com.learning.stock.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;
    private ProductRequest productRequest1;

    @BeforeEach
    void setUp() {
        product1 = new Product(1L, "Laptop", "LAP101", "High-performance laptop", BigDecimal.valueOf(1200.00), 10);
        product2 = new Product(2L, "Mouse", "MOU202", "Wireless mouse", BigDecimal.valueOf(25.00), 50);
        
        productRequest1 = new ProductRequest();
        productRequest1.setName("Laptop");
        productRequest1.setSku("LAP101");
        productRequest1.setDescription("High-performance laptop");
        productRequest1.setPrice(BigDecimal.valueOf(1200.00));
        productRequest1.setQuantity(10);
    }

    @Test
    void createProduct_shouldCreateNewProductSuccessfully() {
        when(productRepository.findByName(productRequest1.getName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        ProductResponse response = productService.createProduct(productRequest1);

        assertNotNull(response);
        assertEquals(product1.getName(), response.getName());
        verify(productRepository, times(1)).findByName(productRequest1.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowDuplicateProductNameException_whenNameExists() {
        when(productRepository.findByName(productRequest1.getName())).thenReturn(Optional.of(product1));

        DuplicateProductNameException thrown = assertThrows(
                DuplicateProductNameException.class,
                () -> productService.createProduct(productRequest1)
        );

        assertEquals("Un produit avec le nom 'Laptop' existe déjà.", thrown.getMessage());
        verify(productRepository, times(1)).findByName(productRequest1.getName());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProducts_shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<ProductResponse> products = productService.getAllProducts();

        assertNotNull(products);
        assertEquals(2, products.size());
        assertEquals(product1.getName(), products.get(0).getName());
        assertEquals(product2.getName(), products.get(1).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getAllProducts_shouldReturnEmptyList_whenNoProductsExist() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductResponse> products = productService.getAllProducts();

        assertNotNull(products);
        assertTrue(products.isEmpty());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(product1.getId(), response.getId());
        assertEquals(product1.getName(), response.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException thrown = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(99L)
        );

        assertEquals("Produit non trouvé avec l'ID: 99", thrown.getMessage());
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    void updateProduct_shouldUpdateProductSuccessfully_whenNameNotChanged() {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Laptop");
        updateRequest.setSku("LAP101-V2");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(BigDecimal.valueOf(1250.00));
        updateRequest.setQuantity(12);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        assertEquals(updateRequest.getName(), response.getName());
        assertEquals(updateRequest.getSku(), response.getSku());
        assertEquals(updateRequest.getDescription(), response.getDescription());
        assertEquals(updateRequest.getPrice(), response.getPrice());
        assertEquals(updateRequest.getQuantity(), response.getQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).findByName(anyString()); // No need to check name if it's the same
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldUpdateProductSuccessfully_whenNameChangedAndUnique() {
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("New Laptop Name");
        updateRequest.setSku("LAP101-V2");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(BigDecimal.valueOf(1250.00));
        updateRequest.setQuantity(12);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findByName("New Laptop Name")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        ProductResponse response = productService.updateProduct(1L, updateRequest);

        assertNotNull(response);
        assertEquals(updateRequest.getName(), response.getName());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByName("New Laptop Name");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        ProductRequest updateRequest = new ProductRequest();
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ProductNotFoundException thrown = assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(99L, updateRequest)
        );

        assertEquals("Produit non trouvé avec l'ID: 99", thrown.getMessage());
        verify(productRepository, times(1)).findById(99L);
        verify(productRepository, never()).findByName(anyString());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_shouldThrowDuplicateProductNameException_whenNameChangedToExistingName() {
        Product existingProductWithSameName = new Product(3L, "Existing Name", "SKU303", "Desc", BigDecimal.TEN, 5);
        
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setName("Existing Name");
        updateRequest.setSku("LAP101-V2"); // SKU is different, but name is the problem
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(BigDecimal.valueOf(1250.00));
        updateRequest.setQuantity(12);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findByName("Existing Name")).thenReturn(Optional.of(existingProductWithSameName));

        DuplicateProductNameException thrown = assertThrows(
                DuplicateProductNameException.class,
                () -> productService.updateProduct(1L, updateRequest)
        );

        assertEquals("Un produit avec le nom 'Existing Name' existe déjà.", thrown.getMessage());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findByName("Existing Name");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteProductSuccessfully() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));

        verify(productRepository, times(1)).existsById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrowProductNotFoundException_whenProductDoesNotExist() {
        when(productRepository.existsById(99L)).thenReturn(false);

        ProductNotFoundException thrown = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(99L)
        );

        assertEquals("Produit non trouvé avec l'ID: 99", thrown.getMessage());
        verify(productRepository, times(1)).existsById(99L);
        verify(productRepository, never()).deleteById(anyLong());
    }
}
