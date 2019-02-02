package o2offx.awagner.tikkit.model

enum class ValidationResult{
    VALID,
    INVALID,
    NOT_A_TICKET
}

class Ticket {
    val id: String
    val name: String
    var securityData: String
    var other: String

    constructor(ID: String, Name: String) {
        id = ID
        name = Name
        securityData = ""
        other = ""
    }

    constructor(ID: String, Name: String, SecurityData: String) {
        id = ID
        name = Name
        securityData = SecurityData
        other = ""
    }

    constructor(ID: String, Name: String, SecurityData: String, Other: String) {
        id = ID
        name = Name
        securityData = SecurityData
        other = Other
    }

    fun isFormatValid() : Boolean {
        return id != "" && name != ""
    }
}