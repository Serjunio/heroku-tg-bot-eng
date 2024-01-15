package com.example.BotEng4.service;

import com.example.BotEng4.JPA.*;
import com.example.BotEng4.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private LessonRepo lessonRepo;
    final BotConfig config;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    boolean flag = true;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listCommand = new ArrayList<>();
        listCommand.add(new BotCommand("/start", "запускает бота"));
        listCommand.add(new BotCommand("/lesson", "узнать своё расписание"));
        listCommand.add(new BotCommand("/change", "поменять расписание"));

        try {
            this.execute(new SetMyCommands(listCommand, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("Error settings bots command");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    startCommandAdd(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/lesson":
                    startCommandLesson(chatId);
                    break;
                case "/change":
                    startCommandChange(chatId);
                    break;
                default:
                    sendMessage(chatId, "чтобы добавить вас в базу данных пропишите /start");
            }
            scheduler.scheduleAtFixedRate(() -> {
                // Вызываем метод sendRemind для каждого пользователя
                List<UserEntity> users = (List<UserEntity>) userRepo.findAll();
                for (UserEntity user : users) {
                    sendRemind(user.getChatId());
                    System.out.println(user);
                }
            }, 0, 1, TimeUnit.MINUTES); // Запускать каждую минуту
        }
    }

    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi" + " " + name;
        sendMessage(chatId, answer);
    }

    private void startCommandAdd(Message message) {
        if (userRepo.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            UserEntity user = new UserEntity();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));


            userRepo.save(user);
        }
        if (lessonRepo.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            LessonEntity lesson = new LessonEntity();

            lesson.setChatId(chatId);

            lessonRepo.save(lesson);
        }
    }

    private void startCommandLesson(long chatId) {
        var lesson = lessonRepo.findById(chatId);
        String allLessons = "";
        if (lesson.isPresent()) {
            allLessons = "Расписание Занятий \n" + "Понедельник - " + lesson.get().getMn() + "\n" + "Вторник - "
                    + lesson.get().getTu() + "\n" + "Среда - " + lesson.get().getWe() + "\n" + "Четверг - " +
                    lesson.get().getTh() + "\n" + "Пятница - " + lesson.get().getFr() + "\n" + "Суббота - "
                    + lesson.get().getSt() + "\n" + "Воскресенье - " + lesson.get().getSn();
        }
        sendMessage(chatId, allLessons);
    }

    private void startCommandChange(long chatId) {
        sendMessage(chatId, "Для изменения времени расписания обратитесь к преподавателю: @aobaninaaa");
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void sendRemind(long chatId) {
        List<LocalDateTime> allLessons = new ArrayList<>();
        var lesson = lessonRepo.findById(chatId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        try {

            LocalTime timeMn = lesson.get().getMn().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getMn(), formatter);
            LocalTime timeTu = lesson.get().getTu().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getTu(), formatter);
            LocalTime timeWe = lesson.get().getWe().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getWe(), formatter);
            LocalTime timeTh = lesson.get().getTh().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getTh(), formatter);
            LocalTime timeFr = lesson.get().getFr().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getFr(), formatter);
            LocalTime timeSt = lesson.get().getSt().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getSt(), formatter);
            LocalTime timeSn = lesson.get().getSn().equals("отсутствует") ? null : LocalTime.parse(lesson.get().getSn(), formatter);

            // создаем LocalDateTime только если время не равно null
            if (timeMn != null) {
                LocalDateTime lessonStartAtMn = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeMn);
                allLessons.add(lessonStartAtMn);
            }
            if (timeTu != null) {
                LocalDateTime lessonStartAtTu = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeTu);
                allLessons.add(lessonStartAtTu);
            }
            if (timeWe != null) {
                LocalDateTime lessonStartAtWe = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeWe);
                allLessons.add(lessonStartAtWe);
            }
            if (timeTh != null) {
                LocalDateTime lessonStartAtTh = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeTh);
                allLessons.add(lessonStartAtTh);
            }
            if (timeFr != null) {
                LocalDateTime lessonStartAtFr = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeFr);
                allLessons.add(lessonStartAtFr);
            }
            if (timeSt != null) {
                LocalDateTime lessonStartAtSt = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeSt);
                allLessons.add(lessonStartAtSt);
            }
            if (timeSn != null) {
                LocalDateTime lessonStartAtSn = LocalDateTime.of(LocalDateTime.now().toLocalDate(), timeSn);
                allLessons.add(lessonStartAtSn);
            }

            for (var lessons : allLessons) {
                switch (lessons.getDayOfWeek()) {
                    case MONDAY:
                        if (flag) {
                            if (!lesson.get().getMn().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    //sendMessage(chatId, String.valueOf((Duration.between(LocalDateTime.now(), lessonStartAtMn).toMinutes())));
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case TUESDAY:
                        if (flag) {
                            if (!lesson.get().getTu().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case WEDNESDAY:
                        if (flag) {
                            if (!lesson.get().getWe().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case THURSDAY:
                        if (flag) {
                            if (!lesson.get().getTh().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case FRIDAY:
                        if (flag) {
                            if (!lesson.get().getFr().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case SATURDAY:
                        if (flag) {
                            if (!lesson.get().getSt().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                    case SUNDAY:
                        if (flag) {
                            if (!lesson.get().getSn().equals("отсутствует")) {
                                if (Duration.between(LocalDateTime.now(), lessons).toMinutes() == 29) {
                                    sendMessage(chatId, "Занятие начнётся через 30 минут");
                                    flag = false;
                                }
                            }
                        }
                        break;
                }
            }
        }
        catch (DateTimeParseException | ArithmeticException e)
        {
            e.printStackTrace();
        }
    }
}