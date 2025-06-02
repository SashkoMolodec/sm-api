package com.sashkomusic.domain.service;

import com.sashkomusic.domain.model.Item;
import com.sashkomusic.domain.model.Tag;
import com.sashkomusic.domain.model.Track;
import com.sashkomusic.domain.repository.ItemRepository;
import com.sashkomusic.web.dto.ItemDto;
import com.sashkomusic.web.dto.TagDto;
import com.sashkomusic.web.dto.TrackDto;
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
        List<ItemDto> itemDtos = new ArrayList<>();
        for (Item item : itemRepository.findAll()) {
            itemDtos.add(toDto(item));
        }
        return itemDtos;
    }

    public ItemDto findById(Long id) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isEmpty()) {
            throw new EntityNotFoundException("No item with id %s: ".formatted(id));
        }
        return toDto(item.get());
    }

    public ItemDto create(ItemCreateDto itemCreateDto, List<MultipartFile> images) {
        List<String> artists = itemCreateDto.artists().stream()
                .map(artistService::create).toList();

        Set<Tag> tags = itemCreateDto.tags().stream()
                .map(tagService::create).collect(Collectors.toSet());

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
        return toDto(item);
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

    private ItemDto toDto(Item item) {
        List<TagDto> tags = item.getTags().stream().map(
                tag -> new TagDto(tag.getCategory(), tag.getName(), tag.getShade())).toList();
        List<TrackDto> tracks = item.getTracks().stream().map(
                track -> new TrackDto(track.getPosition(), track.getName(), track.getSourceUrl())).toList();

        return ItemDto.builder()
                .id(item.getId())
                .title(item.getTitle())
                .releaseYear(item.getReleaseYear())
                .format(item.getFormat())
                .artists(item.getArtists())
                .images(item.getImages())
                .tags(tags)
                .tracks(tracks)
                .build();
    }
}
