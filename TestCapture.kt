import kotlin.reflect.KProperty

class IntState(var value: Int) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int = value
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
    }
}

fun main() {
    var selectedTab by IntState(0)
    
    val updateStyle: (Int) -> Unit = { 
        if (selectedTab == 0) println("Title") else println("Body")
    }
    
    updateStyle(1) // Output: Title
    
    selectedTab = 1
    
    updateStyle(1) // Output: Body
}
