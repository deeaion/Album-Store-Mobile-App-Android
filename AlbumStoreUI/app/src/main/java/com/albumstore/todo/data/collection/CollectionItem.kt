package com.albumstore.todo.data.collection

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.albumstore.todo.data.product.ImageDto

@Entity(tableName = "collection_items")
class CollectionItem (
    @PrimaryKey
    var id: String = "",
    val productId: String? = "",
    var imageId: String? = "",
    val title: String = "",
    val artist: String = "",
    val image: ImageDto? = null,
) {
    override fun toString(): String {
        return "CollectionItem(id='$id', productId=$productId, imageId=$imageId, title='$title', artist='$artist', imageBase64=${image?.imageBase64?.take(10)}" +
                ", contentType=${image?.contentType}, fileName=${image?.fileName})"
    }
}
