package ru.java.mentor.oldranger.club.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.java.mentor.oldranger.club.service.mail.MailService;

@Controller
public class TestMailController {

    @Autowired
    MailService mailService;

    // Пример работы с отправлением почты
    @GetMapping("/testmail")
    public String sendTestMail() {
        mailService.send("daref67649@3dmail.top","test","Lorem ipsum dolor sit amet");
        return "testmail";
    }
}