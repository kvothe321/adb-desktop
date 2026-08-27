package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.model.TerminalSize
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile

/** Answers all input with `output of <input>`. The input `exit` ends the session. */
class FakeShellRepository : ShellRepository {
    val received = mutableListOf<String>()
    var sessionsStarted = 0
        private set
    var startedWith: TerminalSize? = null
        private set

    override fun runSession(serial: String, input: Flow<String>, sizes: Flow<TerminalSize>): Flow<Outcome<String, DeviceError>> = flow {
        startedWith = sizes.first()
        sessionsStarted++
        input.takeWhile { it != "exit" }.collect { text ->
            received += text
            emit(Outcome.Success("output of $text\r\n"))
        }
    }
}
