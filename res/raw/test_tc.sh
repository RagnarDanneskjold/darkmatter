#!/system/bin/sh

PASS_OUTER="password1"
PASS_HIDDEN="secret"
VOL_PATH="vol_test.dat"

# test create, 

function test_create() {
	./tc.sh create $VOL_PATH 500 200 $PASS_OUTER $PASS_HIDDEN
}

function test_open() {
	./tc.sh open $VOL_PATH "/mnt/extSdCard" $1
}

function test_open_outer() {
	test_open $PASS_OUTER
}

function test_open_hidden() {
	test_open $PASS_HIDDEN
}

function test_close() {
	./tc.sh close $VOL_PATH
}

function test_delete() {
	./tc.sh delete $VOL_PATH
}

function dump_info() {
	losetup 
	grep "/dev/mapper" /proc/mounts
}

case $1 in
	n) test_create ;;
	o) test_open_outer ;;
	i) test_open_hidden ;;
	c) test_close ;;
	d) test_delete ;;
	l) dump_info ;;
	a) test_create 
		echo "opening outter"
		test_open_outer 
		test_close 
		echo "--------------------------"
		echo "should be blank"
		dump_info
		echo "--------------------------"
		echo "opening hidden"
		test_open_hidden
		test_close
		echo "--------------------------"
		dump_info
		echo "--------------------------"
		test_delete 
		;;
	
	*) echo "$(basename $0) <n|o|i|c|l|d>"
	   echo "          n - create"
	   echo "          o - open outer"
	   echo "          i - open inner"
	   echo "          c - close"
	   echo "          d - delete"
	   echo "          l - list"
	   exit 2
esac
