package cz.coffeerequired.skriptmail.skript.effects

import ch.njol.skript.Skript
import ch.njol.skript.doc.Examples
import ch.njol.skript.doc.Name
import ch.njol.skript.doc.Since
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import cz.coffeerequired.skriptmail.api.email.BukkitEmailMessageEvent
import cz.coffeerequired.skriptmail.api.email.Email
import cz.coffeerequired.skriptmail.api.isHTML
import jakarta.mail.*
import jakarta.mail.Flags.Flag
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.*
import org.bukkit.event.Event

@Name("Mark email as read/unread")
@Examples(
    """
       on email receive:
            mark event-email as read
    """)
@Since("1.1")
class EvtEmailReceivedEffMark : Effect() {

    companion object {init {
        Skript.registerEffect(EvtEmailReceivedEffMark::class.java,  "mark %emailmessage% as (:read|unread)")
    }}

    private var read: Boolean = false
    private lateinit var exprMessage: Expression<Message>
    override fun toString(event: Event?, debug: Boolean): String = "mark ${exprMessage.toString(event, debug)} as (:read|unread)"

    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?
    ): Boolean {
        if (!parser.isCurrentEvent(BukkitEmailMessageEvent::class.java)) {
            Skript.error("The marking effect of email message can be used only within 'On receive event'")
            return false
        } else {
            read = parseResult!!.hasTag("read")
            exprMessage = expressions!![0] as Expression<Message>
        }
        return true
    }

    override fun execute(event: Event?) {
        val msg = exprMessage.getSingle(event)
        msg?.folder?.setFlags(arrayOf(msg), Flags(if(read) Flag.SEEN else Flag.RECENT), true)
    }
}

@Name("Delete email message")
@Examples(
    """
       on email receive:
            delete event-email
    """)
@Since("1.1")
class EvtEmailReceivedEffDelete : Effect() {

    companion object {init {
        Skript.registerEffect(EvtEmailReceivedEffDelete::class.java,  "delete %emailmessage%")
    }}

    private lateinit var exprMessage: Expression<Message>
    override fun toString(event: Event?, debug: Boolean): String = "delete ${exprMessage.toString(event, debug)}"

    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?
    ): Boolean {
        if (!parser.isCurrentEvent(BukkitEmailMessageEvent::class.java)) {
            Skript.error("The marking effect of email message can be used only within 'On receive event'")
            return false
        } else {
            exprMessage = expressions!![0] as Expression<Message>
        }
        return true
    }

    override fun execute(event: Event?) {
        val msg = exprMessage.getSingle(event)
        msg?.folder?.setFlags(arrayOf(msg), Flags(Flag.DELETED), true)
        msg?.folder?.expunge()
    }
}

@Name("Answer with email")
@Examples(
    """
       on email receive:
            set {_email} to new email using {_account}
            set body of {_email} to "<h1>Hello</h1>"
            answer with {_email} ot event-email
    """)
@Since("1.1")
class EvtEmailReceivedEffAnswer : Effect() {

    companion object {init {
        Skript.registerEffect(EvtEmailReceivedEffAnswer::class.java,  "answer with %email% to %emailmessage%")
    }}

    private lateinit var exprMessage: Expression<Message>
    private lateinit var exprEmail: Expression<Email>
    override fun toString(event: Event?, debug: Boolean): String = "answer with ${exprEmail.toString(event, debug)} to${exprMessage.toString(event, debug)}"

    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?
    ): Boolean {
        if (!parser.isCurrentEvent(BukkitEmailMessageEvent::class.java)) {
            Skript.error("The marking effect of email message can be used only within 'On receive event'")
            return false
        } else {
            exprMessage = expressions!![1] as Expression<Message>
            exprEmail = expressions[0] as Expression<Email>
        }
        return true
    }

    private fun answer(email: Email, msg: Message) {
        val replyMessage = MimeMessage(msg.session)
        replyMessage.subject = "Re: ${msg.subject}"
        replyMessage.setFrom(msg.allRecipients[0])
        replyMessage.setRecipients(Message.RecipientType.TO, msg.from)

        val multipart = MimeMultipart()
        val bodyPart = MimeBodyPart()
        if (email.content?.isHTML() == true) bodyPart.setContent(email.content, "text/html; charset=UTF-8") else bodyPart.setContent(email.content, "text/plain; charset=UTF-8")
        multipart.addBodyPart(bodyPart)
        replyMessage.setContent(multipart)

        val tt = msg.session.transport
        tt.connect()
        tt.sendMessage(replyMessage, replyMessage.allRecipients)
        tt.close()
    }

    override fun execute(event: Event?) {
        val msg = exprMessage.getSingle(event)
        val email = exprEmail.getSingle(event)
        if (msg != null && email != null) {
            msg.folder.setFlags(arrayOf(msg), Flags(Flag.ANSWERED), true)
            answer(email, msg)
        }
    }
}

@Name("Move email from folder to folder")
@Examples(
    """
       on email receive:
            move event-email to "Something"
    """)
@Since("1.1")
class EvtEmailReceivedEffMove : Effect() {

    companion object {init {
        Skript.registerEffect(EvtEmailReceivedEffMove::class.java,  "move %emailmessage% to %string%")
    }}

    private lateinit var exprMessage: Expression<Message>
    private lateinit var exprFolder: Expression<String>
    override fun toString(event: Event?, debug: Boolean): String = "move ${exprMessage.toString(event, debug)} to ${exprFolder.toString(event, debug)}"

    @Suppress("UNCHECKED_CAST")
    override fun init(expressions: Array<out Expression<*>>?, matchedPattern: Int, isDelayed: Kleenean?, parseResult: SkriptParser.ParseResult?
    ): Boolean {
        if (!parser.isCurrentEvent(BukkitEmailMessageEvent::class.java)) {
            Skript.error("The marking effect of email message can be used only within 'On receive event'")
            return false
        } else {
            exprMessage = expressions!![0] as Expression<Message>
            exprFolder = expressions[1] as Expression<String>
        }
        return true
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun move(potentialFolder: String, msg: Message, store: Store) {
        GlobalScope.launch {
            async {
                val result = runCatching {
                    val destFolder = store.getFolder(potentialFolder)
                    val sourceFolder = msg.folder
                    if (destFolder.exists()) {
                        if (!sourceFolder.isOpen) sourceFolder.open(2)
                        if (!destFolder.isOpen) destFolder.open(2)
                        sourceFolder.copyMessages(arrayOf(msg), destFolder)
                        msg.folder.setFlags(arrayOf(msg), Flags(Flag.DELETED), true)
                        sourceFolder.expunge()
                        destFolder.close()
                    } else {
                        if (PROJECT_DEBUG) SkriptMail.logger().debug("Folder for name $potentialFolder does not exist, $0 $destFolder, $1 $msg")
                        SkriptMail.logger().warn("Folder for name $potentialFolder does not exist")
                    }
                }
                result.onFailure {
                    if (PROJECT_DEBUG) SkriptMail.logger().debug("$it")
                    SkriptMail.logger().exception(it as Exception, "Message cannot be moved!")
                }
            }.join()
        }
    }


    override fun execute(event: Event?) {
        val msg = exprMessage.getSingle(event)
        val potentialFolder = exprFolder.getSingle(event)

        if (msg != null && potentialFolder != null) {
            msg.folder.setFlags(arrayOf(msg), Flags(Flag.RECENT), true)
            runBlocking {
                async {
                    val store = msg.session.getStore("imap")
                    store.connect()
                    move(potentialFolder, msg, store)
                }.join()
            }

        }
    }
}