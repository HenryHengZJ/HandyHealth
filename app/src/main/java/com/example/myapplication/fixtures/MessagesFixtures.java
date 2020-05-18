package com.example.myapplication.fixtures;

import com.example.myapplication.model.Message;
import com.example.myapplication.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by troy379 on 12.12.16.
 */
public final class MessagesFixtures extends FixturesData {
    private MessagesFixtures() {
        throw new AssertionError();
    }

    public static Message getImageMessage(Boolean isBot, String imgurl) {
        Message message = new Message(getRandomId(), getUser(isBot), null);
        message.setImage(new Message.Image(imgurl));
        return message;
    }

    public static Message getLoadingImageMessage(String id, Boolean isBot, String imgurl) {
        Message message = new Message(id, getUser(isBot), null);
        message.setImage(new Message.Image(imgurl));
        return message;
    }

    public static Message getReportMessage(String reportId, Boolean isBot, String text) {
        return new Message(reportId, getUser(isBot), text);
    }

    public static Message getVoiceMessage(Boolean isBot) {
        Message message = new Message(getRandomId(), getUser(isBot), null);
        message.setVoice(new Message.Voice("http://example.com", rnd.nextInt(200) + 30));
        return message;
    }

    public static Message getTextMessage() {
        return getTextMessage(getRandomMessage(), true);
    }

    public static Message getTextMessage(String text, Boolean isBot) {
        return new Message(getRandomId(), getUser(isBot), text);
    }

    public static ArrayList<Message> getMessages(Date startDate) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10/*days count*/; i++) {
            int countPerDay = rnd.nextInt(5) + 1;

            for (int j = 0; j < countPerDay; j++) {
                Message message;
                if (i % 2 == 0 && j % 3 == 0) {
                    message = getImageMessage(false, "");
                } else {
                    message = getTextMessage();
                }

                Calendar calendar = Calendar.getInstance();
                if (startDate != null) calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

                message.setCreatedAt(calendar.getTime());
                messages.add(message);
            }
        }
        return messages;
    }

    private static User getUser(Boolean isBot) {
        return new User(
                !isBot ? "0" : "1",
                !isBot ? names.get(0) : names.get(1),
                !isBot ? avatars.get(0) : avatars.get(1),
                true);
    }
}
