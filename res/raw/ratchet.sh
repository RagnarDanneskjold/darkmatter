#!/system/bin/sh

PATH=$(dirname $0):$PATH
APPLIST=(org.whispersystems.whisperpush
org.torproject.android
at.rundquadrat.android.r2mail2
com.twofours.surespot
de.blinkt.openvpn
info.guardianproject.otr.app.im
net.i2p.android.router
net.openvpn.openvpn
com.xabber.androiddev
org.thialfihar.android.apg
com.fsck.k9
org.thoughtcrime.redphone
org.thoughtcrime.textsecure
)


function app_kill() { # <apk.name>
	am force-stop "$1"
	killall -9 "$1"
}

function app_kill_all() { #
	for app in ${APPLIST[*]}; do
		app_kill "$app"
	done
}

function mem_wiper() {
	nohup ./smem -f -l -l &
	# wipe cache?
}

function slam_closed() {
	app_kill_all
	./tc close "volume.dat"
	mem_wiper
}

slam_closed
