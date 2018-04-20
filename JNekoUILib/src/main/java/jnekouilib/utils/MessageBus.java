package jnekouilib.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*******************************************************************************************************************
 * 
 *   АХТУНГ !!!
 * 
 *   Это экспериментальный класс. И это костыль.
 *   Если кто-то захочет использовать его для учебы или своего проекта, знайте: так делать не надо =) 
 * 
 *   Подобный механизм тут нужен для связывания разных классов, дабы не тащить в них сотню разных слушателей,
 *   что облегчает кодинг. Однако это сильно снижает читаемость кода самой библиотеки. 
 *   Хочу впихнуть кодинг гуя в минимальное количество строк. На костыли пофиг, в продакшн не пойдет.
 * 
 *******************************************************************************************************************/

public class MessageBus {
    private static final Map<MessageBusActions, Set<MessageBusAction>>
            messages = new HashMap<>();
    
    public static final void sendMessage(MessageBusActions messageID) {
        sendMessage(messageID, new Object());
    }
    
    public static final void sendMessage(MessageBusActions messageID, Object ... objects) {
        if (!messages.containsKey(messageID)) return;
        messages.get(messageID).forEach(action -> {
            action.OnMessage(messageID, objects); 
        });
    }
            
    public static final void registerMessageReceiver(MessageBusActions messageID, MessageBusAction action) {
        if (!messages.containsKey(messageID)) 
            messages.put(messageID, new HashSet<>());
        messages.get(messageID).add(action);
    }
    
    public static final void unregisterMessageReceiver(MessageBusActions messageID) {
        if (messages.containsKey(messageID))
            messages.remove(messageID);
    }
}
