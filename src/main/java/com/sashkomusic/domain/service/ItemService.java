package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Item;
import com.sashkomusic.domain.model.tag.Tag;
import com.sashkomusic.domain.model.Track;
import com.sashkomusic.domain.repository.ItemRepository;
import com.sashkomusic.web.dto.ItemDto;
import com.sashkomusic.web.dto.create.ItemCreateDto;
import com.sashkomusic.web.dto.create.TrackCreateDto;
import com.sashkomusic.web.exception.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ItemService {
    private final ArtistService artistService;
    private final TagService tagService;
    private final S3Service s3Service;
    private final ItemRepository itemRepository;

    public List<ItemDto> findAll() {
        return itemRepository.findAll().stream().map(ItemDto::of).toList();
    }

    public ItemDto findById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new EntityNotFoundException("No item with id %s: ".formatted(id));
        }
        return ItemDto.of(item.get());
    }

    public List<ItemDto> findRelevantByTags(Set<String> tags) {
        return itemRepository.findByTags(tags).stream().map(ItemDto::of).toList();
    }

    public ItemDto create(ItemCreateDto itemCreateDto, List<MultipartFile> images) {
        List<String> artists = itemCreateDto.artists().stream()
                .map(artistService::create).toList();

        Set<Tag> tags = createTags(itemCreateDto);

        Item item = Item.builder()
                .title(itemCreateDto.title())
                .releaseYear(itemCreateDto.releaseYear())
                .format(itemCreateDto.format())
                .artists(artists)
                .images(Collections.emptyList())
                .tags(tags).build();
        item.setTracks(createTracks(itemCreateDto.tracks()));
        addImages(item, images);

        itemRepository.save(item);
        return ItemDto.of(item);
    }

    private Set<Tag> createTags(ItemCreateDto itemCreateDto) {
        Set<Tag> tags = itemCreateDto.tags().stream()
                .map(tagService::create).collect(Collectors.toSet());
        Set<Tag> suggestedTags = tagService.askTags(itemCreateDto);

        tags.addAll(suggestedTags);
        return suggestedTags;
    }

    private List<Track> createTracks(List<TrackCreateDto> tracksDto) {
        List<Track> tracks = new ArrayList<>();

        int position = 1;
        for (TrackCreateDto trackDto : tracksDto) {
            List<String> artists = new ArrayList<>();
            if (trackDto.artists() != null) {
                artists = trackDto.artists().stream()
                        .map(artistService::create).toList();
            }

            tracks.add(new Track(position, trackDto.name(), null, artists));
            position++;
        }
        return tracks;
    }

    private void addImages(Item item, List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String url = s3Service.uploadFile(image);
            imageUrls.add(url);
        }
        item.setImages(imageUrls);
    }
}
