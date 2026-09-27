package com.learning.stock.product;

import com.learning.stock.exception.DuplicateProductNameException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findByName(request.getName()).isPresent()) {
            throw new DuplicateProductNameException("Un produit avec le nom '" + request.getName() + "' existe déjà.");
        }

        Product product = new Product(
                request.getName(),
                request.getSku(),
                request.getDescription(),
                request.getPrice(),
                request.getQuantity()
        );

        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        );
    }
}
