package cn.wolfcode.ws;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/{token}")
public class OrderWsListener {
    public static ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap<>();
    @OnOpen
    public void onOpen(@PathParam("token") String token,Session session){
        System.out.println("建立会话");
        clients.put(token,session);
    }
    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }
    @OnClose
    public void onClose(@PathParam("token") String token){
        System.out.println("会话关闭");
        clients.remove(token);
    }
}
