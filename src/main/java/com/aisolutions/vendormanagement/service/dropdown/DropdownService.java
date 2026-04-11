package com.aisolutions.vendormanagement.service.dropdown;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

import com.aisolutions.vendormanagement.dto.DropdownOptionDTO;
import com.aisolutions.vendormanagement.enums.DropdownType;
import com.aisolutions.vendormanagement.repository.DropdownRepository;

import java.util.List;

@ApplicationScoped
public class DropdownService {
    
    @Inject
    DropdownRepository repository;

    @Inject
    DropdownCacheService cacheService;

    /**
     * Get dropdown options by type
     */
    public Uni<List<DropdownOptionDTO>> getDropdown(DropdownType type) {
        return switch (type) {
            case VENDOR -> repository.findAllVendors();
            default -> throw new IllegalArgumentException("Unknown dropdown type: " + type);
        };
    }

    /**
     * Get multiple dropdowns by type keys
     * @param typeKeys - List of keys: "vendors", etc.
     */
    public Uni<Map<String, List<DropdownOptionDTO>>> getDropdownsByTypeKeys(List<String> typeKeys) {
        // Always use cached version
        return cacheService.getCachedDropdowns()
            .onItem().transform(allDropdowns -> {
                Map<String, List<DropdownOptionDTO>> result = new HashMap<>();
                for (String typeKey : typeKeys) {
                    List<?> data = allDropdowns.get(typeKey);
                    if (data != null) {
                        @SuppressWarnings("unchecked")
                        List<DropdownOptionDTO> typedList = (List<DropdownOptionDTO>) data;
                        result.put(typeKey, typedList);
                    }
                }
                return result;
            });
    }

    public void clearDropdownCache() {
        cacheService.clearCache();
    }
}
