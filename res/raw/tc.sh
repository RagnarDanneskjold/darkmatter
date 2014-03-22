#!/system/bin/sh

TCNAME="emmc"
TCDEVICE="/dev/mapper/${TCNAME}"

mkdir -p /dev/mapper

function killapk() { # <package>
	am force-stop "$1"
}

function loop_open() { # <volpath>
	local volpath="$1"

	local device=$(losetup -f)
	losetup "$device" "$volpath"
	echo "$device"
}

function loop_lookup() { # <volpath>
	local volpath="$1"

	local device=$(losetup| grep $volpath | cut -d' ' -f 3)
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
		[m][M]) count=$num ;;
		[g][G]) count=$(($num * 1024)) ;;
		*) echo "unknown unit: $unit!" && exit 200
	esac

	dd if=/dev/zero of=$volpath size=$((1024 * 1024)) count=$count
}

function volume_delete() { # <volpath>
	local volpath="$1"
	# dd if=/dev/zero of=$volpath count=4096
	rm $volpath
}

function setup_app() { # <appname> <mount_dir>
        local appname="$1"
        local tcdir="$2"

        if [ ! -d $tcdir/Android/data ]; then
                mkdir -p "$tcdir/Android/data"
        fi

        if [ ! -d $tcdir/data ]; then
                mkdir -p "$tcdir/data"
        fi

        local user=`get_app_user $appname`

        mkdir -p "$tcdir/Android/data/$appname"
        chown $user:$user "$tcdir/Android/data/$appname"

        mkdir -p "$tcdir/data/$appname"
        chown $user:$user "$tcdir/data/$appname"
}

function tc_mount() { # <volpath> <mountpath>
	local volpath="$1"
	local target="$2"

	local device=$(loop_lookup $volpath)
	if [ -z $device ]; then
		local device=$(loop_open $volpath)
	fi

	tcplay -d $device --map $TCNAME

	mount -o "noatime,nodev" -t ext4 $TCDEVICE $target
}

function tc_unmount() { # <volpath>
	local volpath="$1"

	local device=$(loop_lookup $volpath)

	for i in "1" "2"; do
		local mounts=$(grep "$TCDEVICE" /proc/mounts | cut -d ' ' -f 2)
		for m in $mounts; do
			umount $m
		done

		tcplay -d $device --unmap $TCNAME
		if [ $? -eq 0 ]; then
			return 0
		fi
	done
}

function tc_map() { # <device> <name> <password>
        local device="$1"
        local name="$2"
        local password="$3"
        (echo $password; sleep 1) | (tcplay -d $device --map $name) >&2
}

function tc_create() { # <volpath> <size> <pass1> <pass2>
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
                echo "$hiddensz"M
                sleep 1
                echo "y"
        ) | (tcplay -d $device --create --hidden --cipher=AES-256-XTS $DEBUG) >&2

        #   send password, send password
        #   send hiddenp, send hiddenp
        #   send hidden_size
        #   send "y"

        # mount real
                # mkfs.ext4
                # mkdir
        # umount real
        tc_map "$device" "$TCNAME" "$pass1"
        mkfs.ext2 -O ^has_journal $TCDEVICE
        tcplay -d $device --unmap $TCNAME

        # mount decoy
                # mkfs.ext4
                # mkdir
		# umount decoy
		tc_map "$device" "$TCNAME" "$pass1"
        mkfs.ext2 -O ^has_journal $TCDEVICE
        tcplay -d $device --unmap $TCNAME
        
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

function mount_app() { # <app_name> <mount_path>
        local appname="$1"
	local tcdir="$2"
        local user=`get_app_user $appname`

        # make sure nothing is open on our target directory. maybe
	killapk $appname >/dev/null 2>/dev/null
        killall $appname >/dev/null 2>/dev/null

        local datadir="/data/data"

	mkdir -p /sdcard/Android/data >/dev/null 2>/dev/null

        bind_mount "$tcdir/data/$appname" "/data/data/$appname" "$user"
        bind_mount "$tcdir/Android/data/$appname" "/sdcard/Android/data/$appname" "$user"
}

function tc_open() { # <volpath> <mountpath> 
        local volume="$1"
        local path="$2"
        local password="$3"

        # create loopback device
        local device=$(losetup -f)
        losetup "$device" "$volume"


        # decrypt the volume
        # tcplay -d <volume> -m <name>
        tcplay -d $device -m $TCNAME
	# send password???

	if [ ! -d "$path" ]; then
		mount -o remount,rw /
		mkdir -p "$path"
		mount -o remount,ro /
	fi

        # mount </dev/mapper/name> <path>
	mount -o noatime $TCDEVICE "$path"

	# for apk in $apklist
	# mount_apk "$apk" "$path"
}

function tc_close() { # <volpath>
        local volpath="$1"

        # killapps

        tc_unmount $volpath
}

case $1 in
	"create")
		shift
		tc_create $*
		;;
	"open")
		shift # discard first arg
		tc_open $*
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

