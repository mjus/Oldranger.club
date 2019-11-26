package ru.java.mentor.oldranger.club.service.media.impl;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.java.mentor.oldranger.club.dao.UserRepository.media.PhotoAlbumRepository;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.model.user.media.Media;
import ru.java.mentor.oldranger.club.model.user.media.Photo;
import ru.java.mentor.oldranger.club.model.user.media.PhotoAlbum;
import ru.java.mentor.oldranger.club.service.media.PhotoAlbumService;
import ru.java.mentor.oldranger.club.service.user.UserService;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoAlbumServiceImpl implements PhotoAlbumService {
    @Value("${photoalbums.location}")
    private String albumsdDir;

    @Value("${upload.medium}")
    private int medium;
    @Value("${upload.small}")
    private int small;

    private PhotoAlbumRepository albumRepository;

    private UserService userService;

    @Autowired
    public void setUserService(UserService service) {
        this.userService = service;
    }

    @Autowired
    public PhotoAlbumServiceImpl(PhotoAlbumRepository repository) {
        this.albumRepository = repository;
    }

    public PhotoAlbumServiceImpl() {
        super();
    }

    @Override
    public void createAlbumDir(String albumId) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        File uploadPath = new File(albumsdDir + File.separator + userName + File.separator + "photo_albums" + File.separator + albumId);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
    }

    @Override
    public PhotoAlbum save(PhotoAlbum album) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByNickName(userName);
        Media media = user.getMedia();
        media.getPhotoAlbums().add(album);
        PhotoAlbum savedAlbum = albumRepository.save(album);
        createAlbumDir(savedAlbum.getId() + "");
        return savedAlbum;
    }

    @Override
    public List<PhotoAlbum> findAll() {
        return albumRepository.findAll();
    }

    @Override
    @PostConstruct
    public void deleteAllAlbums() {
        File dir = new File(albumsdDir);
        FileSystemUtils.deleteRecursively(dir);
    }

    @Override
    public void deleteAlbumDir(String id) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        File dir = new File(albumsdDir + File.separator + userName
                + File.separator + "photo_albums" + File.separator + id);
        FileSystemUtils.deleteRecursively(dir);
    }

    @Override
    public void deleteAlbum(String id) {
        albumRepository.deleteById(Long.parseLong(id));
        deleteAlbumDir(id);
    }

    @Override
    public PhotoAlbum findByTitle(String title) {
        return null;
    }

    @Override
    public PhotoAlbum findById(Long id) {
        return albumRepository.findById(id).get();
    }

    @Override
    public Photo addPhotoToDir(MultipartFile file, PhotoAlbum album) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String resultFileName = UUID.randomUUID().toString() + StringUtils.cleanPath(file.getOriginalFilename());
        File uploadPath = new File(albumsdDir + File.separator + userName
                + File.separator + "photo_albums" + File.separator + album.getId() + File.separator + resultFileName);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        Path copyLocation = Paths.get(uploadPath + File.separator + resultFileName);
        try {
            Files.copy(file.getInputStream(), copyLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thumbnails.of(uploadPath + File.separator + resultFileName)
                    .size(medium, medium)
                    .toFile(uploadPath + File.separator + "medium_" + resultFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Photo photo = new Photo(resultFileName, "medium_" + resultFileName);
        return photo;
    }

    @Override
    public PhotoAlbum update(PhotoAlbum album) {
        return albumRepository.save(album);
    }
}
