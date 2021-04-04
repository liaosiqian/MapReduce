package com.mirror;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;
import java.io.InputStream;

public class LineReaderSource{

    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private InputStream in;
    private byte[] buffer;
    // the number of bytes of real data in the buffer
    private int bufferLength = 0;
    // the current position in the buffer
    private int bufferPosn = 0;

    private static final byte CR = '\r';
    private static final byte LF = '\n';


    /**
     * Read a line terminated by one of CR, LF, or CRLF.
     */
    private int readDefaultLine(Text str, int maxLineLength, int maxBytesToConsume)
            throws IOException {
        /* We're reading data from in, but the head of the stream may be
         * already buffered in buffer, so we have several cases:
         * 1. No newline characters are in the buffer, so we need to copy
         *    everything and read another buffer from the stream.
         * 2. An unambiguously terminated line is in buffer, so we just
         *    copy to str.
         * 3. Ambiguously terminated line is in buffer, i.e. buffer ends
         *    in CR.  In this case we copy everything up to CR to str, but
         *    we also need to see what follows CR: if it's LF, then we
         *    need consume LF as well, so next call to readLine will read
         *    from after that.
         * We use a flag prevCharCR to signal if previous character was CR
         * and, if it happens to be at the end of the buffer, delay
         * consuming it until we have a chance to look at the char that
         * follows.
         */
        str.clear();
        int txtLength = 0; //tracks str.getLength(), as an optimization
        int newlineLength = 0; //length of terminating newline
        boolean prevCharCR = false; //true of prev char was CR
        // 声明要丢弃的（将要消费的）字节长度
        long bytesConsumed = 0;
        do {
            int startPosn = bufferPosn; //starting from where we left off the last time
            if (bufferPosn >= bufferLength) {
                startPosn = bufferPosn = 0;
                // 如果上一个缓冲区读到的是 \r，则要丢弃的字节长度+1
                if (prevCharCR) {
                    ++bytesConsumed; //account for CR from previous read
                }
                //从输入流读取指定长度的内容
                //bufferLength = fillBuffer(in, buffer, prevCharCR);
                if (bufferLength <= 0) {
                    break; // EOF
                }
            }
            // 遍历缓冲区数据
            for (; bufferPosn < bufferLength; ++bufferPosn) { //search for newline

                // 如果读取的字节为  \n  则表示换行（window系统 / linux系统），后续丢弃的字节需要额外+1
                if (buffer[bufferPosn] == LF) {
                    // 如果前面已经有 \r 标识（window系统），因为上一次逻辑并未累加要丢弃的位置，因此 加上 \n 需要 添加两个字节
                    // 如果前面没有 \r标识（linux系统），则只需要添加一个要丢弃的字节长度
                    newlineLength = (prevCharCR) ? 2 : 1;
                    // 缓冲去的位置 递增1，然后跳出字节比对逻辑
                    ++bufferPosn; // at next invocation proceed from following byte
                    break;
                }

                // 如果前面已经有 \r的读取表示，则表示换行(mac系统)，同样中止比对逻辑
                if (prevCharCR) { //CR + notLF, we are at notLF
                    newlineLength = 1;
                    break;
                }

                // 否则，则继续比对下一个字符是否为  \r
                prevCharCR = (buffer[bufferPosn] == CR);
            }

            // 读取的长度  =  中止比对的缓冲区位置 - 开始读取缓冲区的位置
            int readLength = bufferPosn - startPosn;

            // 如果有cr标识， 并且在行末，则其长度的计算要跟下一次读取缓冲区做合并
            // 因为如果是 window系统，要扣除2， 如果是 mac系统，则只需要扣除1
            // 因此只有当读取到写一个字节的时候，才能最终决定扣减的数量，因此此处要先行扣减1
            if (prevCharCR && newlineLength == 0) {
                --readLength; //CR at the end of the buffer
            }

            //要 丢弃的 字节长度  = 中止缓冲区读取的字节长度
            bytesConsumed += readLength;

            // 要追加的字节长度 = 读取到的内容长度 - 换行标识占用的长度
            int appendLength = readLength - newlineLength;
            // 如果要追加的字节长度  大于 最大可追加的字节长度，则对其进行截取
            if (appendLength > maxLineLength - txtLength) {
                appendLength = maxLineLength - txtLength;
            }

            // 如果从缓冲读取到的长度不为空，则将其追加到  Text 中，并且维护其长度
            if (appendLength > 0) {
                str.append(buffer, startPosn, appendLength);
                txtLength += appendLength;
            }

            // 如果还没有确定 换行的标识，并且要 丢弃的字节长度还未达到上限，则继续从缓冲区中读取信息
        } while (newlineLength == 0 && bytesConsumed < maxBytesToConsume);

        if (bytesConsumed > Integer.MAX_VALUE) {
            throw new IOException("Too many bytes before newline: " + bytesConsumed);
        }

        // 返回 要丢弃的字节长度
        return (int)bytesConsumed;
    }


}
