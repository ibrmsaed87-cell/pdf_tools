import kotlin.reflect.KProperty

class State {
    var value = 0
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
    }
}

fun main() {
    var selectedTab by State()
    val update = {
        println(if (selectedTab == 0) "Zero" else "One")
    }
    update()
    selectedTab = 1
    update()
}
