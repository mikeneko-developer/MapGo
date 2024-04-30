package net.mikemobile.navi.bluetooth

internal abstract class CustomRunnable(private val obj: Any?) : Runnable {
    abstract fun run(`object`: Any)

    override fun run() {
        obj?.let { run(it) }
    }
}
