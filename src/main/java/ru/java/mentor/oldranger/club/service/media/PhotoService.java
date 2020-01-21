package ru.java.mentor.oldranger.club.service.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import ru.java.mentor.oldranger.club.dto.CommentDto;
import ru.java.mentor.oldranger.club.dto.PhotoCommentDto;
import ru.java.mentor.oldranger.club.model.comment.PhotoComment;
import ru.java.mentor.oldranger.club.model.forum.Topic;
import ru.java.mentor.oldranger.club.model.media.Photo;
import ru.java.mentor.oldranger.club.model.media.PhotoAlbum;
import ru.java.mentor.oldranger.club.model.user.UserStatistic;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhotoService {
    Photo save(PhotoAlbum album, MultipartFile file);

    Photo findById(Long id);

    void deletePhoto(Long id);

    void deletePhotoByName(String name);

    Photo update(Photo photo);

    List<Photo> findOldPhoto(PhotoAlbum album);

    List<Photo> findPhotoByAlbum(PhotoAlbum album);

    PhotoComment getCommentById(Long id);

    PhotoCommentDto conversionCommentToDto(PhotoComment photoComment);

    public void deleteComment(long id);

    public void addCommentToPhoto(PhotoComment photoComment);

    public void updatePhotoComment(PhotoComment photoComment);

    Page<PhotoCommentDto> getPageableCommentDtoByPhoto(Photo photo, Pageable pageable, int position);

    PhotoCommentDto assembleCommentDto(PhotoComment comment);
}
