package cloud.manager

import cloud.model.net.UploadTask

object UploadTaskManager {

    interface Listener {
        fun onTasksUpdate(tasks: List<UploadTask>)
        fun onTaskRemove(path: String)
    }

    private val tasksMap = hashMapOf<String, ArrayList<UploadTask>>()
    private val listenerMap = hashMapOf<String, ArrayList<Listener>>()

    fun getCurrentTasks(username: String): List<UploadTask>? = tasksMap[username]

    fun addListener(username: String, listener: Listener): Unit = synchronized(username.intern()) {
        val listeners = listenerMap[username]
                ?: ArrayList<Listener>().also { listenerMap[username] = it }
        if (!listeners.contains(listener)) listeners.add(listener)
    }


    fun removeListener(username: String, listener: Listener): Unit = synchronized(username.intern()) {
        listenerMap[username]?.removeIf { it == listener }
    }

    fun addTask(username: String, uploadTask: UploadTask) = synchronized(username.intern()) {
        logger.info("addTask->${uploadTask.toString()}")
        val tasks = tasksMap[username] ?: (ArrayList<UploadTask>().also {
            logger.info("addTask 新建list->${System.identityHashCode(it)}")
            tasksMap[username] = it
        })
        tasks.add(uploadTask)
        updateTask(username)
    }

    fun updateTask(username: String, uploadTask: UploadTask? = null): Unit = synchronized(username.intern()) {

        val tasks = tasksMap[username] ?: return@synchronized

        listenerMap[username]?.forEach { it.onTasksUpdate(tasks) }

//        logger.info("onTasksUpdate->${tasks.map { it.toString() }}  ${System.identityHashCode(tasks)}")
    }

    fun removeTask(username: String, uploadTask: UploadTask? = null): Unit = synchronized(username.intern()) {

        if (uploadTask != null) {
            tasksMap[username]?.remove(uploadTask).also {
                logger.info("removeTask->${uploadTask.toString()}  remove成功->${it}")
            }
        } else {
            tasksMap[username]?.clear()
        }

        updateTask(username)
    }
}