package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.entities.ProductImage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadProductImageResponse {
    @JsonProperty("message")
    private String message;
    @JsonProperty("productImages")
    private List<ProductImage> productImages;
}
