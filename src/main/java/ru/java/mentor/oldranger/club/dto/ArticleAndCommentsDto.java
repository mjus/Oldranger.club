package ru.java.mentor.oldranger.club.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import ru.java.mentor.oldranger.club.model.article.Article;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAndCommentsDto {

    private Article article;
    private Page<ArticleCommentDto> articleCommentDto;
}
