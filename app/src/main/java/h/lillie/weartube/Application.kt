package h.lillie.weartube

import android.app.Application

class Application : Application() {
    companion object {
        private var videoData = String()
        fun getVideoData() : String {
            return videoData
        }
        fun setVideoData(info: String) {
            videoData = info
        }
    }
}