package com.sashkomusic.web;

import com.sashkomusic.domain.service.ItemService;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import com.sashkomusic.web.dto.ItemDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemDto>> findAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemDto> create(@RequestPart("json") ItemCreateDto itemDto,
                                          @RequestPart("images") List<MultipartFile> images) {

        return ResponseEntity.ok(itemService.create(itemDto, images));
    }
}
