#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <string.h>
#include <arpa/inet.h>
#include <errno.h>
#include <linux/unistd.h>
#include <pthread.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include "termios.h"
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/time.h>
#include <linux/input.h>
#include <android/log.h>
#include <math.h>
#include <fcntl.h>
#include <sys/epoll.h>


#define LOG_TAG "serialAPI"
#undef  LOG
#define LOGD(...)	//__android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

int rs485Fd = -1;
//配置串口参数
int set_opt(int fd, int nSpeed, int nBits, char nEvent, int nStop) {
	struct termios newtio, oldtio;
	if (tcgetattr(fd, &oldtio) != 0) {
		perror("SetupSerial 1");
		return -1;
	}
	bzero(&newtio, sizeof(newtio));
	newtio.c_cflag &= ~CSTOPB;
	newtio.c_cflag &= ~CSIZE;
	newtio.c_cflag |= (CLOCAL | CREAD);
	newtio.c_cflag &= ~CRTSCTS;

	/* set no software stream control */
	newtio.c_iflag &= ~(IXON | INLCR | ICRNL | IGNCR | IUCLC);
	/* set output mode with no define*/
	newtio.c_oflag &= ~OPOST;
	/* set input mode with non-format */
	newtio.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	newtio.c_iflag |= IGNBRK|IGNPAR; //for 0xd,0x11,0x13

	switch (nBits) {
		case 7:
			newtio.c_cflag |= CS7;
			break;
		case 8:
			newtio.c_cflag |= CS8;
			break;
	}

	switch (nEvent) {
		case 'O':
			newtio.c_cflag |= PARENB;
			//	newtio.c_cflag |= PARODD;
			//	newtio.c_iflag |= (INPCK | ISTRIP);
			break;
		case 'E':
			newtio.c_iflag |= INPCK;
			newtio.c_cflag |= PARENB;
			newtio.c_cflag &= ~PARODD;
			break;
		case 'N':
			newtio.c_cflag &= ~PARENB;
			break;
	}

	switch (nSpeed) {
		case 2400:
			cfsetispeed(&newtio, B2400);
			cfsetospeed(&newtio, B2400);
			break;
		case 4800:
			cfsetispeed(&newtio, B4800);
			cfsetospeed(&newtio, B4800);
			break;
		case 9600:
			cfsetispeed(&newtio, B9600);
			cfsetospeed(&newtio, B9600);
			break;
		case 19200:
			cfsetispeed(&newtio, B19200);
			cfsetospeed(&newtio, B19200);
			break;
		case 38400:
			cfsetispeed(&newtio, B38400);
			cfsetospeed(&newtio, B38400);
			break;
		case 57600:
			cfsetispeed(&newtio, B57600);
			cfsetospeed(&newtio, B57600);
			break;
		case 115200:
			cfsetispeed(&newtio, B115200);
			cfsetospeed(&newtio, B115200);
			break;
		case 500000:
			cfsetispeed(&newtio, B500000);
			cfsetospeed(&newtio, B500000);
			break;
		case 1500000:
			cfsetispeed(&newtio, B1500000);
			cfsetospeed(&newtio, B1500000);
			break;
		default:
			cfsetispeed(&newtio, B9600);
			cfsetospeed(&newtio, B9600);
			break;
	}
	if (nStop == 1)
		newtio.c_cflag &= ~CSTOPB;
	else if (nStop == 2)
		newtio.c_cflag |= CSTOPB;
	newtio.c_cc[VTIME] = 0;
	newtio.c_cc[VMIN] = 0;
	tcflush(fd, TCIFLUSH);
	if ((tcsetattr(fd, TCSANOW, &newtio)) != 0) {
		perror("com set error");
		return -1;
	}
	return 0;
}

static int read_uart_data(int fd, char* data,  int len)
{
	struct timeval timeout;
	timeout.tv_sec = 2;
	timeout.tv_usec = 0;
	int ret = 0;
	memset(data,0,len);
	do {
		fd_set readfds;
		FD_ZERO(&readfds);
		FD_SET(fd, &readfds);
		//wait for 2 seconds if no data come
		ret = select(FD_SETSIZE, &readfds, NULL, NULL, &timeout);
		if (ret < 0)
			continue;
		if (FD_ISSET(fd, &readfds)) {
			ret = read( fd, data, len);
		}
	} while (ret < 0 && errno == EINTR);
	return ret;
}

/*
 * 设置串口属性
 * baud: 波特率
 * dataBits:数据位数
 * parity: 校验
 * stopBits:停止位
*/
int native_setOpt(JNIEnv* env, jobject thiz, jint fd,  jint baud, jint dataBits, jint parity, jint stopBits) {

	//配置串口
	char tmp;
	if(parity == 0)
		tmp = 'N';
	else if(parity == 1)
		tmp = 'O';
	else if(parity == 2)
		tmp = 'E';
	LOGD("baudrate %d databits %d parity %c stopBits %d\n",baud, dataBits, tmp, stopBits);
	int nset = set_opt(fd, baud, dataBits, tmp, stopBits);
	if (nset == -1) {
		return -1;
	}


}
/*
wr: '0', 设置rs485模块为写模式
    '1', 设置rs485模块为读模式, 注意是字符串
*/
int control_rs485(char rd){
	int fd = open("/sys/class/io_control/rs485_con", O_RDWR | O_NOCTTY);
	if (fd == -1) {
		return 2;
	}
	write(fd, &rd, 1);
	close(fd);
}
/*
 * 打开串口
 * device: /dev/ttyS1 /dev/ttyS2之类的串口名字
 * */
int native_uartInit(JNIEnv* env, jobject thiz, jstring device) {
	//打开串口
	int fd;
	char* dev = (char*)(*env)->GetStringUTFChars(env,device,0);
	LOGD("Uart name %s\n", dev);
	fd = open(dev, O_RDWR);
	if (fd == -1) {
		LOGD("###OPEN %s fail\n", dev);
		return -1;
	}
	control_rs485('1');
	(*env)->ReleaseStringUTFChars(env,device, dev);
	return fd;
}
int native_uartDestroy(JNIEnv* env, jobject thiz, jint fd) {
	close(fd);
}
/**
 * 把数据发送出去
 * byteBuf:数据缓冲
 * length: 要发送数据的长度
 */
#define uart_tcdrain(fd) ioctl(fd, TCSBRK, 1)
int native_send(JNIEnv* env, jobject thiz, jint fd, jintArray intBuf, jint length) {
	int i,ret = -1;
	jboolean isCopy;
	if(fd == -1)
		return -1;

	jint* arr = (int*)(*env)->GetIntArrayElements(env,intBuf,&isCopy);

	unsigned char *xwdata = malloc(length*sizeof(unsigned char));

	for(i=0; i<length; i++) {
		xwdata[i] = (unsigned char)arr[i];
		LOGD("send  %x ",xwdata[i]);
	}
	//tcflush(fd,   TCIOFLUSH);
	tcflush(fd,   TCOFLUSH);
	ret = write(fd, xwdata, length);
	//tcdrain函数等待所有输出都被发送。若成功为0，出错为-1
	//usleep(5000);
	if( uart_tcdrain(fd)==-1 )
		ret = -1;
	(*env)->ReleaseIntArrayElements(env,intBuf,(jbyte*)arr, JNI_ABORT);
	free(xwdata);
	xwdata = NULL;
	return ret;

}

/**
 *读取串口数据
 * byteBuf： 数据缓冲
 * length: 要读取的数据长度
 * return :
 */
int native_recv(JNIEnv* env, jobject thiz, jint fd, jintArray intBuf, jint length) {

	int i,ret = -1;
	jboolean isCopy = JNI_FALSE;
	if(fd == -1)
		return -1;
	unsigned char *xrdata = malloc(length*sizeof(unsigned char));
	unsigned int *xrdataInt = malloc(length*sizeof(unsigned int));

	//int* arr = (int*)(*env)->GetIntArrayElements(env,intBuf,&isCopy);

	ret = read_uart_data(fd, xrdata, length);

	for(i=0; i<ret; i++) {
		xrdataInt[i] = xrdata[i];
        LOGD("recv  %x ",xrdataInt[i]);
	}
	(*env)->SetIntArrayRegion(env, intBuf, 0, length, xrdataInt);
	//(*env)->ReleaseIntArrayElements(env,intBuf,(jbyte*)arr, JNI_ABORT);
	LOGD("xrdata addr = 0x%p \n", xrdata);
	free(xrdata);
	free(xrdataInt);

	return ret;

}
/*
 * 把485设置为读模式或者写模式
 * read  1 : 设置为读模式
 * 		   0 : 设置为写模式
 * */
int native_setRS485ReadWrite(JNIEnv* env, jobject thiz, jint read) {
	if(read == 1) {
		control_rs485('1');
	} else {
		control_rs485('0');
	}
	return 0;
}



/*
 * 打开RS485串口
 * */
jboolean native_rs485_Init(JNIEnv* env, jobject thiz) {
	if (rs485Fd > 0)
		return 1;
	//打开串口
	rs485Fd = open("/dev/ttyS3", O_RDWR);
	if (rs485Fd < 0) {
		return 0;
	}
	//配置串口
	int nset = set_opt(rs485Fd, 9600, 8, 'N', 1);
	if (nset < 0) {
		return 0;
	}
	control_rs485('1');
	return 1;
}

void native_rs485_Destroy(JNIEnv* env, jobject thiz) {
	close(rs485Fd);
	rs485Fd = -1;
}

/**
 * cardID：板子标号 		doorID：门号				 info: 单片机返回的数据
 * return 0表示获取信息成功，-1代表失败
 */
int native_rs485_OpenGrid(JNIEnv* env, jobject thiz, jint cardID,
						  jint doorID, jintArray info) {
	int* buf;
	int i, ret=-1, len,j;

	//协议五位  命令：0x8A   板地址：0X01-0XC8    锁地址：0X01—18    状态：0X11    校检码：前面几位异或
	char xwdata[5] = { 0X8A, (char) cardID, (char) doorID, 0x11 };
	char xrdata[5] = { 0 };
	char xrdata_tmp[50] = {0};

	int start;

	xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
//	LOGD("Open cardId %d door %d\n",cardID,doorID);
	LOGD("open send:0x%x:0x%x:0x%x:0x%x:0x%x\n",xwdata[0],xwdata[1],xwdata[2],xwdata[3],xwdata[4]);
	tcflush(rs485Fd,   TCIOFLUSH);
	control_rs485('0');
	usleep(5000);
	write(rs485Fd, xwdata, 5);
	usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
	control_rs485('1');
	ret = read_uart_data(rs485Fd, xrdata,5);
	if(ret <= 0) {
		LOGD("Open: fail to get uart data\n");

	}
	if((xrdata[0]==0x00) && (xrdata[1]==0x00)) {
		LOGD("###########open fail. board %d door %d#################\n",cardID,doorID);
	}
	//		LOGD("open ret:0x%x:0x%x:0x%x:0x%x:0x%x\n",xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);


	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
	if (buf == NULL)
		goto retern__;
	for(i=0;i<5;i++)
		buf[i] = xrdata[i];
	(*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);
	retern__:
	return 0;

}

/**
 * 获取锁控板地址
 * info : 锁控板地址列表
 */
int native_rs485_GetBoardAddress(JNIEnv* env, jobject thiz, jint addrnum, jint maxNum, jintArray info) {
	int i,ret,boardIndex,j;
	int* buf;

	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
	//协议五位  命令：0x81 板地址 0x01-0x0f  固定：0X01    状态：0X99    校检码：前面几位异或 0X19
	char xwdata[5] = { 0X81, 0X01, 0x01, 0x99 , 0x19};
	char xrdata[5] = { 0 };
	tcflush(rs485Fd,   TCIOFLUSH);
	control_rs485('0');
	boardIndex = 0;
	//遍历核心板，询问是否有回应
	for(i=0; i<maxNum; i++) {
		tcflush(rs485Fd,   TCIOFLUSH);
		control_rs485('0');
		usleep(5000);
		xwdata[1] = i;
		xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
		write(rs485Fd, xwdata, 5);
		usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
		control_rs485('1');
		ret = read_uart_data(rs485Fd, xrdata,5);
		if(ret <= 0) {
			LOGD("BoardAdress: fail to get uart data\n");
		}
		LOGD("BoardAdress ret %d:%d 0x%x:0x%x:0x%x:0x%x:0x%x\n",
			 ret, i,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);
		if(ret != 0 && (xrdata[0] == 0x81) &&
		   ((xrdata[0]^xrdata[1]^xrdata[2]^xrdata[3]^xrdata[4]) == 0x0)) {
			buf[boardIndex] = xrdata[1];
			boardIndex++;
			if (boardIndex >= addrnum)
				goto _return_;
		}
		usleep(200000);
	}
	LOGD("get address done\n");
	_return_:
	(*env)->ReleaseIntArrayElements(env, info, (jint*)buf, JNI_ABORT);
	return boardIndex;
}

/*
 * 获取协议ID
 * cardID : 锁控板卡地址
 * info : 返回板卡程序协议
 */
int native_rs485_GetProtocalID(JNIEnv* env, jobject thiz,jint cardID, jintArray info) {
	int i,ret,boardIndex,j;
	int* buf;
	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);
	//协议五位  命令：0X91, boardaddress, 0xfe, 0xfe , 0x6f
	char xwdata[5] = { 0X91, 0X0, 0xfe, 0xfe , 0x6f};
	char xrdata[5] = { 0 };

	xwdata[1] = cardID;
	xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
	tcflush(rs485Fd,   TCIOFLUSH);
	control_rs485('0');
	usleep(5000);
	write(rs485Fd, xwdata, 5);
	usleep(10000); //必须等待一段时间，rs485才会把数据发送出去
	control_rs485('1');
	ret = read_uart_data(rs485Fd, xrdata,5);
	if(ret <= 0) {
		LOGD("Protocal: fail to get uart data\n");
	}

	LOGD("Protocal ret %d, 0x%x:0x%x:0x%x:0x%x:0x%x\n",
		 ret,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4]);

	for(i=0;i<5;i++)
		buf[i] = xrdata[i];
	(*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);
	return 0;


}

/**
 * 获取锁的状态
 *
 * cardID：板子标号 		doorID：门号				 info: 单片机返回的数据
 */
int native_rs485_GetDoorState(JNIEnv* env, jobject thiz, jint boardID, jint doorID, jintArray info) {

	int i, len,ret;
	//协议五位  命令：0x80 板地址：0X01-0xC8   锁地址:0x00-0x18  命令:0x33    校检码：前面几位异或
	char xwdata[5] = { 0X80, (char)boardID, (char)doorID, 0x33};
	char xrdata[7] = { 0 };

	if(doorID == 0)
		len = 7;
	else
		len = 5;

	xwdata[4] = (xwdata[0] ^ xwdata[1] ^ xwdata[2] ^ xwdata[3]) & 0xff;
	//LOGD("get state send:0x%x:0x%x:0x%x:0x%x:0x%x\n",xwdata[0],xwdata[1],xwdata[2],xwdata[3],xwdata[4]);
	tcflush(rs485Fd,   TCIOFLUSH);
	control_rs485('0');
	usleep(5000);
	write(rs485Fd, xwdata, 5);
	usleep(10000);
	control_rs485('1');


	ret = read_uart_data(rs485Fd, xrdata,len);
	if(ret <= 0) {
		LOGD("State: fail to get uart data\n");
	}
	//ret = read(uart_fd, xrdata, len);
	if((xrdata[0]==0x00) && (xrdata[1]==0x00)) {
		LOGD("###########get state fail. board %d door %d#################\n",boardID,doorID );
	}
	LOGD("state ret %d, 0x%x:0x%x:0x%x:0x%x:0x%x:0x%x:0x%x\n",
		 ret,xrdata[0],xrdata[1],xrdata[2],xrdata[3],xrdata[4],xrdata[5],xrdata[6]);

	int* buf;

	buf = (int*)(*env)->GetIntArrayElements(env,info,NULL);

	for(i=0;i<len;i++)
		buf[i] = xrdata[i];
	(*env)->ReleaseIntArrayElements(env,info, (jint*)buf, JNI_ABORT);
	return 0;
}

static const JNINativeMethod methods1[] = {
		{"uartInit", "(Ljava/lang/String;)I", (void *)native_uartInit},
		{"uartDestroy", "(I)I", (void *)native_uartDestroy},
		{"setOpt", "(IIIII)I", (void *)native_setOpt},
		{"send", "(I[II)I", (void *)native_send},
		{"recv", "(I[II)I", (void *)native_recv},
		{"setRS485WriteRead", "(I)I", (void *)native_setRS485ReadWrite},

};
static const JNINativeMethod methods2[] = {
		{"rs485Init", "()Z", (void *)native_rs485_Init},
		{"rs485Destroy", "()V", (void *)native_rs485_Destroy},
		{"rs485GetBoardAddress", "(II[I)I", (void *)native_rs485_GetBoardAddress},
		{"rs485OpenGrid", "(II[I)I", (void *)native_rs485_OpenGrid},
		{"rs485GetDoorState", "(II[I)I", (void *)native_rs485_GetDoorState},
		{"rs485GetProtocalID", "(I[I)I", (void *)native_rs485_GetProtocalID},
};

jint JNI_OnLoad(JavaVM *jvm, void *reserved)
{
	JNIEnv *env;
	jclass cls1, cls2;

	if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6)) {
		return JNI_ERR; /* JNI version not supported */
	}
	cls1 = (*env)->FindClass(env, "com/smatek/uart/UartComm");
	if (cls1 == NULL) {
		return JNI_ERR;
	}
	cls2 = (*env)->FindClass(env, "com/smatek/uart/UartComm$Rs485");
	if (cls1 == NULL) {
		return JNI_ERR;
	}

	if ((*env)->RegisterNatives(env, cls1, methods1, sizeof(methods1)/sizeof(methods1[0])) < 0)
		return JNI_ERR;
	if ((*env)->RegisterNatives(env, cls2, methods2, sizeof(methods2)/sizeof(methods2[0])) < 0)
		return JNI_ERR;

	return JNI_VERSION_1_4;
}
