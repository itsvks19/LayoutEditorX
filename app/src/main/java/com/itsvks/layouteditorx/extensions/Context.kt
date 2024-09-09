package com.itsvks.layouteditorx.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import com.itsvks.layouteditor.vectormaster.VectorMasterDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> Context.open(clazz: Class<T>, newTask: Boolean = false) {
  val intent = Intent(this, clazz)
  if (newTask)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

  startActivity(intent, getEmptyActivityBundle())
}

fun Context.getEmptyActivityBundle(): Bundle? {
  return ActivityOptionsCompat.makeCustomAnimation(
    this,
    android.R.anim.fade_in,
    android.R.anim.fade_out
  ).toBundle()
}

suspend fun Context.getVectorDrawableAsync(uri: Uri): VectorMasterDrawable? {
  return try {
    withContext(Dispatchers.IO) {
      contentResolver.openInputStream(uri).use {
        val drawable = VectorMasterDrawable(this@getVectorDrawableAsync).apply {
          setInputStream(it)
        }
        drawable
      }
    }
  } catch (err: Exception) {
    err.printStackTrace()
    null
  }
}