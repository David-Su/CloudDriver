package cloud.manager

import java.util.logging.Level
import java.util.logging.Logger

val logger: Logger = Logger.getLogger("CloudDriver").also {
    it.level = Level.ALL
}