package cloud.endpoint

import cloud.model.net.UploadTask
import cloud.manager.UploadTaskManager
import cloud.manager.logger
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import java.util.Timer
import javax.websocket.*
import javax.websocket.server.ServerEndpoint
import kotlin.concurrent.schedule
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

@ServerEndpoint("/websocket/uploadtasks")
class DownloadEndPoint {

    companion object {

        private const val KEY_DATA_TYPE = "dataType"
        private const val KEY_DATA = "data"

        //更新任务上传进度
        private const val DATA_TYPE_UPDATE = 0

        //移除任务
        private const val DATA_TYPE_REMOVE = 1

    }

    private var listener: UploadTaskManager.Listener? = null

    private var _username: String? = null

    private val username get() = _username!!

    private val debugTimer = Timer()

    @OnOpen
    fun onOpen(session: Session) {
        logger.info("onOpen  ${session.pathParameters} ${session.requestParameterMap}")
        val token = session.requestParameterMap["token"]?.getOrNull(0)

        if (token.isNullOrEmpty() || !TokenUtil.valid(token)) {
            session.close()
            return
        }

        val username = TokenUtil.getUsername(token)

        _username = username

        val listener = object : UploadTaskManager.Listener {
            override fun onTasksUpdate(tasks: List<UploadTask>) {

//                logger.info("onTasksUpdate->${JsonUtil.toJson(tasks)}")

                mapOf(
                        Pair(KEY_DATA_TYPE, DATA_TYPE_UPDATE),
                        Pair(KEY_DATA, tasks),
                ).let {
                    JsonUtil.toJson(it)
                }.also {
                    session.basicRemote.sendText(it)
                }

            }

            override fun onTaskRemove(path: String) {

                logger.info("onTaskRemove->${path}")

                mapOf(
                        Pair(KEY_DATA_TYPE, DATA_TYPE_REMOVE),
                        Pair(KEY_DATA, path),
                ).let {
                    JsonUtil.toJson(it)
                }.also {
                    session.basicRemote.sendText(it)
                }

            }

        }

        //初始化数据
        UploadTaskManager.getCurrentTasks(username)?.also { listener.onTasksUpdate(it) }
        UploadTaskManager.addListener(username, listener)
        this.listener = listener

        debugTimer.schedule(timerTask {
            logger.info {
                val tasks = UploadTaskManager.getCurrentTasks(username)
                "定时获取任务信息：${tasks}"
            }
        }, 10 * 1000, 10 * 1000)
    }


    @OnClose
    fun onClose() {


        logger.info("onClose")
        listener?.also { UploadTaskManager.removeListener(username, it) }
        debugTimer.cancel()
    }


    @OnMessage
    fun onMessage(message: String, session: Session) {
        logger.info("来自客户端的消息:$message")
    }

    @OnError
    fun onError(session: Session?, error: Throwable) {
        logger.info("websocket发生错误：$error")
    }

}