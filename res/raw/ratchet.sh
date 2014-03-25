#!/system/bin/sh

APPLIST=""

function app_kill() { # <apk.name>
	am force-stop "$1"
	killall -9 "$1"
}

function app_kill_all() { #
}

function mem_wiper() {
}
