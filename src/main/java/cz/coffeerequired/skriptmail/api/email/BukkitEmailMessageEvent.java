package cz.coffeerequired.skriptmail.api.email;

import cz.coffeerequired.skriptmail.api.WillUsed;
import jakarta.mail.Address;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class BukkitEmailMessageEvent extends Event {
    private final String id;
    private ReceivedEmail receivedEmail;

    public BukkitEmailMessageEvent(String id, boolean isAsync) {
        super(isAsync);
        this.id = id;
    }

    public String id() {
        return id;
    }

    @WillUsed
    public ReceivedEmail getLastReceived() {return this.receivedEmail;}

    public void callEventWithData(String subject, Date rec, Object content, Address[] recipients, Address[] from) {
        this.receivedEmail = new ReceivedEmail(subject, rec, content, recipients, from);
        this.callEvent();
    }
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
