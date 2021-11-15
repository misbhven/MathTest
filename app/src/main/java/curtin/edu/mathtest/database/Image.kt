package curtin.edu.mathtest.database

import java.io.Serializable

class Image(private var imgBArray : ByteArray?) : Serializable {
    fun setImg(newImg : ByteArray?) {
        imgBArray = newImg
    }

    fun getImg() : ByteArray? {
        return imgBArray
    }
}