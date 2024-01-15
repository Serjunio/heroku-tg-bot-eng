package com.example.BotEng4.JPA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonService {

    @Autowired
    private LessonRepo lessonRepo;

    public void save(LessonEntity entity)
    {
        lessonRepo.save(entity);
    }

    public boolean existsByChatId(Long chatId)
    {
        return lessonRepo.existsByChatId(chatId);
    }

    public LessonEntity inTable (Long chatId)
    {
        return lessonRepo.findByChatId(chatId);
    }
}
