package cloud.manager

import cloud.bean.UploadTask

object UploadTaskManager {

    interface Listener {
        fun onTasksUpdate(tasks: List<UploadTask>)
        fun onTaskRemove(path: String)
    }

    private val tasksMap = hashMapOf<String, ArrayList<UploadTask>>()
    private val listenerMap = hashMapOf<String, ArrayList<Listener>>()

    fun getCurrentTasks(username: String): List<UploadTask>? = tasksMap[username]

    fun addListener(username: String, listener: Listener) {
        val listeners = listenerMap[username]
                ?: arrayListOf<Listener>().also { listenerMap[username] = it }
        if (!listeners.contains(listener)) listeners.add(listener)
    }

    fun removeListener(username: String, listener: Listener) {
        listenerMap[username]?.removeIf { it == listener }
    }

    fun updateTask(username: String, uploadTask: UploadTask? = null) {
        val tasks = tasksMap[username] ?: arrayListOf<UploadTask>().also { tasksMap[username] = it }

        if (uploadTask != null) {
            val index = tasks.indexOfFirst { uploadTask == it || it.path == uploadTask.path }.takeIf { it != -1 }
            if (index != null) {
                tasks.removeAt(index)
                tasks.add(index, uploadTask)
            } else {
                tasks.add(uploadTask)
            }
        }

        listenerMap[username]?.forEach { it.onTasksUpdate(tasks) }
    }

    fun removeTask(username: String, uploadTask: UploadTask? = null) {

        if (uploadTask != null) {
            tasksMap[username]?.removeIf { it == uploadTask || it.path == uploadTask.path }
            if (tasksMap[username].isNullOrEmpty()) tasksMap.remove(username)

            listenerMap[username]?.forEach { it.onTaskRemove(uploadTask.path) }

        } else {
            tasksMap[username]?.forEach { task ->
                listenerMap[username]?.forEach { it.onTaskRemove(task.path) }
            }
            tasksMap.remove(username)
        }

    }
}