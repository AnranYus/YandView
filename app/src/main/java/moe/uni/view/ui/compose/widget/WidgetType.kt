package moe.uni.view.ui.compose.widget

open class SettingType <T>(val title:String, val value:T,val onValueChanged:(T) -> Unit){
    class Switch(title: String,value:Boolean, onValueChanged:(Boolean) -> Unit):SettingType<Boolean>(title,value, onValueChanged)

}