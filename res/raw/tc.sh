#!/system/bin/sh

TCNAME="emmc"
TCDEVICE="/dev/mapper/${TCNAME}"
APPLIST=(org.whispersystems.whisperpush
org.torproject.android
at.rundquadrat.android.r2mail2
com.twofours.surespot
de.blinkt.openvpn
info.guardianproject.otr.app.im
net.i2p.android.router
net.openvpn.openvpn
org.thialfihar.android.apg
com.fsck.k9
org.thoughtcrime.redphone
org.thoughtcrime.textsecure
com.xabber.androiddev
)

PATH=${PATH}:$(dirname $0)

umask 022

mkdir -p /dev/mapper
mkdir -p /sdcard/Android/data

function loop_open() { # <volpath>
	local volpath="$1"

	local device=$(losetup -f)
	losetup "$device" "$volpath"
	echo "$device"
}

function loop_lookup() { # <volpath>
	local volpath="$1"

	local device=$(losetup| grep $volpath | head -1| sed -e 's/:.*$//')
	echo "$device"
}

function loop_close() { # <volpath>
	local volpath="$1"

	local device=$(loop_lookup $volpath)
	losetup -d $device
}

function volume_create() { # <volpath> <num> <unit>
	local volpath="$1"
	local num="$2"
	local unit="$3"

	case $unit in
		m) count=$num ;;
		M) count=$num ;;
		g) count=$(($num * 1024)) ;;
		G) count=$(($num * 1024)) ;;
		*) echo "unknown unit: $unit!" && exit 200
	esac

	dd if=/dev/zero of=$volpath bs=$((1024 * 1024)) count=$count
}

function volume_delete() { # <volpath>
	local volpath="$1"
	# offset: 0
	# offset: (volume_size)-BACKUP_HDR_OFFSET
	# offset: HIDDEN_HDR_OFFSET
	#define HDR_OFFSET_HIDDEN	65536
	#define BACKUP_HDR_HIDDEN_OFFSET_END	65536
	#define BACKUP_HDR_OFFSET_END	131072
	
	local volsize=$(ls -l "$volpath"| cut -d' ' -f 12)
	# wipe hidden header
	dd if=/dev/zero of="$volpath" bs=512 count=1 seek=65536
	# wipe real header
	dd if=/dev/zero of="$volpath" bs=512 count=1 seek=0
	# wipe backup header
	dd if=/dev/zero of="$volpath" bs=512 count=1 seek=$(($volsize - 131072))

	rm -f $volpath
}

function setup_app() { # <appname> <mount_dir>
        local appname="$1"
        local tcdir="$2"

        if [ ! -d $tcdir/Android/data ]; then
                mkdir -p "$tcdir/Android/data"
        fi

        if [ ! -d "$tcdir/data" ]; then
                mkdir -p "$tcdir/data"
        fi

        local user=`get_app_user $appname`

        if [ ! -d "$tcdir/Android/data/$appname" ]; then
        	mkdir -p "$tcdir/Android/data/$appname"
        	chown $user:$user "$tcdir/Android/data/$appname"
	fi

        if [ ! -d "$tcdir/data/$appname" ]; then
        	mkdir -p "$tcdir/data/$appname"
        	chown $user:$user "$tcdir/data/$appname"
	fi
}

# the device must already be mapped onto a /dev/mapper/$NAME
function tc_mount() { # <tcdevice> <mountpath>
	local tcdevice="$1"
	local path="$2"
	
	if [ ! -d "$path" ]; then
		mount -o remount,rw /
		mkdir -p "$path"
		mount -o remount,ro /
	fi
	
	mount -o "noatime,nodev" -t ext4 $tcdevice $path
}

function tc_unmount() { # <volpath>
	local volpath="$1"

	local retries="2"

	local device=$(loop_lookup $volpath)

	# $(seq $retries)
	for i in $(seq $retries); do
		local mounts=$(grep "$TCDEVICE" /proc/mounts | cut -d ' ' -f 2)
		for m in $mounts; do
			umount $m
		done
	done
}

function tc_map() { # <device> <name> <password> [<hidden>]
        local device="$1"
        local name="$2"
        local password="$3"
	local hidden="$4"

	if [ -z $hidden ]; then
        	(echo $password; sleep 1) | (tcplay -d $device --map $name) >&2
	else
		(echo $password; sleep 1; echo $hidden; sleep 1) | (tcplay -d $device --map $name -e) >&2
	fi
}

function tc_unmap() { # <device> <name>
	local device="$1"
	local name="$2"
	
	tcplay -d $device --unmap $name
}

function tc_init_device() { # <device> <target> <password> [<hidden>]
	local device="$1"
	local target="$2"
	local password="$3"
	local hidden="$4"

	if [ ! -d "$target" ]; then
		mount -o rw,remount /
		mkdir -p $target
		mount -o ro,remount /
	fi

        tc_map "$device" "$TCNAME" "$password" $hidden
        mkfs.ext2 -O ^has_journal $TCDEVICE
	mount -o "noatime,nodev" -t ext4 $TCDEVICE "$target"
	mkdir -p "$target/data" 
	mkdir -p "$target/Android/data"
	chmod 0755 "$target/data"
	chmod 0755 "$target/Android/data"
	for appname in ${APPLIST[*]}; do
		setup_app "$appname" "$target"
	done
	umount "$target"
	tc_unmap "$device" "$TCNAME"
}

function tc_create() { # <volpath> <size> <hiddensz> <pass1> <pass2>
	local volpath="$1"
	local size="$2"
	local hiddensz="$3"M
	local pass1="$4"
	local pass2="$5"

	# dd /dev/zero, volpath, num_mb, "M" (or 'G' for num_gb)
	volume_create "$volpath" "$size" "M"
	# loop mount
	local device=$(loop_open "$volpath")

	local DEBUG="-z -w"
        # tcplay create
        (
                echo $pass1
                sleep 1
                echo $pass1
                sleep 1
                echo $pass2
                sleep 1
                echo $pass2
                sleep 1
                echo $hiddensz
                sleep 1
                echo "y"
        ) | (tcplay -d $device --create --hidden --cipher=AES-256-XTS $DEBUG) >&2

        #   send password, send password
        #   send hiddenp, send hiddenp
        #   send hidden_size
        #   send "y"

	local target="/mnt/extSdCard"

	tc_init_device "$device" "$target" "$pass2"
	tc_init_device "$device" "$target" "$pass1" "$pass2"
        
        losetup -d $device
}

function get_app_user() {
        echo $(ls -ld "/data/data/$1"| cut -d' ' -f 2)
}

function bind_mount() { # <from> <dest> <user>
	local from="$1"
	local dest="$2"
	local user="$3"

	local m=$(grep "$dest" /proc/mounts| wc -l)
	if [ $m -ne 0 ]; then
                return 1
	fi

	mount -o bind,user=$user,relatime,nodev $from $dest
	return $?
}

function app_mount() { # <app_name> <mount_path>
	local appname="$1"
	local tcdir="$2"
	local user=`get_app_user $appname`

	# make sure nothing is open on our target directory. maybe
	killall $appname >/dev/null 2>/dev/null

	if [ ! -d "$tcdir/data/$appname" ]; then
		setup_app "$tcdir" "$appname"
	fi

	bind_mount "$tcdir/data/$appname" "/data/data/$appname" "$user"
	bind_mount "$tcdir/Android/data/$appname" "/sdcard/Android/data/$appname" "$user"
}

function app_umount() { # <app_name>
	local appname="$1"
	
	killall $appname >/dev/null 2>/dev/null

	umount "/data/data/$appname"
	umount "/sdcard/Android/data/$appname"
}

function app_kill() { # <package>
	am force-stop "$1"
}

function tc_open() { # <volpath> <mountpath> <password>
	local volume="$1"
	local path="$2"
	local password="$3"

	local device=$(loop_open "$volume")

	tc_map "$device" "$TCNAME" "$password"
	tc_mount "$TCDEVICE" "$path"
		
	for app in ${APPLIST[*]}; do
		app_kill "$app"
		app_mount "$app" "$path"
	done
}

function tc_close() { # <volpath>
	local volpath="$1"
	local device=$(loop_lookup "$volpath")

	for app in ${APPLIST[*]}; do
		app_kill "$app"
		app_umount "$app" "$path"
	done

	tc_unmount "$volpath"
	tc_unmap "$device" "$TCNAME"
	loop_close "$volpath"
}

function tc_delete() { # <volpath>
	local volpath="$1"

	volume_delete "$volpath"
}

case $1 in
	"create")
		shift
		tc_create $*
		;;
	"open")
		shift # discard first arg
		tc_open $*
		exit 0
		;;
	"close")
		shift
		tc_close $*
		;;
	"delete")
		shift
		tc_delete $*
		;;
	*)
		echo "$0 <create|open|close|delete> [args]" 
		exit 127
		;;
esac

