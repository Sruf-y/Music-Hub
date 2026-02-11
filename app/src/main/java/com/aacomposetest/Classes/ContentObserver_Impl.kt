package com.aacomposetest.Classes

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper

class ContentObserver_Impl(
   onAnyChange:(selfChange:Boolean)->Unit = {_-> },
   onSingleChange:(selfChange: Boolean, uri: Uri?, flags: Int)->Unit = {_,_,_-> },
   onMultipleChanges:(selfChange: Boolean, uris: Collection<Uri?>, flags: Int)-> Unit = {_,_,_-> }
):ContentObserver(Handler(Looper.getMainLooper())) {
   override fun onChange(selfChange: Boolean) {
      super.onChange(selfChange)
   }
   
   override fun onChange(selfChange: Boolean, uri: Uri?) {
      super.onChange(selfChange, uri)
   }
   
   override fun onChange(selfChange: Boolean, uri: Uri?, flags: Int) {
      super.onChange(selfChange, uri, flags)
   }
   
   override fun onChange(selfChange: Boolean, uris: Collection<Uri?>, flags: Int) {
      super.onChange(selfChange, uris, flags)
   }
   
   // Yes, i am interested in being notified about changes i make myself
   override fun deliverSelfNotifications(): Boolean {
      return true
   }
}
