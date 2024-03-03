package cz.coffeerequired.skriptmail.api.email

class EmailException(msg: String, type: EmailExceptionType? = EmailExceptionType.UNKNOWN) : Exception("[$type] $msg")
