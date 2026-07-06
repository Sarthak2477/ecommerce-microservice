package com.example.inventory_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.inventory_service.dto.AddInventoryRequest;
import com.example.inventory_service.dto.InventoryResponse;
import com.example.inventory_service.entity.Inventory;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    InventoryResponse toResponse(Inventory inventory);

    @Mapping(target = "id", ignore = true)
    Inventory toEntity(AddInventoryRequest request);
}
