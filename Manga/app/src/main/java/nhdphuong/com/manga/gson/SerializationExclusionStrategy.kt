package nhdphuong.com.manga.gson

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/*
 * Created by nhdphuong on 3/24/18.
 */
class SerializationExclusionStrategy : ExclusionStrategy {

    override fun shouldSkipField(f: FieldAttributes) = f.getAnnotation(SerializationExclude::class.java) != null

    override fun shouldSkipClass(clazz: Class<*>) = false
}