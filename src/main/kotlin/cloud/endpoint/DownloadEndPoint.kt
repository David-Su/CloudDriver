package cloud.endpoint

import cloud.bean.UploadTask
import cloud.manager.UploadTaskManager
import cloud.manager.logger
import cloud.util.JsonUtil
import cloud.util.TokenUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonWriter
import javax.websocket.*
import javax.websocket.server.ServerEndpoint

@ServerEndpoint("/websocket/uploadtasks")
class DownloadEndPoint {

    companion object {
        //更新任务上传进度
        private const val DATA_TYPE_UPDATE = 0

        //移除任务
        private const val DATA_TYPE_REMOVE = 1
    }

    private val webSockets = hashMapOf<String, Session>()

    private var token: String? = null

    private var listener: UploadTaskManager.Listener? = null

    @OnOpen
    fun onOpen(session: Session) {
        logger.info("onOpen  ${session.pathParameters} ${session.requestParameterMap}")
        val token = session.requestParameterMap["token"]?.getOrNull(0)
        val username = TokenUtil.getUsername(token)
        if (token.isNullOrEmpty() || !TokenUtil.valid(token)) {
            session.close()
            return
        }
        this.token = token
        webSockets[token] = session


        val listener = object : UploadTaskManager.Listener {
            override fun onTasksUpdate(tasks: List<UploadTask>) {

                if (!TokenUtil.valid(token)) {
                    session.close()
                    return
                }

                logger.info("onTasksUpdate->${JsonUtil.toJson(tasks)}")

                mapOf(pair = Pair(DATA_TYPE_UPDATE, tasks))
                        .let {
                            JsonUtil.toJson(it)
                        }.also {
                            session.basicRemote.sendText(it)
                        }

            }

            override fun onTaskRemove(path: String) {

                logger.info("onTaskRemove->${path}")

                mapOf(pair = Pair(DATA_TYPE_REMOVE, path))
                        .let {
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
    }


    @OnClose
    fun onClose() {
        logger.info("onClose")
        token?.takeIf { webSockets.containsKey(it) }?.also { webSockets.remove(it) }
        listener?.also { UploadTaskManager.removeListener(TokenUtil.getUsername(token), it) }
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