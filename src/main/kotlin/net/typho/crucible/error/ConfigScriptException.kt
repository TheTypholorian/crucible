package net.typho.crucible.error

import kotlin.script.experimental.api.ScriptDiagnostic

class ConfigScriptException : RuntimeException {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )

    constructor(report: ScriptDiagnostic) : this(report.render(withSeverity = false, withStackTrace = true))
}