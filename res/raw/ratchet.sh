#!/system/bin/sh

PATH=$(dirname $0):$PATH

function app_get_list() { #
	cat "$(dirname 0)/applist"
}

function app_kill() { # <apk.name>
	am force-stop "$1"
	killall -9 "$1"
}

function app_kill_all() { #
	for app in `app_get_list` ; do
		app_kill "$app"
	done
}

function mem_wiper() {
	nohup ./smem -f -l -l &
	# wipe cache?
}

app_kill_all
mem_wiper
