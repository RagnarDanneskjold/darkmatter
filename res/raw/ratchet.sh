#!/system/bin/sh

APPLIST=""

function app_kill() { # <apk.name>
	am force-stop "$1"
	killall -9 "$1"
}

function app_kill_all() { #
	for app in $APPLIST; do
		app_kill "$app"
	done
}

function mem_wiper() {
	./smem -f -l
	# wipe cache?
}

app_kill_all
mem_wiper
