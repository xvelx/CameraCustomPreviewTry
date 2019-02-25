package xvelx.github.io.cameratry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
//                .replace(R.id.container, MyCameraFragment())
//                .replace(R.id.container, Camera2BasicFragment())
//                .replace(R.id.container, SimpleCameraFragment())
                .replace(R.id.container, SimpleCamera2Fragment())
                .commit()
        }
    }
}
