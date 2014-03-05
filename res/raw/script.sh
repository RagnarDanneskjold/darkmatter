#!/system/bin/sh

# /system/bin must come first, /system/xbin/ has only busybox tools!
PATH=/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin

function stop_app() { # <app name>
        local app="$1"

        su 0 am force-stop "$app" 2>&1 > /dev/null
        return $?
}

stop_app at.rundquadrat.android.r2mail2

su 0 sh /sdcard/Download/b.sh umount
su 0 sh /sdcard/Download/m umount
su 0 truecrypt -d 2>&1 >/dev/null
