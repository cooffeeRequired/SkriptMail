package cz.coffeerequired.skriptmail.api

class Version(
    private var major: Long,
    private var minor: Long,
    private var patch: Long
): Comparable<Version> {
    operator fun component1() = major
    operator fun component2() = minor
    operator fun component3() = patch

    constructor(strVersion: String) : this(0, 0, 0) {
        val parts: List<String> = strVersion.split(".")
        if (parts.size >= 2) {
            val major: Long? = parts[0].toLongOrNull()
            val minor: Long? = parts[1].toLongOrNull()
            val patch: Long = parts.getOrNull(2)?.toLongOrNull() ?: 0L
            if (major != null) this.major = major
            if (minor != null) this.minor = minor
            this.patch = patch
        } else {
            throw IllegalArgumentException("Version must have at least a major, minor, and patch version")
        }
    }

    override fun compareTo(other: Version): Int {
        val majorDiff = this.major.compareTo(other.major)
        if (majorDiff != 0) {
            return majorDiff
        }

        val minorDiff = this.minor.compareTo(other.minor)
        if (minorDiff != 0) {
            return minorDiff
        }

        return this.patch.compareTo(other.patch)
    }

    override fun toString(): String {
        return "Version{major: ${this.major}, minor: ${this.minor}, patch: ${this.patch}}"
    }

    public fun toString(d: Int): String {
        return when (d) {
            0 -> "major: ${this.major}, minor: ${this.minor}, patch: ${this.patch}"
            1 -> "${this.major}.${this.minor}.${this.patch}"
            else -> toString()
        }
    }

    public fun getSanitized(): Long {
        return this.toString(1).replace(".", "").toLong()
    }
}
