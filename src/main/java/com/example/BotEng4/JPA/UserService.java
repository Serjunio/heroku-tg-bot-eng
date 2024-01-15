package com.example.BotEng4.JPA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class UserService
{
    @Autowired
    private UserRepo repo;

    public void save(UserEntity entity)
    {
        repo.save(entity);
    }

    public boolean existsByChatId(Long chatId)
    {
        return repo.existsByChatId(chatId);
    }

    public UserEntity inTable (Long chatId)
    {
        return repo.findByChatId(chatId);
    }

}
