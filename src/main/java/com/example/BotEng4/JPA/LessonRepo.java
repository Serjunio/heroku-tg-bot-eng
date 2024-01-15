package com.example.BotEng4.JPA;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepo extends CrudRepository<LessonEntity, Long> {

    boolean existsByChatId(Long chatId);
    LessonEntity findByChatId(Long chatId);
}
