package com.jim.common.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IOReader {

	int read(ByteBuffer buffer) throws IOException;

}
