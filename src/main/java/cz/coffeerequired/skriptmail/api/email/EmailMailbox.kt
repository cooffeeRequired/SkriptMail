package cz.coffeerequired.skriptmail.api.email

import cz.coffeerequired.skriptmail.SkriptMail
import cz.coffeerequired.skriptmail.api.ConfigFields
import cz.coffeerequired.skriptmail.api.ConfigFields.EMAIL_DEBUG
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_BATCH_PER_REQUEST
import cz.coffeerequired.skriptmail.api.ConfigFields.MAILBOX_RATE_UNIT
import cz.coffeerequired.skriptmail.api.ConfigFields.PROJECT_DEBUG
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Store
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.time.measureTime

/**
 * A class that represents an email mailbox.
 *
 * @param store the [Store] that is used to access the email server
 */
class EmailMailbox(private val store: Store) {
    /**
     * A list of messages in the mailbox.
     */
    private val messages = mutableListOf<Message?>()

    /**
     * A opened folder from mailbox storage
     */
    lateinit var folder: Folder

    /**
     * A flag indicating whether the mailbox is closed.
     */
    private var isClosed = false

    /**
     * The initial delay for the refresh task.
     */
    private val initialDelay = 0L

    /**
     * The period for the refresh task.
     */
    private val period = ConfigFields.MAILBOX_REFRESH_RATE

    /**
     * The refresh task.
     */
    private var task: Runnable? = null

    /**
     * The listener event
     */
    lateinit var event: BukkitEmailMessageEvent

    /**
     * The scheduler for the refresh task.
     */
    private var scheduler: ScheduledExecutorService? = Executors.newSingleThreadScheduledExecutor()

    companion object {

        /**
         * A map of registered mailboxes, indexed by their IDs.
         */
        private val registeredMailboxes = mutableMapOf<String, EmailMailbox>()

        /**
         * Unregisters the email mailbox for the given ID and store.
         *
         * @param id the ID of the mailbox
         */
        fun unregister(id: String) {
            val mailbox = registeredMailboxes[id]
                ?: throw EmailException("Email storage for $id was not found!", EmailExceptionType.DISCONNECT)
            try {
                mailbox.close()
                if (mailbox.folder.isOpen) throw EmailException("Email folder must be closed before unregistering!", EmailExceptionType.DISCONNECT)
                registeredMailboxes -= id
                if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Unregistering mail for service $id was successful $0 $this")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        /**
         * Registers an email mailbox for the given ID, store, and allowed folders.
         *
         * @param id the ID of the mailbox
         * @param store the store that the mailbox is registered with
         * @param allowedFolders the allowed folders for the mailbox
         * @return the registered email mailbox
         */
        fun register(id: String, store: Store, @Suppress("UNUSED_PARAMETER") allowedFolders: List<String>): EmailMailbox {
            val inbox = EmailMailbox(store)
            store.getFolder("INBOX").use { folder ->
                folder.open(2)
                inbox.folder = folder
                runBlocking {
                    async {
                        fetchAllPossibleEmails(folder, inbox)
                        inbox.applyTask {
                             if (folder.hasNewMessages()) {
                                 val time = measureTime { updateEmailMailbox(folder, inbox) }
                                 if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Email mailbox updated in ${time.inWholeMilliseconds} ms; $0 $this")
                             }
                        }
                    }.await()
                }
            }
            registeredMailboxes[id] = inbox
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Mailbox registered for service $0 $this, $1 $id")
            return inbox
        }

        /**
         * Updates the email mailbox with new messages from the given folder.
         *
         * @param folder the folder from which to retrieve new messages
         * @param inbox the email mailbox to update
         */
        private fun updateEmailMailbox(folder: Folder, inbox: EmailMailbox) {
            val newMessagesCount = folder.newMessageCount
            val max = folder.messageCount
            if (newMessagesCount > 0) {
                val messages = folder.getMessages(max, max + newMessagesCount)
                runBlocking { EmailService.performAsyncOperation { inbox.updateInbox(*messages) } }
            }
        }

        /**
         * Fetches all possible emails from the given folder and adds them to the given email mailbox.
         *
         * @param folder the folder from which to fetch emails
         * @param inbox the email mailbox to which to add the emails
         */
        @OptIn(DelicateCoroutinesApi::class)
        private suspend fun fetchAllPossibleEmails(folder: Folder, inbox: EmailMailbox) {
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Fetching emails... $inbox, $folder")
            val task = GlobalScope.async {
                inbox.updateInbox(*folder.messages)
            }
            task.join()
        }

    }

    /**
     * Opens the mailbox.
     */
    fun open() {
        if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Setting the 'READ_WRITE' flag to folder $0 $this.")
        val call = runCatching { task?.let { scheduler?.scheduleAtFixedRate(it, initialDelay, period, MAILBOX_RATE_UNIT) } }
        if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Setting the 'READ_WRITE' successfully $0 $this.")
        call.onFailure {
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().exception(it as Exception, "Something goes wrong")
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Setting the 'READ_WRITE' was unsuccessfully $0 $this.")
        }
    }

    /**
     * Applies the given event [BukkitEmailMessageEvent] and binding them to current mailbox [EmailMailbox]
     */
    fun bind(event: BukkitEmailMessageEvent) {
        this.event = event
    }

    fun emit(emitter: (it: BukkitEmailMessageEvent) -> Unit) {
        if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Calling emitter event (BukkitEmailMessageEvent) $0 $emitter")
        emitter(this.event)
    }

    /**
     * Applies the given task to the refresh task.
     *
     * @param task the task to apply
     */
    fun applyTask(task: Runnable) {
        this.task = task
        if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Task applied $0 $task $1 $this")
    }

    /**
     * Updates the mailbox with the given messages.
     *
     * @param messages the messages to add
     */
    suspend fun updateInbox(vararg messages: Message) {
        withContext(Dispatchers.IO) {
            val uniqueNewMessages = messages.filter { !this@EmailMailbox.messages.contains(it) }
            val overflow = (this@EmailMailbox.messages.size + uniqueNewMessages.size) - 2 * MAILBOX_BATCH_PER_REQUEST
            if (overflow > 0) {
                this@EmailMailbox.messages.addAll(uniqueNewMessages.takeLast((uniqueNewMessages.size - overflow).toInt()))
            } else {
                this@EmailMailbox.messages.addAll(uniqueNewMessages)
            }
        }
    }

    /**
     * Closes the mailbox.
     */
    fun close() {
        val result = runCatching {
            if (scheduler!!.isShutdown || scheduler!!.isShutdown) scheduler!!.shutdown()
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Shutdown the scheduler $0 $this $1 $scheduler")
            if (this.folder.isOpen) this.folder.close()
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Closing folder '$folder'")
            task = null
            isClosed = true
            if (PROJECT_DEBUG && EMAIL_DEBUG) SkriptMail.logger().debug("Removing repeating task $0 $task")
        }
        result.onFailure { it.printStackTrace() }
    }

    /**
     * Adds the given message to the mailbox.
     *
     * @param message the message to add
     */
    fun addMessage(message: Message?) {
        this.messages.add(message)
    }

    fun getEmails(intRange: IntRange, i: Int): Array<Message?> {
        if (this.messages.isEmpty()) return arrayOf()
        return when (i) {
            2 -> messages.subList(intRange.first, intRange.last).toTypedArray()
            1 -> messages.subList((messages.size - intRange.last), (messages.size - intRange.first)).toTypedArray()
            else -> arrayOf()
        }
    }
    fun getFirstEmail(): Message? {
        return messages.last()
    }
    fun getLastEmail(): Message? {
        return this.messages.first()
    }

}
