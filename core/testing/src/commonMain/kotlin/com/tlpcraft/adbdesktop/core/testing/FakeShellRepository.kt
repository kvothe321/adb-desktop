package com.tlpcraft.adbdesktop.core.testing

import com.tlpcraft.adbdesktop.core.common.Outcome
import com.tlpcraft.adbdesktop.core.domain.error.DeviceError
import com.tlpcraft.adbdesktop.core.domain.repository.ShellRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile

/** Echoes every command as `output of <command>`. The command `exit` ends the session. */
class FakeShellRepository : ShellRepository {
    val received = mutableListOf<String>()
    var sessionsStarted = 0
        private set

    override fun runSession(serial: String, commands: Flow<String>): Flow<Outcome<String, DeviceError>> = flow {
        sessionsStarted++
        commands.takeWhile { it != "exit" }.collect { command ->
            received += command
            emit(Outcome.Success("output of $command\n"))
        }
    }
}
